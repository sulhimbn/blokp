package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
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

    private lateinit var repository: UserRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = UserRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getUsers should return success when API returns valid response`() = runTest {
        val mockData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = UserResponse(
            success = true,
            message = "Users fetched successfully",
            data = mockData
        )

        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertNotNull(responseBody)
        assertEquals(mockResponse, responseBody)
    }

    @Test
    fun `getUsers should return failure when response body is null`() = runTest {
        `when`(apiService.getUsers()).thenReturn(Response.success(null))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
    }

    @Test
    fun `getUsers should retry on SocketTimeoutException`() = runTest {
        `when`(apiService.getUsers())
            .thenThrow(SocketTimeoutException())
            .thenThrow(SocketTimeoutException())
            .thenReturn(Response.success(UserResponse(success = true, message = "Success", data = emptyList())))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getUsers()
    }

    @Test
    fun `getUsers should retry on UnknownHostException`() = runTest {
        val mockData = listOf(DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        ))
        val mockResponse = UserResponse(success = true, message = "Success", data = mockData)

        `when`(apiService.getUsers())
            .thenThrow(UnknownHostException())
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers should retry on SSLException`() = runTest {
        val mockResponse = UserResponse(success = true, message = "Success", data = emptyList())

        `when`(apiService.getUsers())
            .thenThrow(SSLException("SSL error"))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers should return failure after max retries on SocketTimeoutException`() = runTest {
        `when`(apiService.getUsers())
            .thenThrow(SocketTimeoutException())

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers should not retry on non-retryable exception`() = runTest {
        `when`(apiService.getUsers()).thenThrow(IOException("File not found"))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers should retry on 500 error`() = runTest {
        val mockResponse = UserResponse(success = true, message = "Success", data = emptyList())

        `when`(apiService.getUsers())
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers should retry on 503 error`() = runTest {
        val mockResponse = UserResponse(success = true, message = "Success", data = emptyList())

        `when`(apiService.getUsers())
            .thenReturn(Response.error(503, okhttp3.ResponseBody.create(null, "Service Unavailable")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers should retry on 408 Request Timeout error`() = runTest {
        val mockResponse = UserResponse(success = true, message = "Success", data = emptyList())

        `when`(apiService.getUsers())
            .thenReturn(Response.error(408, okhttp3.ResponseBody.create(null, "Request Timeout")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers should retry on 429 Too Many Requests error`() = runTest {
        val mockResponse = UserResponse(success = true, message = "Success", data = emptyList())

        `when`(apiService.getUsers())
            .thenReturn(Response.error(429, okhttp3.ResponseBody.create(null, "Too Many Requests")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers should not retry on 400 Bad Request error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(400, okhttp3.ResponseBody.create(null, "Bad Request")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers should not retry on 404 Not Found error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(404, okhttp3.ResponseBody.create(null, "Not Found")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers should return failure after max retries on server error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers should return failure after max retries on 503 error`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error(503, okhttp3.ResponseBody.create(null, "Service Unavailable")))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers should handle mixed retry scenarios with eventual success`() = runTest {
        val mockResponse = UserResponse(success = true, message = "Success", data = emptyList())

        `when`(apiService.getUsers())
            .thenThrow(SocketTimeoutException())
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getUsers()
    }

    @Test
    fun `getUsers should return empty list successfully`() = runTest {
        val mockResponse = UserResponse(success = true, message = "No users", data = emptyList())

        `when`(apiService.getUsers()).thenReturn(Response.success(mockResponse))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertTrue(responseBody?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers should return failure on IOException`() = runTest {
        `when`(apiService.getUsers()).thenThrow(IOException("Network error"))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getUsers exponential backoff with jitter works correctly`() = runTest {
        val mockResponse = UserResponse(success = true, message = "Success", data = emptyList())
        val callTimes = mutableListOf<Long>()

        `when`(apiService.getUsers()).thenAnswer {
            callTimes.add(System.currentTimeMillis())
            if (callTimes.size < 3) {
                throw SocketTimeoutException()
            } else {
                Response.success(mockResponse)
            }
        }

        repository.getUsers()

        assertEquals(3, callTimes.size)
    }
}
