package com.example.iurankomplek.network.health

import com.example.iurankomplek.network.resilience.CircuitBreakerState
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class IntegrationHealthMetricsTest {

    @Test
    fun `CircuitBreakerMetrics with closed state is healthy`() {
        val metrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
            state = CircuitBreakerState.Closed,
            failureCount = 0,
            successCount = 10,
            lastFailureTime = null,
            lastSuccessTime = Date(),
            lastStateChange = null
        )

        assertEquals(CircuitBreakerState.Closed, metrics.state)
        assertEquals(0, metrics.failureCount)
        assertEquals(10, metrics.successCount)
    }

    @Test
    fun `CircuitBreakerMetrics with open state`() {
        val lastFailure = Date(1234567890000)
        val metrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
            state = CircuitBreakerState.Open,
            failureCount = 5,
            successCount = 0,
            lastFailureTime = lastFailure,
            lastSuccessTime = null,
            lastStateChange = lastFailure
        )

        assertEquals(CircuitBreakerState.Open, metrics.state)
        assertEquals(5, metrics.failureCount)
        assertEquals(0, metrics.successCount)
        assertEquals(lastFailure, metrics.lastFailureTime)
        assertEquals(lastFailure, metrics.lastStateChange)
    }

    @Test
    fun `RateLimiterMetrics with no rate limits`() {
        val metrics = IntegrationHealthMetrics.RateLimiterMetrics(
            totalRequests = 100,
            requestsInLastMinute = 50,
            requestsInLastSecond = 2,
            rateLimitExceededCount = 0,
            perEndpointStats = emptyMap()
        )

        assertEquals(100, metrics.totalRequests)
        assertEquals(50, metrics.requestsInLastMinute)
        assertEquals(2, metrics.requestsInLastSecond)
        assertEquals(0, metrics.rateLimitExceededCount)
        assertTrue(metrics.perEndpointStats.isEmpty())
    }

    @Test
    fun `RateLimiterMetrics with rate limit exceeded`() {
        val endpointStats = mapOf(
            "/api/v1/payments" to IntegrationHealthMetrics.RateLimiterMetrics.EndpointStats(
                requestCount = 150,
                lastRequestTime = Date(),
                rateLimitExceeded = true
            )
        )

        val metrics = IntegrationHealthMetrics.RateLimiterMetrics(
            totalRequests = 200,
            requestsInLastMinute = 100,
            requestsInLastSecond = 10,
            rateLimitExceededCount = 5,
            perEndpointStats = endpointStats
        )

        assertEquals(5, metrics.rateLimitExceededCount)
        assertEquals(1, metrics.perEndpointStats.size)
        assertTrue(metrics.perEndpointStats["/api/v1/payments"]!!.rateLimitExceeded)
    }

    @Test
    fun `EndpointStats with all fields populated`() {
        val lastRequest = Date(1234567890000)
        val stats = IntegrationHealthMetrics.RateLimiterMetrics.EndpointStats(
            requestCount = 42,
            lastRequestTime = lastRequest,
            rateLimitExceeded = false
        )

        assertEquals(42, stats.requestCount)
        assertEquals(lastRequest, stats.lastRequestTime)
        assertFalse(stats.rateLimitExceeded)
    }

    @Test
    fun `RequestMetrics with successful requests`() {
        val metrics = IntegrationHealthMetrics.RequestMetrics(
            totalRequests = 100,
            successfulRequests = 95,
            failedRequests = 5,
            retriedRequests = 10,
            averageResponseTimeMs = 150.5,
            minResponseTimeMs = 50,
            maxResponseTimeMs = 500,
            p95ResponseTimeMs = 400,
            p99ResponseTimeMs = 480
        )

        assertEquals(100, metrics.totalRequests)
        assertEquals(95, metrics.successfulRequests)
        assertEquals(5, metrics.failedRequests)
        assertEquals(10, metrics.retriedRequests)
        assertEquals(150.5, metrics.averageResponseTimeMs, 0.01)
    }

    @Test
    fun `RequestMetrics with no requests has zero values`() {
        val metrics = IntegrationHealthMetrics.RequestMetrics(
            totalRequests = 0,
            successfulRequests = 0,
            failedRequests = 0,
            retriedRequests = 0,
            averageResponseTimeMs = 0.0,
            minResponseTimeMs = 0,
            maxResponseTimeMs = 0,
            p95ResponseTimeMs = 0,
            p99ResponseTimeMs = 0
        )

        assertEquals(0, metrics.totalRequests)
        assertEquals(0.0, metrics.averageResponseTimeMs, 0.01)
    }

    @Test
    fun `ErrorMetrics with various error types`() {
        val httpErrors = mapOf(400 to 5, 500 to 3)
        val metrics = IntegrationHealthMetrics.ErrorMetrics(
            totalErrors = 10,
            timeoutErrors = 2,
            connectionErrors = 1,
            httpErrors = httpErrors,
            circuitBreakerErrors = 3,
            rateLimitErrors = 4,
            unknownErrors = 0
        )

        assertEquals(10, metrics.totalErrors)
        assertEquals(2, metrics.timeoutErrors)
        assertEquals(1, metrics.connectionErrors)
        assertEquals(3, metrics.circuitBreakerErrors)
        assertEquals(4, metrics.rateLimitErrors)
        assertEquals(0, metrics.unknownErrors)
        assertEquals(5, metrics.httpErrors[400])
        assertEquals(3, metrics.httpErrors[500])
    }

    @Test
    fun `ErrorMetrics with no errors`() {
        val metrics = IntegrationHealthMetrics.ErrorMetrics(
            totalErrors = 0,
            timeoutErrors = 0,
            connectionErrors = 0,
            httpErrors = emptyMap(),
            circuitBreakerErrors = 0,
            rateLimitErrors = 0,
            unknownErrors = 0
        )

        assertEquals(0, metrics.totalErrors)
        assertTrue(metrics.httpErrors.isEmpty())
    }

    @Test
    fun `IntegrationHealthMetrics is healthy when all conditions are good`() {
        val circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
            state = CircuitBreakerState.Closed,
            failureCount = 0,
            successCount = 10,
            lastFailureTime = null,
            lastSuccessTime = Date(),
            lastStateChange = null
        )

        val rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
            totalRequests = 100,
            requestsInLastMinute = 50,
            requestsInLastSecond = 2,
            rateLimitExceededCount = 0,
            perEndpointStats = emptyMap()
        )

        val requestMetrics = IntegrationHealthMetrics.RequestMetrics(
            totalRequests = 100,
            successfulRequests = 100,
            failedRequests = 0,
            retriedRequests = 0,
            averageResponseTimeMs = 100.0,
            minResponseTimeMs = 50,
            maxResponseTimeMs = 150,
            p95ResponseTimeMs = 140,
            p99ResponseTimeMs = 145
        )

        val errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
            totalErrors = 0,
            timeoutErrors = 0,
            connectionErrors = 0,
            httpErrors = emptyMap(),
            circuitBreakerErrors = 0,
            rateLimitErrors = 0,
            unknownErrors = 0
        )

        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = circuitBreakerMetrics,
            rateLimiterMetrics = rateLimiterMetrics,
            requestMetrics = requestMetrics,
            errorMetrics = errorMetrics
        )

        assertTrue(metrics.isHealthy())
    }

    @Test
    fun `IntegrationHealthMetrics is not healthy when circuit breaker is open`() {
        val circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
            state = CircuitBreakerState.Open,
            failureCount = 5,
            successCount = 0,
            lastFailureTime = Date(),
            lastSuccessTime = null,
            lastStateChange = Date()
        )

        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = circuitBreakerMetrics,
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertFalse(metrics.isHealthy())
    }

    @Test
    fun `IntegrationHealthMetrics is not healthy when rate limit exceeded`() {
        val rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
            totalRequests = 100,
            requestsInLastMinute = 50,
            requestsInLastSecond = 2,
            rateLimitExceededCount = 5,
            perEndpointStats = emptyMap()
        )

        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = rateLimiterMetrics,
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertFalse(metrics.isHealthy())
    }

    @Test
    fun `IntegrationHealthMetrics is not healthy when circuit breaker errors exist`() {
        val errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
            totalErrors = 5,
            timeoutErrors = 0,
            connectionErrors = 0,
            httpErrors = emptyMap(),
            circuitBreakerErrors = 5,
            rateLimitErrors = 0,
            unknownErrors = 0
        )

        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = errorMetrics
        )

        assertFalse(metrics.isHealthy())
    }

    @Test
    fun `getHealthScore returns 100 for perfectly healthy metrics`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 10,
                lastFailureTime = null,
                lastSuccessTime = Date(),
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 100,
                requestsInLastMinute = 50,
                requestsInLastSecond = 2,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 100,
                successfulRequests = 100,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 100.0,
                minResponseTimeMs = 50,
                maxResponseTimeMs = 150,
                p95ResponseTimeMs = 140,
                p99ResponseTimeMs = 145
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(100.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore subtracts 50 when circuit breaker is open`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Open,
                failureCount = 5,
                successCount = 0,
                lastFailureTime = Date(),
                lastSuccessTime = null,
                lastStateChange = Date()
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(50.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore subtracts 25 when circuit breaker is half open`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.HalfOpen,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = Date()
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(75.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore subtracts for rate limit errors`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 3,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(70.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore rate limit errors capped at 30 points`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 100,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(70.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore subtracts for circuit breaker errors`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 2,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(70.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore circuit breaker errors capped at 45 points`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 0,
                successfulRequests = 0,
                failedRequests = 0,
                retriedRequests = 0,
                averageResponseTimeMs = 0.0,
                minResponseTimeMs = 0,
                maxResponseTimeMs = 0,
                p95ResponseTimeMs = 0,
                p99ResponseTimeMs = 0
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 100,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(55.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore subtracts for request failure rate`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 100,
                successfulRequests = 80,
                failedRequests = 20,
                retriedRequests = 0,
                averageResponseTimeMs = 100.0,
                minResponseTimeMs = 50,
                maxResponseTimeMs = 150,
                p95ResponseTimeMs = 140,
                p99ResponseTimeMs = 145
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(90.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore failure rate capped at 40 points`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Closed,
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 0,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 100,
                successfulRequests = 0,
                failedRequests = 100,
                retriedRequests = 0,
                averageResponseTimeMs = 100.0,
                minResponseTimeMs = 50,
                maxResponseTimeMs = 150,
                p95ResponseTimeMs = 140,
                p99ResponseTimeMs = 145
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 0,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(60.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `getHealthScore never goes below zero`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.Open,
                failureCount = 100,
                successCount = 0,
                lastFailureTime = Date(),
                lastSuccessTime = null,
                lastStateChange = Date()
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = 0,
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = 100,
                perEndpointStats = emptyMap()
            ),
            requestMetrics = IntegrationHealthMetrics.RequestMetrics(
                totalRequests = 100,
                successfulRequests = 0,
                failedRequests = 100,
                retriedRequests = 0,
                averageResponseTimeMs = 100.0,
                minResponseTimeMs = 50,
                maxResponseTimeMs = 150,
                p95ResponseTimeMs = 140,
                p99ResponseTimeMs = 145
            ),
            errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
                totalErrors = 0,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 100,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertEquals(0.0, metrics.getHealthScore(), 0.01)
    }

    @Test
    fun `IntegrationHealthTracker records successful request`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordRequest(150, success = true)

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.requestMetrics.totalRequests)
        assertEquals(1, metrics.requestMetrics.successfulRequests)
        assertEquals(0, metrics.requestMetrics.failedRequests)
    }

    @Test
    fun `IntegrationHealthTracker records failed request`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordRequest(200, success = false)

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.requestMetrics.totalRequests)
        assertEquals(0, metrics.requestMetrics.successfulRequests)
        assertEquals(1, metrics.requestMetrics.failedRequests)
    }

    @Test
    fun `IntegrationHealthTracker records retry`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordRetry()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.requestMetrics.retriedRequests)
    }

    @Test
    fun `IntegrationHealthTracker records timeout error`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordTimeoutError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.timeoutErrors)
    }

    @Test
    fun `IntegrationHealthTracker records connection error`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordConnectionError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.connectionErrors)
    }

    @Test
    fun `IntegrationHealthTracker records circuit breaker error`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordCircuitBreakerError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.circuitBreakerErrors)
    }

    @Test
    fun `IntegrationHealthTracker records rate limit error`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordRateLimitError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.rateLimitErrors)
    }

    @Test
    fun `IntegrationHealthTracker records unknown error`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordUnknownError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.unknownErrors)
    }

    @Test
    fun `IntegrationHealthTracker records HTTP error`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordHttpError(404)
        tracker.recordHttpError(500)

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.httpErrors[404])
        assertEquals(1, metrics.errorMetrics.httpErrors[500])
    }

    @Test
    fun `IntegrationHealthTracker records multiple same HTTP errors`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordHttpError(404)
        tracker.recordHttpError(404)
        tracker.recordHttpError(404)

        val metrics = tracker.generateMetrics()
        assertEquals(3, metrics.errorMetrics.httpErrors[404])
    }

    @Test
    fun `IntegrationHealthTracker calculates response time statistics`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordRequest(100, true)
        tracker.recordRequest(150, true)
        tracker.recordRequest(200, true)
        tracker.recordRequest(250, true)
        tracker.recordRequest(300, true)

        val metrics = tracker.generateMetrics()
        assertEquals(100, metrics.requestMetrics.minResponseTimeMs)
        assertEquals(300, metrics.requestMetrics.maxResponseTimeMs)
        assertEquals(200.0, metrics.requestMetrics.averageResponseTimeMs, 0.01)
    }

    @Test
    fun `IntegrationHealthTracker with no requests has zero statistics`() {
        val tracker = IntegrationHealthTracker()
        val metrics = tracker.generateMetrics()

        assertEquals(0, metrics.requestMetrics.totalRequests)
        assertEquals(0.0, metrics.requestMetrics.averageResponseTimeMs, 0.01)
        assertEquals(0, metrics.requestMetrics.minResponseTimeMs)
        assertEquals(0, metrics.requestMetrics.maxResponseTimeMs)
    }

    @Test
    fun `IntegrationHealthTracker reset clears all metrics`() {
        val tracker = IntegrationHealthTracker()
        tracker.recordRequest(100, true)
        tracker.recordRequest(200, false)
        tracker.recordRetry()
        tracker.recordTimeoutError()
        tracker.recordHttpError(500)

        tracker.reset()

        val metrics = tracker.generateMetrics()
        assertEquals(0, metrics.requestMetrics.totalRequests)
        assertEquals(0, metrics.requestMetrics.successfulRequests)
        assertEquals(0, metrics.requestMetrics.failedRequests)
        assertEquals(0, metrics.requestMetrics.retriedRequests)
        assertEquals(0, metrics.errorMetrics.timeoutErrors)
        assertTrue(metrics.errorMetrics.httpErrors.isEmpty())
    }

    @Test
    fun `IntegrationHealthTracker is thread safe`() {
        val tracker = IntegrationHealthTracker()
        val threads = List(10) {
            Thread {
                repeat(100) {
                    tracker.recordRequest(100, it % 2 == 0)
                    tracker.recordRetry()
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val metrics = tracker.generateMetrics()
        assertEquals(1000, metrics.requestMetrics.totalRequests)
        assertEquals(1000, metrics.requestMetrics.retriedRequests)
    }

    @Test
    fun `IntegrationHealthTracker response times are capped at MAX_RESPONSE_TIMES_HISTORY`() {
        val tracker = IntegrationHealthTracker()
        repeat(1000) {
            tracker.recordRequest(100, true)
        }

        val metrics = tracker.generateMetrics()
        assertEquals(1000, metrics.requestMetrics.totalRequests)
        assertTrue(metrics.requestMetrics.averageResponseTimeMs > 0)
    }
}
