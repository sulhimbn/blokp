package com.example.iurankomplek.data.repository

import com.example.iurankomplek.TestFixtures
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.Response
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class PemanfaatanRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: PemanfaatanRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = mock(ApiService::class.java)
        CacheManager.getInstance().clearSync()
        repository = PemanfaatanRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        Dispatchers.resetMain()
    }

    private fun errorResponse(code: Int): Response<PemanfaatanResponse> =
        Response.error(code, "{}".toResponseBody("application/json".toMediaType()))

    @Test
    fun `getPemanfaatan returns the utilisation rows from the API`() = runTest(testDispatcher) {
        val payload = PemanfaatanResponse(
            listOf(
                TestFixtures.dataItem(pemanfaatanIuran = "Perbaikan jalan"),
                TestFixtures.dataItem(pemanfaatanIuran = "Pengecatan")
            )
        )
        `when`(apiService.getPemanfaatan()).thenReturn(Response.success(payload))

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        assertEquals(
            listOf("Perbaikan jalan", "Pengecatan"),
            result.getOrNull()?.data?.map { it.pemanfaatan_iuran }
        )
    }

    @Test
    fun `getPemanfaatan maps a 404 and does not retry it`() = runTest(testDispatcher) {
        `when`(apiService.getPemanfaatan()).thenReturn(errorResponse(404))

        val result = repository.getPemanfaatan()

        assertEquals("Resource not found", result.exceptionOrNull()?.message)
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `getPemanfaatan retries a 500 four times in total then reports Server error`() =
        runTest(testDispatcher) {
            `when`(apiService.getPemanfaatan()).thenReturn(errorResponse(500))

            val result = repository.getPemanfaatan()

            assertEquals("Server error", result.exceptionOrNull()?.message)
            verify(apiService, times(4)).getPemanfaatan()
        }

    @Test
    fun `getPemanfaatan keeps the status code for an unmapped 5xx`() =
        runTest(testDispatcher) {
            `when`(apiService.getPemanfaatan()).thenReturn(errorResponse(503))

            val result = repository.getPemanfaatan()

            assertEquals("HTTP Error: 503", result.exceptionOrNull()?.message)
            verify(apiService, times(4)).getPemanfaatan()
        }

    @Test
    fun `getPemanfaatan retries UnknownHostException and reports No internet connection`() =
        runTest(testDispatcher) {
            `when`(apiService.getPemanfaatan()).thenAnswer { throw UnknownHostException("dns") }

            val result = repository.getPemanfaatan()

            assertEquals("No internet connection", result.exceptionOrNull()?.message)
            verify(apiService, times(4)).getPemanfaatan()
        }

    @Test
    fun `getPemanfaatan is served from cache on the second call`() = runTest(testDispatcher) {
        val payload = PemanfaatanResponse(listOf(TestFixtures.dataItem()))
        `when`(apiService.getPemanfaatan()).thenReturn(Response.success(payload))

        val first = repository.getPemanfaatan()
        val second = repository.getPemanfaatan()

        assertEquals(payload, first.getOrNull())
        assertEquals(payload, second.getOrNull())
        verify(apiService, times(1)).getPemanfaatan()
    }

    @Test
    fun `a null body on a 200 is reported as Response body is null`() = runTest(testDispatcher) {
        `when`(apiService.getPemanfaatan()).thenReturn(Response.success(null))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }
}
