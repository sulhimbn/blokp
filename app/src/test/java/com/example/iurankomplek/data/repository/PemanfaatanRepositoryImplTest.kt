package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiService
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
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
class PemanfaatanRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: PemanfaatanRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private fun dataItem() = DataItem(
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

    private fun httpError(code: Int, message: String) =
        Response.error<PemanfaatanResponse>(
            code,
            message.toResponseBody("text/plain".toMediaType())
        )

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
    fun `getPemanfaatan returns the payload the API produced`() = runTest {
        val expected = PemanfaatanResponse(listOf(dataItem()))
        whenever(apiService.getPemanfaatan()).thenReturn(Response.success(expected))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getPemanfaatan fails with an explicit message when the body is null`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenReturn(Response.success<PemanfaatanResponse>(null))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getPemanfaatan retries a socket timeout and eventually succeeds`() = runTest {
        val expected = PemanfaatanResponse(listOf(dataItem()))
        whenever(apiService.getPemanfaatan())
            .thenAnswer { throw SocketTimeoutException() }
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(Response.success(expected))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(3)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan retries an unknown host`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenAnswer { throw UnknownHostException() }
            .thenReturn(Response.success(PemanfaatanResponse(listOf(dataItem()))))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan retries an SSL failure`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenAnswer { throw SSLException("SSL error") }
            .thenReturn(Response.success(PemanfaatanResponse(emptyList())))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan gives up after the retry budget is exhausted`() = runTest {
        whenever(apiService.getPemanfaatan()).thenAnswer { throw SocketTimeoutException() }

        assertTrue(repository.getPemanfaatan().isFailure)
        verify(apiService, times(4)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan does not retry a non-retryable IOException`() = runTest {
        whenever(apiService.getPemanfaatan()).thenAnswer { throw IOException("File not found") }

        assertTrue(repository.getPemanfaatan().isFailure)
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan retries a 500`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenReturn(httpError(500, "Internal Server Error"))
            .thenReturn(Response.success(PemanfaatanResponse(emptyList())))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan retries a 503`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenReturn(httpError(503, "Service Unavailable"))
            .thenReturn(Response.success(PemanfaatanResponse(emptyList())))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan retries a 408 Request Timeout`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenReturn(httpError(408, "Request Timeout"))
            .thenReturn(Response.success(PemanfaatanResponse(emptyList())))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan retries a 429 Too Many Requests`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenReturn(httpError(429, "Too Many Requests"))
            .thenReturn(Response.success(PemanfaatanResponse(emptyList())))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(2)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan does not retry a 400`() = runTest {
        whenever(apiService.getPemanfaatan()).thenReturn(httpError(400, "Bad Request"))

        assertTrue(repository.getPemanfaatan().isFailure)
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan does not retry a 404`() = runTest {
        whenever(apiService.getPemanfaatan()).thenReturn(httpError(404, "Not Found"))

        assertTrue(repository.getPemanfaatan().isFailure)
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan gives up after exhausting retries on a persistent 500`() = runTest {
        whenever(apiService.getPemanfaatan()).thenReturn(httpError(500, "Internal Server Error"))

        assertTrue(repository.getPemanfaatan().isFailure)
        verify(apiService, times(4)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan gives up after exhausting retries on a persistent 503`() = runTest {
        whenever(apiService.getPemanfaatan()).thenReturn(httpError(503, "Service Unavailable"))

        assertTrue(repository.getPemanfaatan().isFailure)
        verify(apiService, times(4)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan survives a mixed failure sequence`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenAnswer { throw SocketTimeoutException() }
            .thenReturn(httpError(500, "Internal Server Error"))
            .thenReturn(Response.success(PemanfaatanResponse(emptyList())))

        assertTrue(repository.getPemanfaatan().isSuccess)
        verify(apiService, times(3)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan returns an empty feed as a success`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenReturn(Response.success(PemanfaatanResponse(emptyList())))

        val result = repository.getPemanfaatan()

        assertTrue("an empty ledger is not an error", result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getPemanfaatan backs off in virtual time between retries`() = runTest {
        var attempts = 0
        whenever(apiService.getPemanfaatan()).thenAnswer {
            attempts++
            if (attempts < 3) throw SocketTimeoutException()
            Response.success(PemanfaatanResponse(emptyList()))
        }

        val start = testScheduler.currentTime
        assertTrue(repository.getPemanfaatan().isSuccess)

        assertEquals(3, attempts)
        assertTrue(
            "expected backoff to advance virtual time by at least 3000ms",
            testScheduler.currentTime - start >= 3000L
        )
    }

    @Test
    fun `getPemanfaatan serves a repeat call from cache instead of the network`() = runTest {
        whenever(apiService.getPemanfaatan())
            .thenReturn(Response.success(PemanfaatanResponse(listOf(dataItem()))))

        repository.getPemanfaatan()
        repository.getPemanfaatan()

        verify(apiService, times(1)).getPemanfaatan()
    }
}
