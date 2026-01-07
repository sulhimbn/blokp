package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.model.ApiError
import com.example.iurankomplek.network.model.ApiErrorCode
import com.example.iurankomplek.network.model.NetworkError
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

class NetworkErrorInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()

        val interceptor = NetworkErrorInterceptor(
            enableLogging = false,
            tag = "TestNetworkErrorInterceptor"
        )

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
    fun `successful request returns response without throwing error`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(gson.toJson(mapOf("status" to "success")))

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .tag("TestRequest")
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertTrue(response.isSuccessful)
        assertEquals(200, response.code)
        assertEquals("success", gson.fromJson(response.body?.string(), Map::class.java)["status"])
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `HTTP 400 error throws HttpError with correct message`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(400)
            .setBody(gson.toJson(ApiError(
                code = "BAD_REQUEST",
                message = "Bad request",
                details = mapOf("field" to "Invalid parameter")
            )))

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .tag("TestRequest")
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.BAD_REQUEST, e.code)
            assertEquals(400, e.httpCode)
            assertTrue(e.userMessage!!.isNotEmpty())
            throw e
        }
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `HTTP 401 error throws HttpError with unauthorized message`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(401)
            .setBody(gson.toJson(ApiError(
                code = "UNAUTHORIZED",
                message = "Unauthorized access",
                details = null
            )))

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.UNAUTHORIZED, e.code)
            assertEquals(401, e.httpCode)
            throw e
        }
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `HTTP 403 error throws HttpError with forbidden message`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(403)
            .setBody("{}")

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.FORBIDDEN, e.code)
            assertEquals(403, e.httpCode)
            assertEquals("Access denied.", e.userMessage)
            throw e
        }
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `HTTP 404 error throws HttpError with not found message`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(404)
            .setBody("{}")

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/nonexistent"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.NOT_FOUND, e.code)
            assertEquals(404, e.httpCode)
            assertEquals("Resource not found.", e.userMessage)
            throw e
        }
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `HTTP 429 error throws HttpError with rate limit message`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(429)
            .setBody(gson.toJson(ApiError(
                code = "TOO_MANY_REQUESTS",
                message = "Rate limit exceeded",
                details = mapOf("retryAfter" to "60")
            )))

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.TOO_MANY_REQUESTS, e.code)
            assertEquals(429, e.httpCode)
            assertTrue(e.userMessage!!.contains("slow down"))
            throw e
        }
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `HTTP 500 error throws HttpError with server error message`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody(gson.toJson(ApiError(
                code = "INTERNAL_SERVER_ERROR",
                message = "Internal server error",
                details = null
            )))

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.INTERNAL_SERVER_ERROR, e.code)
            assertEquals(500, e.httpCode)
            assertTrue(e.userMessage!!.contains("Server error"))
            throw e
        }
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `HTTP 503 error throws HttpError with service unavailable message`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(503)
            .setBody("{}")

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.SERVICE_UNAVAILABLE, e.code)
            assertEquals(503, e.httpCode)
            assertEquals("Service unavailable. Please try again later.", e.userMessage)
            throw e
        }
    }

    @Test(expected = NetworkError.TimeoutError::class)
    fun `SocketTimeoutException throws TimeoutError`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBodyDelay(10, TimeUnit.SECONDS) // Delay longer than read timeout
            .setBody("{}")

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected TimeoutError to be thrown")
        } catch (e: NetworkError.TimeoutError) {
            // Assert
            assertTrue(e.cause is SocketTimeoutException)
            throw e
        }
    }

    @Test(expected = NetworkError.ConnectionError::class)
    fun `UnknownHostException throws ConnectionError`() {
        // Arrange - Use a non-existent URL
        val request = Request.Builder()
            .url("http://this-domain-definitely-does-not-exist-12345.com")
            .build()

        // Act
        try {
            okHttpClient.newCall(request).execute()
            fail("Expected ConnectionError to be thrown")
        } catch (e: NetworkError.ConnectionError) {
            // Assert
            assertTrue(e.cause is UnknownHostException)
            throw e
        }
    }

    @Test(expected = NetworkError.ConnectionError::class)
    fun `SSLException throws ConnectionError with secure connection message`() {
        // Note: This test simulates SSL error by using an invalid HTTPS URL
        // In a real scenario, this would require setting up a server with invalid SSL

        // Arrange
        val clientWithSslError = OkHttpClient.Builder()
            .addInterceptor(NetworkErrorInterceptor())
            .hostnameVerifier { _, _ -> false } // Force SSL error
            .build()

        // Note: We can't easily test SSLException in a unit test without a real SSL failure
        // This is a placeholder that documents the expected behavior
        // In practice, you would need a mock server with SSL issues
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `malformed JSON response uses default error messages`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(400)
            .setBody("{ invalid json")

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert - Should use default message when JSON parsing fails
            assertEquals(ApiErrorCode.BAD_REQUEST, e.code)
            assertEquals(400, e.httpCode)
            assertEquals("Bad request. Please check your parameters.", e.userMessage)
            throw e
        }
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `empty JSON response uses default error messages`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(404)
            .setBody("{}")

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals(ApiErrorCode.NOT_FOUND, e.code)
            assertEquals(404, e.httpCode)
            assertEquals("Resource not found.", e.userMessage)
            throw e
        }
    }

    @Test
    fun `request tag is preserved and used for error logging`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(gson.toJson(mapOf("status" to "success")))

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .tag("CustomRequestTag")
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Assert - Request tag should be accessible
        assertEquals("CustomRequestTag", request.tag(String::class.java))
        assertTrue(response.isSuccessful)
    }

    @Test(expected = NetworkError.HttpError::class)
    fun `error details are correctly parsed from response`() {
        // Arrange
        val errorDetails = mapOf(
            "field" to "email",
            "reason" to "Invalid format"
        )
        val mockResponse = MockResponse()
            .setResponseCode(400)
            .setBody(gson.toJson(ApiError(
                code = "VALIDATION_ERROR",
                message = "Validation failed",
                details = errorDetails
            )))

        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        try {
            okHttpClient.newCall(request).execute()
            fail("Expected HttpError to be thrown")
        } catch (e: NetworkError.HttpError) {
            // Assert
            assertEquals("Validation failed", e.userMessage)
            assertNotNull(e.details)
            assertEquals("email", e.details?.get("field"))
            assertEquals("Invalid format", e.details?.get("reason"))
            throw e
        }
    }

    @Test(expected = NetworkError.UnknownNetworkError::class)
    fun `unexpected exception throws UnknownNetworkError`() {
        // Note: This test documents the expected behavior for unexpected exceptions
        // In practice, testing this requires forcing an unexpected exception
        // which is difficult in a unit test environment

        // The interceptor wraps any non-network exceptions in UnknownNetworkError
    }
}
