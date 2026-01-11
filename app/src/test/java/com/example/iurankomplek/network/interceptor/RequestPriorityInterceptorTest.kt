package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.interceptor.RequestPriority
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class RequestPriorityInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val interceptor = RequestPriorityInterceptor()
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `payments confirm endpoint gets CRITICAL priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/payments/123/confirm")
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        val response = client.newCall(request).execute()
        val priority = request.tag(RequestPriority::class.java)

        assertNotNull(priority)
        assertEquals(RequestPriority.CRITICAL, priority)
        assertEquals("CRITICAL", response.header("X-Priority"))
    }

    @Test
    fun `payments initiate endpoint gets CRITICAL priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/payments/initiate")
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.CRITICAL, priority)
        assertEquals("CRITICAL", response.header("X-Priority"))
    }

    @Test
    fun `payments status endpoint gets HIGH priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/payments/123/status")
            .get()
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.HIGH, priority)
        assertEquals("HIGH", response.header("X-Priority"))
    }

    @Test
    fun `health endpoint gets CRITICAL priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/health")
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.CRITICAL, priority)
        assertEquals("CRITICAL", response.header("X-Priority"))
    }

    @Test
    fun `auth endpoints get CRITICAL priority`() {
        val loginRequest = Request.Builder()
            .url("https://api.example.com/api/v1/auth/login")
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        val priority = loginRequest.tag(RequestPriority::class.java)
        val response = client.newCall(loginRequest).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.CRITICAL, priority)
        assertEquals("CRITICAL", response.header("X-Priority"))
    }

    @Test
    fun `create user POST gets HIGH priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/users")
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.HIGH, priority)
        assertEquals("HIGH", response.header("X-Priority"))
    }

    @Test
    fun `GET users gets NORMAL priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/users")
            .get()
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.NORMAL, priority)
        assertEquals("NORMAL", response.header("X-Priority"))
    }

    @Test
    fun `announcements GET gets LOW priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/announcements")
            .get()
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.LOW, priority)
        assertEquals("LOW", response.header("X-Priority"))
    }

    @Test
    fun `background sync gets BACKGROUND priority`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/background-sync")
            .get()
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.BACKGROUND, priority)
        assertEquals("BACKGROUND", response.header("X-Priority"))
    }

    @Test
    fun `unknown endpoint gets NORMAL priority by default`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/v1/unknown-endpoint")
            .get()
            .build()

        val priority = request.tag(RequestPriority::class.java)
        val response = client.newCall(request).execute()

        assertNotNull(priority)
        assertEquals(RequestPriority.NORMAL, priority)
        assertEquals("NORMAL", response.header("X-Priority"))
    }
}
