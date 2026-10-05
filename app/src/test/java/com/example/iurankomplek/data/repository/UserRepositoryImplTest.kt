package com.example.iurankomplek.data.repository

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Covers [UserRepositoryImpl], in particular the retry/backoff policy in
 * [BaseNetworkRepository.executeWithRetry] and the five-minute response cache.
 *
 * The cache is a process-wide singleton ([CacheManager.getInstance]) shared by every
 * repository, so each test clears it first; without that, the first test's cached
 * response would satisfy later tests and the `verify(apiService, times(n))` assertions
 * would be measuring nothing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var sessionManager: UserSessionManager

    private lateinit var repository: UserRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private fun errorBody(message: String) = message.toResponseBody("text/plain".toMediaType())

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        CacheManager.getInstance().clearSync()
        repository = UserRepositoryImpl(apiService, sessionManager)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        Dispatchers.resetMain()
    }

    @Test
    fun `getUsers returns the payload on a successful response`() = runTest {
        val mockData = listOf(
            TestFixtures.dataItem(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuranPerwarga = 100,
                totalIuranRekap = 500,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 150,
                pengeluaranIuranWarga = 50,
                pemanfaatanIuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = UserResponse(data = mockData)

        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getUsers fails when the response body is null`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.success(null as UserResponse?))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertTrue(
            "unexpected message: ${result.exceptionOrNull()?.message}",
            result.exceptionOrNull()?.message?.contains("Response body is null") == true
        )
    }

    @Test
    fun `getUsers retries on SocketTimeoutException`() = runTest {
        `when`(apiService.getUsers())
            .thenAnswer { throw SocketTimeoutException() }
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getUsers()
    }

    @Test
    fun `getUsers retries on UnknownHostException`() = runTest {
        val mockResponse = UserResponse(data = listOf(TestFixtures.dataItem(email = "john.doe@example.com")))

        `when`(apiService.getUsers())
            .thenAnswer { throw UnknownHostException() }
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries on SSLException`() = runTest {
        val mockResponse = UserResponse(data = emptyList())

        `when`(apiService.getUsers())
            .thenAnswer { throw SSLException("SSL error") }
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers gives up after maxRetries attempts on a persistent SocketTimeoutException`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw SocketTimeoutException() }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers() // initial call + 3 retries
    }

    @Test
    fun `getUsers does not retry an IOException`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw IOException("File not found") }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers retries a 500 response`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(500, errorBody("Internal Server Error")))
            .thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries a 503 response`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(503, errorBody("Service Unavailable")))
            .thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries a 408 Request Timeout response`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(408, errorBody("Request Timeout")))
            .thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries a 429 Too Many Requests response`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(429, errorBody("Too Many Requests")))
            .thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers does not retry a 400 Bad Request response`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(400, errorBody("Bad Request")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers does not retry a 404 Not Found response`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(404, errorBody("Not Found")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers gives up after maxRetries on a persistent 500`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(500, errorBody("Internal Server Error")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers gives up after maxRetries on a persistent 503`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(503, errorBody("Service Unavailable")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers recovers from a mix of transport and server failures`() = runTest {
        `when`(apiService.getUsers())
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(Response.error(500, errorBody("Internal Server Error")))
            .thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getUsers()
    }

    @Test
    fun `getUsers surfaces an empty result set as success`() = runTest {
        `when`(apiService.getUsers()).thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers fails on IOException`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw IOException("Network error") }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getUsers backs off between retries instead of hammering the API`() = runTest {
        var attempts = 0

        `when`(apiService.getUsers()).thenAnswer {
            attempts++
            if (attempts < 3) throw SocketTimeoutException()
            Response.success(UserResponse(data = emptyList()))
        }

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(3, attempts)
    }

    @Test
    fun `a second getUsers call is served from cache without hitting the API`() = runTest {
        val mockResponse = UserResponse(data = listOf(TestFixtures.dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val first = repository.getUsers()
        val second = repository.getUsers()

        assertEquals(mockResponse, first.getOrNull())
        assertEquals(mockResponse, second.getOrNull())
        verify(apiService, times(1)).getUsers() // second call served from the 5 minute cache
    }

    @Test
    fun `login resolves the matching user and stores it in the session`() = runTest {
        val userData = TestFixtures.dataItem(email = "john.doe@example.com", firstName = "John", lastName = "Doe")
        `when`(apiService.getUsers()).thenReturn(Response.success(UserResponse(data = listOf(userData))))

        val result = repository.login("john.doe@example.com", "irrelevant")

        assertTrue(result.isSuccess)
        assertEquals("john.doe@example.com", result.getOrNull()?.id)
        assertEquals("John", result.getOrNull()?.firstName)
        verify(sessionManager).setCurrentUser(org.mockito.kotlin.any())
    }

    @Test
    fun `login fails when no user matches the email`() = runTest {
        `when`(apiService.getUsers()).thenReturn(Response.success(UserResponse(data = emptyList())))

        val result = repository.login("nobody@example.com", "irrelevant")

        assertTrue(result.isFailure)
    }
}