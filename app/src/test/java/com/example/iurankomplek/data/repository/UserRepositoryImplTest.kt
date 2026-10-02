package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var sessionManager: UserSessionManager

    private lateinit var repository: UserRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private fun dataItem(email: String = "john.doe@example.com") = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    private fun httpError(code: Int, message: String) =
        Response.error<UserResponse>(code, message.toResponseBody("text/plain".toMediaType()))

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
    fun `getUsers returns the payload the API produced`() = runTest {
        val expected = UserResponse(listOf(dataItem()))
        whenever(apiService.getUsers()).thenReturn(Response.success(expected))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getUsers fails with an explicit message when the body is null`() = runTest {
        whenever(apiService.getUsers()).thenReturn(Response.success<UserResponse>(null))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUsers retries a socket timeout and eventually succeeds`() = runTest {
        val expected = UserResponse(emptyList())
        whenever(apiService.getUsers())
            .thenAnswer { throw SocketTimeoutException() }
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(Response.success(expected))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getUsers()
    }

    @Test
    fun `getUsers retries an unknown host`() = runTest {
        val expected = UserResponse(listOf(dataItem()))
        whenever(apiService.getUsers())
            .thenAnswer { throw UnknownHostException() }
            .thenReturn(Response.success(expected))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries an SSL failure`() = runTest {
        val expected = UserResponse(emptyList())
        whenever(apiService.getUsers())
            .thenAnswer { throw SSLException("SSL error") }
            .thenReturn(Response.success(expected))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers gives up after the retry budget is exhausted`() = runTest {
        whenever(apiService.getUsers()).thenAnswer { throw SocketTimeoutException() }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers does not retry a non-retryable IOException`() = runTest {
        whenever(apiService.getUsers()).thenAnswer { throw IOException("File not found") }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers retries a 500`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(httpError(500, "Internal Server Error"))
            .thenReturn(Response.success(UserResponse(emptyList())))

        assertTrue(repository.getUsers().isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries a 503`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(httpError(503, "Service Unavailable"))
            .thenReturn(Response.success(UserResponse(emptyList())))

        assertTrue(repository.getUsers().isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries a 408 Request Timeout`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(httpError(408, "Request Timeout"))
            .thenReturn(Response.success(UserResponse(emptyList())))

        assertTrue(repository.getUsers().isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries a 429 Too Many Requests`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(httpError(429, "Too Many Requests"))
            .thenReturn(Response.success(UserResponse(emptyList())))

        assertTrue(repository.getUsers().isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers does not retry a 400`() = runTest {
        whenever(apiService.getUsers()).thenReturn(httpError(400, "Bad Request"))

        assertTrue(repository.getUsers().isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers does not retry a 404`() = runTest {
        whenever(apiService.getUsers()).thenReturn(httpError(404, "Not Found"))

        assertTrue(repository.getUsers().isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers gives up after exhausting retries on a persistent 500`() = runTest {
        whenever(apiService.getUsers()).thenReturn(httpError(500, "Internal Server Error"))

        assertTrue(repository.getUsers().isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers survives a mixed failure sequence and still succeeds`() = runTest {
        whenever(apiService.getUsers())
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(httpError(500, "Internal Server Error"))
            .thenReturn(Response.success(UserResponse(emptyList())))

        assertTrue(repository.getUsers().isSuccess)
        verify(apiService, times(3)).getUsers()
    }

    @Test
    fun `getUsers returns an empty list as a success`() = runTest {
        whenever(apiService.getUsers()).thenReturn(Response.success(UserResponse(emptyList())))

        val result = repository.getUsers()

        assertTrue("an empty roster is not an error", result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers surfaces a raw IOException as a failure`() = runTest {
        whenever(apiService.getUsers()).thenAnswer { throw IOException("Network error") }

        assertTrue(repository.getUsers().isFailure)
    }

    @Test
    fun `getUsers backs off between retries in virtual time`() = runTest {
        var attempts = 0
        whenever(apiService.getUsers()).thenAnswer {
            attempts++
            if (attempts < 3) throw SocketTimeoutException()
            Response.success(UserResponse(emptyList()))
        }

        val start = testScheduler.currentTime
        assertTrue(repository.getUsers().isSuccess)

        assertEquals(3, attempts)
        // 1000ms + 2000ms of backoff at minimum; jitter only adds time.
        assertTrue(
            "expected backoff delay to advance virtual time",
            testScheduler.currentTime - start >= 3000L
        )
    }

    @Test
    fun `getUsers serves a repeat call from cache instead of the network`() = runTest {
        val expected = UserResponse(listOf(dataItem()))
        whenever(apiService.getUsers()).thenReturn(Response.success(expected))

        repository.getUsers()
        repository.getUsers()

        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `login stores the authenticated user in the session`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(Response.success(UserResponse(listOf(dataItem()))))

        val result = repository.login("john.doe@example.com", "any-password")

        assertTrue(result.isSuccess)
        assertEquals("john.doe@example.com", result.getOrNull()?.email)
        assertEquals("John Doe", result.getOrNull()?.fullName)
        verify(sessionManager, times(1)).setCurrentUser(result.getOrNull()!!)
    }

    @Test
    fun `login rejects an email that is not in the roster`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(Response.success(UserResponse(listOf(dataItem()))))

        val result = repository.login("nobody@example.com", "any-password")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
        verify(sessionManager, never()).setCurrentUser(org.mockito.kotlin.any())
    }

    @Test
    fun `login propagates an upstream failure without creating a session`() = runTest {
        whenever(apiService.getUsers()).thenAnswer { throw IOException("Network error") }

        val result = repository.login("john.doe@example.com", "any-password")

        assertTrue(result.isFailure)
        verify(sessionManager, never()).setCurrentUser(org.mockito.kotlin.any())
    }

    @Test
    fun `login does not consult the API twice for a cached roster`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(Response.success(UserResponse(listOf(dataItem()))))

        repository.login("john.doe@example.com", "pw")
        repository.login("john.doe@example.com", "pw")

        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `logout clears the session and the cached roster`() = runTest {
        whenever(apiService.getUsers())
            .thenReturn(Response.success(UserResponse(listOf(dataItem()))))
        repository.getUsers()

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify(sessionManager, times(1)).clearSession()

        repository.getUsers()
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `logout succeeds even when nothing is cached`() = runTest {
        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify(sessionManager, times(1)).clearSession()
    }

    @Test
    fun `current user flow is the exact flow the session manager exposes`() {
        val flow = MutableStateFlow<User?>(null)
        whenever(sessionManager.currentUser).thenReturn(flow)

        assertTrue(repository.getCurrentUserFlow() === flow)
    }
}
