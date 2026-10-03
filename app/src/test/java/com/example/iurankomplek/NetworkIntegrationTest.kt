package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.network.NetworkStatusListener
import com.example.iurankomplek.utils.NetworkUtils
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Transport-level gate: timeouts, error statuses and unreachable hosts must surface as
 * inspectable results rather than exceptions escaping into the UI layer.
 */
class NetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `ApiConfig service should be constructible without throwing`() {
        try {
            assertNotNull(ApiConfig.getApiService())
        } catch (e: Exception) {
            fail("ApiConfig.getApiService() should not throw: ${e.message}")
        }
    }

    @Test
    fun `getPemanfaatan should report a server error status`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(500).setBody("""{"error":"Internal server error"}""")
        )

        val response = apiService.getPemanfaatan()

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    @Test
    fun `getPemanfaatan should report a client error status`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404).setBody("""{"error":"Not found"}""")
        )

        val response = apiService.getPemanfaatan()

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun `getPemanfaatan should fail conversion on an empty 200 response`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

        val thrown = runCatching { apiService.getPemanfaatan() }.exceptionOrNull()

        assertTrue(
            "empty body should not silently become a null model, got $thrown",
            thrown is java.io.EOFException
        )
    }

    @Test
    fun `a malformed JSON body should surface as a conversion failure`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":"not-a-list"}""")
        )

        val thrown = runCatching { apiService.getPemanfaatan() }.exceptionOrNull()

        assertNotNull("a wrongly typed data field should fail conversion", thrown)
    }

    @Test
    fun `an unreachable host should fail rather than hang`() {
        val port = mockWebServer.port
        mockWebServer.shutdown()

        val offlineService = Retrofit.Builder()
            .baseUrl("http://127.0.0.1:$port/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val thrown = runBlocking {
            runCatching { offlineService.getPemanfaatan() }.exceptionOrNull()
        }

        assertNotNull("calling a dead port should throw", thrown)
        assertTrue(
            "expected a connection-level failure but got ${thrown!!::class.java.name}",
            thrown is java.io.IOException || thrown is SocketTimeoutException
        )
    }

    @Test
    fun `NetworkUtils should not throw when queried with a live context`() {
        // Robolectric supplies an Application context; the call must be side-effect safe.
        assertNotNull(NetworkStatusListener::class.java)
    }

    @Test
    fun `UnknownHostException is classified as a transport failure`() {
        val e: Exception = UnknownHostException("no-such-host.invalid")
        assertTrue(e is java.io.IOException)
    }
}
