package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class TimeoutInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(TimeoutInterceptor())
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `FAST timeout profile for health endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"status\":\"OK\"}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/health")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.FAST, profile)
        assertEquals(Constants.Network.FAST_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `FAST timeout profile for status endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"status\":\"COMPLETED\"}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/payments/123/status")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.FAST, profile)
        assertEquals(Constants.Network.FAST_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `SLOW timeout profile for payment initiation`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"paymentId\":\"123\"}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/payments/initiate")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.SLOW, profile)
        assertEquals(Constants.Network.SLOW_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for users endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"users\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/users")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for pemanfaatan endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"data\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/pemanfaatan")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for vendors endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"vendors\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/vendors")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for work orders endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"workOrders\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/work-orders")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for announcements endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"announcements\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/announcements")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for messages endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"messages\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/messages")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for community posts endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"posts\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/community-posts")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `NORMAL timeout profile for unknown endpoints`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/unknown-endpoint")}")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val profile = TimeoutProfileConfig.getTimeoutForPath(request.url.encodedPath)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)

        assertEquals(200, response.code)
        assertEquals(TimeoutProfile.NORMAL, profile)
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, timeoutMs)
    }

    @Test
    fun `getTimeoutMs returns correct values for all profiles`() {
        assertEquals(Constants.Network.FAST_TIMEOUT_MS, TimeoutProfileConfig.getTimeoutMs(TimeoutProfile.FAST))
        assertEquals(Constants.Network.NORMAL_TIMEOUT_MS, TimeoutProfileConfig.getTimeoutMs(TimeoutProfile.NORMAL))
        assertEquals(Constants.Network.SLOW_TIMEOUT_MS, TimeoutProfileConfig.getTimeoutMs(TimeoutProfile.SLOW))
    }

    @Test
    fun `health endpoint uses FAST timeout profile`() {
        val profile = TimeoutProfileConfig.getTimeoutForPath("/api/v1/health")
        assertEquals(TimeoutProfile.FAST, profile)
    }

    @Test
    fun `payment status endpoint uses FAST timeout profile`() {
        val profile = TimeoutProfileConfig.getTimeoutForPath("/api/v1/payments/abc123/status")
        assertEquals(TimeoutProfile.FAST, profile)
    }

    @Test
    fun `payment initiation endpoint uses SLOW timeout profile`() {
        val profile = TimeoutProfileConfig.getTimeoutForPath("/api/v1/payments/initiate")
        assertEquals(TimeoutProfile.SLOW, profile)
    }

    @Test
    fun `FAST timeout is 5 seconds`() {
        assertEquals(5000L, Constants.Network.FAST_TIMEOUT_MS)
    }

    @Test
    fun `NORMAL timeout is 30 seconds`() {
        assertEquals(30000L, Constants.Network.NORMAL_TIMEOUT_MS)
    }

    @Test
    fun `SLOW timeout is 60 seconds`() {
        assertEquals(60000L, Constants.Network.SLOW_TIMEOUT_MS)
    }
}
