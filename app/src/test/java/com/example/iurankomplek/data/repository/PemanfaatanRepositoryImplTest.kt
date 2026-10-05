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
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        CacheManager.getInstance().clearSync()
        repository = PemanfaatanRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
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
        val mockResponse = PemanfaatanResponse(data = mockData)

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
    fun `getPemanfaatan should return empty list successfully`() = runTest {
        val mockResponse = PemanfaatanResponse(data = emptyList())

        `when`(apiService.getPemanfaatan()).thenReturn(Response.success(mockResponse))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertTrue(responseBody?.data?.isEmpty() == true)
    }

}
