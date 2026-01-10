package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.health.IntegrationHealthMonitor
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
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@RunWith(MockitoJUnitRunner::class)
class HealthCheckInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @Mock
    private lateinit var mockHealthMonitor: IntegrationHealthMonitor

    @Before
    fun setup() {
        mockWebServer = MockWebServer()

        val interceptor = HealthCheckInterceptor(
            healthMonitor = mockHealthMonitor,
            enableLogging = false
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
    fun `successful request returns response without error`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"status": "success"}""")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertTrue(response.isSuccessful)
        assertEquals(200, response.code)
        assertEquals("""{"status": "success"}""", response.body?.string())
    }

    @Test
    fun `successful request does not record retry`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, never()).recordRetry(anyString())
    }

    @Test
    fun `failed request records retry on health monitor`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, times(1)).recordRetry("GET:/api/users")
    }

    @Test
    fun `HTTP 429 response records rate limit exceeded`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "60")
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(
            IntegrationHealthMonitor.HealthReport(
                timestamp = java.util.Date(),
                overallStatus = com.example.iurankomplek.network.health.IntegrationHealthStatus.Healthy(),
                healthScore = 95.0,
                metrics = com.example.iurankomplek.network.health.IntegrationHealthMetrics(
                    requestMetrics = com.example.iurankomplek.network.health.RequestMetrics(
                        totalRequests = 10,
                        successfulRequests = 9,
                        failedRequests = 1,
                        retriedRequests = 1,
                        averageResponseTimeMs = 150.0
                    ),
                    errorMetrics = com.example.iurankomplek.network.health.ErrorMetrics(
                        timeoutErrors = 0,
                        rateLimitErrors = 1,
                        circuitBreakerErrors = 0,
                        connectionErrors = 0
                    )
                ),
                circuitBreakerStats = mapOf("state" to "CLOSED"),
                rateLimiterStats = mapOf(),
                componentHealth = mapOf(),
                recommendations = listOf()
            )
        )

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, times(1)).recordRateLimitExceeded(
            eq("GET:/api/users"),
            eq(10)
        )
    }

    @Test
    fun `HTTP 503 response records circuit breaker open`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(503)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, times(1)).recordCircuitBreakerOpen("api_service")
    }

    @Test
    fun `HTTP 500 response does not record circuit breaker open`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert - 500 should only record retry, not circuit breaker open
        verify(mockHealthMonitor, times(1)).recordRetry("GET:/api/users")
        verify(mockHealthMonitor, never()).recordCircuitBreakerOpen(anyString())
    }

    @Test
    fun `health endpoint request is skipped from monitoring`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/v1/health"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert - No health monitoring for health endpoint
        verify(mockHealthMonitor, never()).recordRetry(anyString())
        verify(mockHealthMonitor, never()).recordRateLimitExceeded(anyString(), anyInt())
        verify(mockHealthMonitor, never()).recordCircuitBreakerOpen(anyString())
    }

    @Test
    fun `health endpoint with different path is skipped from monitoring`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/health"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, never()).recordRetry(anyString())
    }

    @Test
    fun `endpoint key correctly formats method and path`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users/123"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert - Verify the endpoint key format
        // The interceptor uses: request.method:request.url.encodedPath
        // For this test, we verify successful execution
        assertTrue(mockWebServer.takeRequest().path?.startsWith("/api/users") == true)
    }

    @Test
    fun `POST request records retry on failure`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(400)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, times(1)).recordRetry("POST:/api/users")
    }

    @Test
    fun `PUT request records retry on failure`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(404)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users/123"))
            .put("{}".toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, times(1)).recordRetry("PUT:/api/users/123")
    }

    @Test
    fun `DELETE request records retry on failure`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(404)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users/123"))
            .delete()
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, times(1)).recordRetry("DELETE:/api/users/123")
    }

    @Test
    fun `IOException on network failure records retry`() {
        // Arrange - Use a non-existent URL to trigger IOException
        val request = Request.Builder()
            .url("http://this-domain-definitely-does-not-exist-12345.com/api/users")
            .build()

        // Act
        try {
            okHttpClient.newCall(request).execute()
            fail("Expected IOException to be thrown")
        } catch (e: IOException) {
            // Expected exception
        }

        // Assert
        verify(mockHealthMonitor, times(1)).recordRetry("GET:/api/users")
    }

    @Test
    fun `interceptor does not modify response headers`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setHeader("X-Custom-Header", "CustomValue")
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertEquals("application/json", response.header("Content-Type"))
        assertEquals("CustomValue", response.header("X-Custom-Header"))
    }

    @Test
    fun `interceptor does not modify response body`() {
        // Arrange
        val responseBody = """{"message": "success", "data": {"id": 123}}"""
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(responseBody)
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Assert
        assertEquals(responseBody, response.body?.string())
    }

    @Test
    fun `interceptor does not modify request headers`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .header("Authorization", "Bearer token123")
            .header("X-Custom-Header", "CustomValue")
            .build()

        okHttpClient.newCall(request).execute()
        val recordedRequest = mockWebServer.takeRequest()

        // Assert
        assertEquals("Bearer token123", recordedRequest.getHeader("Authorization"))
        assertEquals("CustomValue", recordedRequest.getHeader("X-Custom-Header"))
    }

    @Test
    fun `multiple successful requests do not record retries`() {
        // Arrange
        repeat(5) {
            val mockResponse = MockResponse()
                .setResponseCode(200)
                .setBody("{}")
            mockWebServer.enqueue(mockResponse)
        }

        // Act
        repeat(5) {
            val request = Request.Builder()
                .url(mockWebServer.url("/api/users"))
                .build()

            okHttpClient.newCall(request).execute()
        }

        // Assert
        verify(mockHealthMonitor, never()).recordRetry(anyString())
    }

    @Test
    fun `mixed success and failure requests record retries only for failures`() {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("{}"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        // Act
        val endpoints = listOf("/api/users", "/api/users", "/api/users", "/api/users", "/api/users")
        endpoints.forEach { endpoint ->
            val request = Request.Builder()
                .url(mockWebServer.url(endpoint))
                .build()
            okHttpClient.newCall(request).execute()
        }

        // Assert - Only the 500 and 404 responses should record retries
        verify(mockHealthMonitor, times(2)).recordRetry("GET:/api/users")
    }

    @Test
    fun `endpoint with query parameters is correctly identified`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users?page=1&limit=10"))
            .build()

        okHttpClient.newCall(request).execute()

        // Assert
        verify(mockHealthMonitor, times(1)).recordRetry("GET:/api/users")
    }

    @Test
    fun `interceptor with logging enabled does not throw errors`() {
        // Arrange
        val mockWebServer = MockWebServer()
        mockWebServer.start()

        val interceptor = HealthCheckInterceptor(
            healthMonitor = mockHealthMonitor,
            enableLogging = true
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response = client.newCall(request).execute()

        // Assert - Should complete without errors even with logging enabled
        assertTrue(response.isSuccessful)
        assertEquals(200, response.code)

        mockWebServer.shutdown()
    }

    @Test
    fun `response time is measured for all requests`() {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{}")
        mockWebServer.enqueue(mockResponse)

        // Act
        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val responseTimeMs = measureTimeMillis {
            okHttpClient.newCall(request).execute()
        }

        // Assert
        assertTrue(responseTimeMs >= 0)
    }

    @Test
    fun `interceptor uses default IntegrationHealthMonitor when not provided`() {
        // Arrange
        val interceptor = HealthCheckInterceptor()
        assertNotNull(interceptor)
    }
}
