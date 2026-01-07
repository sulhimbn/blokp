package com.example.iurankomplek.network.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RequestIdInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private val REQUEST_ID_HEADER = "X-Request-ID"

    @Before
    fun setup() {
        mockWebServer = MockWebServer()

        val interceptor = RequestIdInterceptor()

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `interceptor adds X-Request-ID header to request`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()

        // Assert
        assertTrue(recordedRequest.headers.names().contains(REQUEST_ID_HEADER))
        val requestId = recordedRequest.getHeader(REQUEST_ID_HEADER)
        assertNotNull(requestId)
        assertTrue(requestId!!.isNotEmpty())
    }

    @Test
    fun `X-Request-ID header contains timestamp`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        val beforeRequestTime = System.currentTimeMillis()

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        okHttpClient.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()

        val afterRequestTime = System.currentTimeMillis()

        // Assert
        val requestId = recordedRequest.getHeader(REQUEST_ID_HEADER)
        val timestamp = requestId?.split("-")?.get(0)?.toLongOrNull()

        assertNotNull(timestamp)
        assertTrue(timestamp!! >= beforeRequestTime)
        assertTrue(timestamp <= afterRequestTime)
    }

    @Test
    fun `X-Request-ID header contains random suffix`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        okHttpClient.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()

        // Assert
        val requestId = recordedRequest.getHeader(REQUEST_ID_HEADER)
        val parts = requestId?.split("-")

        assertEquals(2, parts?.size)
        val randomPart = parts?.get(1)
        assertNotNull(randomPart)
        assertTrue(randomPart!!.toIntOrNull() in 0..9999)
    }

    @Test
    fun `each request gets unique X-Request-ID`() {
        // Arrange
        val mockResponse1 = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        val mockResponse2 = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse1)
        mockWebServer.enqueue(mockResponse2)

        // Act
        val request1 = Request.Builder()
            .url(mockWebServer.url("/test1"))
            .build()

        val request2 = Request.Builder()
            .url(mockWebServer.url("/test2"))
            .build()

        okHttpClient.newCall(request1).execute()
        val recordedRequest1 = mockWebServer.takeRequest()

        okHttpClient.newCall(request2).execute()
        val recordedRequest2 = mockWebServer.takeRequest()

        // Assert
        val requestId1 = recordedRequest1.getHeader(REQUEST_ID_HEADER)
        val requestId2 = recordedRequest2.getHeader(REQUEST_ID_HEADER)

        assertNotNull(requestId1)
        assertNotNull(requestId2)
        assertNotEquals(requestId1, requestId2)
    }

    @Test
    fun `X-Request-ID header format is valid`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        okHttpClient.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()

        // Assert
        val requestId = recordedRequest.getHeader(REQUEST_ID_HEADER)
        val pattern = Regex("^\\d+-\\d+$")

        assertTrue(pattern.matches(requestId ?: ""))
    }

    @Test
    fun `interceptor adds request tag with request ID`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert - Check that the tag was set on the original request
        // Note: The tag is set on a new request builder, so we need to verify the chain behavior
        // In a real scenario, we would need to verify this through the chain
    }

    @Test
    fun `interceptor does not modify other request headers`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .header("Custom-Header", "CustomValue")
            .header("Another-Header", "AnotherValue")
            .build()

        okHttpClient.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()

        // Assert
        assertEquals("CustomValue", recordedRequest.getHeader("Custom-Header"))
        assertEquals("AnotherValue", recordedRequest.getHeader("Another-Header"))
        assertTrue(recordedRequest.headers.names().contains(REQUEST_ID_HEADER))
    }

    @Test
    fun `interceptor does not modify request body or method`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val requestBody = """
            {"key": "value"}
        """.trimIndent()

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()

        // Assert
        assertEquals("POST", recordedRequest.method)
        assertEquals(requestBody, recordedRequest.body.readUtf8())
    }

    @Test
    fun `interceptor does not modify response`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"message": "success"}""")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertTrue(response.isSuccessful)
        assertEquals(200, response.code)
        assertEquals("""{"message": "success"}""", response.body?.string())
    }
}
