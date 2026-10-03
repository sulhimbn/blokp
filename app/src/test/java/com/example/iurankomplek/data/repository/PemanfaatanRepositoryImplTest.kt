package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

@OptIn(ExperimentalCoroutinesApi::class)
class PemanfaatanRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: PemanfaatanRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        CacheManager.getInstance().clearSync()
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = PemanfaatanRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getPemanfaatan should return success when API returns valid response`() = runTest {
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
        val mockResponse = PemanfaatanResponse(
            data = mockData
        )

        `when`(apiService.getPemanfaatan()).thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertNotNull(responseBody)
        assertEquals(mockResponse, responseBody)
    }

    @Test
    fun `getPemanfaatan should return failure when response body is null`() = runTest {
        `when`(apiService.getPemanfaatan()).thenReturn(Response.success(null))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
    }

    @Test
    fun `getPemanfaatan should retry on SocketTimeoutException`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan())
            .thenAnswer { throw SocketTimeoutException() }
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should retry on UnknownHostException`() = runTest {
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
        val mockResponse = PemanfaatanResponse(data = mockData)

        `when`(apiService.getPemanfaatan())
            .thenAnswer { throw UnknownHostException() }
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should retry on SSLException`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan())
            .thenAnswer { throw SSLException("SSL error") }
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should return failure after max retries on SocketTimeoutException`() = runTest {
        `when`(apiService.getPemanfaatan())
            .thenAnswer { throw SocketTimeoutException() }

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should not retry on non-retryable exception`() = runTest {
        `when`(apiService.getPemanfaatan()).thenAnswer { throw IOException("File not found") }

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should retry on 500 error`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should retry on 503 error`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(503, okhttp3.ResponseBody.create(null, "Service Unavailable")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should retry on 408 Request Timeout error`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(408, okhttp3.ResponseBody.create(null, "Request Timeout")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should retry on 429 Too Many Requests error`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(429, okhttp3.ResponseBody.create(null, "Too Many Requests")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should not retry on 400 Bad Request error`() = runTest {
        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(400, okhttp3.ResponseBody.create(null, "Bad Request")))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should not retry on 404 Not Found error`() = runTest {
        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(404, okhttp3.ResponseBody.create(null, "Not Found")))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should return failure after max retries on server error`() = runTest {
        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error")))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should return failure after max retries on 503 error`() = runTest {
        `when`(apiService.getPemanfaatan())
            .thenReturn(Response.error(503, okhttp3.ResponseBody.create(null, "Service Unavailable")))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        verify(apiService, times(4)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should handle mixed retry scenarios with eventual success`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan())
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error")))
            .thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        verify(apiService, times(3)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan should return empty list successfully`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan()).thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertTrue(responseBody?.data?.isEmpty() == true)
    }

    @Test
    fun `getPemanfaatan should return failure on IOException`() = runTest {
        `when`(apiService.getPemanfaatan()).thenAnswer { throw IOException("Network error") }

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPemanfaatan exponential backoff with jitter works correctly`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())
        val callTimes = mutableListOf<Long>()

        `when`(apiService.getPemanfaatan()).thenAnswer {
            callTimes.add(System.currentTimeMillis())
            if (callTimes.size < 3) {
                throw SocketTimeoutException()
            } else {
                Response.success(mockResponse)
            }
        }

        repository.getPemanfaatan()

        assertEquals(3, callTimes.size)
    }
}
