package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        CacheManager.getInstance().clearSync()
        repository = UserRepositoryImpl(apiService, sessionManager)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
    }

    @Test
    fun `getUsers returns success when API returns valid response`() = runTest {
        val mockResponse = UserResponse(data = listOf(dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        assertEquals("john.doe@example.com", result.getOrNull()?.data?.first()?.email)
    }

    @Test
    fun `getUsers returns failure when response body is null`() = runTest {
        @Suppress("UNCHECKED_CAST")
        `when`(apiService.getUsers())
            .thenReturn(Response.success(null) as Response<UserResponse>)

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
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
        val mockResponse = UserResponse(data = listOf(dataItem()))
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
    fun `getUsers returns failure after max retries on SocketTimeoutException`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw SocketTimeoutException() }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers does not retry on non-retryable exception`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw IOException("File not found") }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers retries on 500 error`() = runTest {
        val mockResponse = UserResponse(data = emptyList())
        `when`(apiService.getUsers())
            .thenReturn(Response.error(500, "Internal Server Error".toResponseBody(null)))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries on 503 error`() = runTest {
        val mockResponse = UserResponse(data = emptyList())
        `when`(apiService.getUsers())
            .thenReturn(Response.error(503, "Service Unavailable".toResponseBody(null)))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries on 408 Request Timeout error`() = runTest {
        val mockResponse = UserResponse(data = emptyList())
        `when`(apiService.getUsers())
            .thenReturn(Response.error(408, "Request Timeout".toResponseBody(null)))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers retries on 429 Too Many Requests error`() = runTest {
        val mockResponse = UserResponse(data = emptyList())
        `when`(apiService.getUsers())
            .thenReturn(Response.error(429, "Too Many Requests".toResponseBody(null)))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers does not retry on 400 Bad Request error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(400, "Bad Request".toResponseBody(null)))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers does not retry on 404 Not Found error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(404, "Not Found".toResponseBody(null)))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers returns failure after max retries on server error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(500, "Internal Server Error".toResponseBody(null)))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers returns failure after max retries on 503 error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(503, "Service Unavailable".toResponseBody(null)))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers handles mixed retry scenarios with eventual success`() = runTest {
        val mockResponse = UserResponse(data = emptyList())
        `when`(apiService.getUsers())
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(Response.error(500, "Internal Server Error".toResponseBody(null)))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getUsers()
    }

    @Test
    fun `getUsers returns empty list successfully`() = runTest {
        val mockResponse = UserResponse(data = emptyList())
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers returns failure on IOException`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw IOException("Network error") }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getUsers stops retrying after the configured attempt budget`() = runTest {
        val mockResponse = UserResponse(data = emptyList())
        var attempts = 0
        `when`(apiService.getUsers()).thenAnswer {
            attempts++
            if (attempts < 3) throw SocketTimeoutException()
            Response.success(mockResponse)
        }

        repository.getUsers()

        assertEquals(3, attempts)
    }

    @Test
    fun `second getUsers call is served from cache`() = runTest {
        val mockResponse = UserResponse(data = listOf(dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        repository.getUsers()
        repository.getUsers()

        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `login succeeds for a known email and stores the session`() = runTest {
        val mockResponse = UserResponse(data = listOf(dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val result = repository.login("john.doe@example.com", "any-password")

        assertTrue(result.isSuccess)
        assertEquals("john.doe@example.com", result.getOrNull()?.email)
        assertEquals("John", result.getOrNull()?.firstName)
        verify(sessionManager).setCurrentUser(result.getOrNull()!!)
    }

    @Test
    fun `login fails for an unknown email`() = runTest {
        val mockResponse = UserResponse(data = listOf(dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val result = repository.login("nobody@example.com", "any-password")

        assertTrue(result.isFailure)
    }

    @Test
    fun `login rejects a blank password`() = runTest {
        val mockResponse = UserResponse(data = listOf(dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        assertTrue(repository.login("john.doe@example.com", "").isFailure)
        assertTrue(repository.login("john.doe@example.com", "   ").isFailure)
    }

    @Test
    fun `login rejects a blank email`() = runTest {
        val mockResponse = UserResponse(data = listOf(dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        assertTrue(repository.login("", "any-password").isFailure)
    }

    @Test
    fun `logout clears the session and the user cache`() = runTest {
        val mockResponse = UserResponse(data = listOf(dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))
        repository.login("john.doe@example.com", "any-password")

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify(sessionManager).clearSession()

        repository.getUsers()
        verify(apiService, times(2)).getUsers()
    }
}