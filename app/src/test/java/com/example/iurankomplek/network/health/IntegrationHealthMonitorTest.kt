package com.example.iurankomplek.network.health

import com.example.iurankomplek.network.resilience.CircuitBreakerState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class IntegrationHealthMonitorTest {

    private lateinit var healthMonitor: IntegrationHealthMonitor

    @Before
    fun setup() {
        healthMonitor = IntegrationHealthMonitor.getInstance()
        healthMonitor.reset()
    }

    @Test
    fun `initial health status is healthy`() = runTest {
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.Healthy)
        assertEquals("HEALTHY", status.status)
        assertTrue(status.isHealthy())
        assertFalse(status.isDegraded())
        assertFalse(status.isUnhealthy())
    }

    @Test
    fun `health status transitions to degraded on retry`() = runTest {
        healthMonitor.recordRetry("/api/v1/users")
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.Degraded)
        assertEquals("DEGRADED", status.status)
        assertTrue(status.isDegraded())
        assertFalse(status.isHealthy())
        assertFalse(status.isUnhealthy())
    }

    @Test
    fun `health status transitions to unhealthy on circuit open`() = runTest {
        healthMonitor.recordCircuitBreakerOpen("api_service")
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.CircuitOpen)
        assertEquals("CIRCUIT_OPEN", status.status)
        assertTrue(status.isUnhealthy())
        assertFalse(status.isHealthy())
        assertFalse(status.isDegraded())
    }

    @Test
    fun `health status transitions to rate limited`() = runTest {
        healthMonitor.recordRateLimitExceeded("/api/v1/users", 65)
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.RateLimited)
        assertEquals("RATE_LIMITED", status.status)
        assertTrue(status.isUnhealthy())
    }

    @Test
    fun `health status transitions to unhealthy on request failure`() = runTest {
        healthMonitor.recordRequest(
            endpoint = "/api/v1/users",
            responseTimeMs = 200,
            success = false,
            httpCode = 500
        )
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.Unhealthy || status is IntegrationHealthStatus.Degraded)
    }

    @Test
    fun `health report includes all metrics`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 150, true, 200)
        healthMonitor.recordRequest("/api/v1/users", 200, false, 500)

        val report = healthMonitor.getDetailedHealthReport()

        assertNotNull(report)
        assertNotNull(report.metrics)
        assertNotNull(report.circuitBreakerStats)
        assertNotNull(report.rateLimiterStats)
        assertNotNull(report.componentHealth)
        assertTrue(report.recommendations.isNotEmpty())
    }

    @Test
    fun `health report shows correct request counts`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 150, true, 200)
        healthMonitor.recordRequest("/api/v1/users", 200, true, 200)
        healthMonitor.recordRequest("/api/v1/users", 300, false, 500)

        val report = healthMonitor.getDetailedHealthReport()

        assertEquals(3, report.metrics.requestMetrics.totalRequests)
        assertEquals(2, report.metrics.requestMetrics.successfulRequests)
        assertEquals(1, report.metrics.requestMetrics.failedRequests)
    }

    @Test
    fun `health report shows correct response times`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 100, true, 200)
        healthMonitor.recordRequest("/api/v1/users", 200, true, 200)
        healthMonitor.recordRequest("/api/v1/users", 300, false, 500)

        val report = healthMonitor.getDetailedHealthReport()

        assertTrue(report.metrics.requestMetrics.averageResponseTimeMs > 0)
        assertEquals(100, report.metrics.requestMetrics.minResponseTimeMs)
        assertEquals(300, report.metrics.requestMetrics.maxResponseTimeMs)
    }

    @Test
    fun `health score is between 0 and 100`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 150, true, 200)

        val report = healthMonitor.getDetailedHealthReport()

        assertTrue(report.healthScore >= 0.0)
        assertTrue(report.healthScore <= 100.0)
    }

    @Test
    fun `health score decreases on failures`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 150, true, 200)
        val healthyScore = healthMonitor.getDetailedHealthReport().healthScore

        healthMonitor.recordRequest("/api/v1/users", 200, false, 500)
        healthMonitor.recordRequest("/api/v1/users", 300, false, 500)

        val degradedScore = healthMonitor.getDetailedHealthReport().healthScore

        assertTrue(degradedScore < healthyScore)
    }

    @Test
    fun `recommendations are generated for unhealthy status`() = runTest {
        healthMonitor.recordCircuitBreakerOpen("api_service")
        val report = healthMonitor.getDetailedHealthReport()

        assertTrue(report.recommendations.isNotEmpty())
        assertTrue(report.recommendations.any { it.contains("circuit breaker", ignoreCase = true) })
    }

    @Test
    fun `recommendations are generated for rate limited status`() = runTest {
        healthMonitor.recordRateLimitExceeded("/api/v1/users", 65)
        val report = healthMonitor.getDetailedHealthReport()

        assertTrue(report.recommendations.isNotEmpty())
        assertTrue(report.recommendations.any { it.contains("rate limit", ignoreCase = true) })
    }

    @Test
    fun `recommendations are generated for degraded status`() = runTest {
        healthMonitor.recordRetry("/api/v1/users")
        val report = healthMonitor.getDetailedHealthReport()

        assertTrue(report.recommendations.isNotEmpty())
    }

    @Test
    fun `recommendations are generated for healthy status`() = runTest {
        val report = healthMonitor.getDetailedHealthReport()

        assertTrue(report.recommendations.isNotEmpty())
        assertTrue(report.recommendations.any { it.contains("healthy", ignoreCase = true) })
    }

    @Test
    fun `reset clears all health data`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 150, true, 200)
        healthMonitor.recordCircuitBreakerOpen("api_service")
        healthMonitor.recordRateLimitExceeded("/api/v1/users", 65)

        healthMonitor.reset()
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.Healthy)
    }

    @Test
    fun `singleton pattern returns same instance`() {
        val instance1 = IntegrationHealthMonitor.getInstance()
        val instance2 = IntegrationHealthMonitor.getInstance()

        assertSame(instance1, instance2)
    }
}

@RunWith(MockitoJUnitRunner::class)
class IntegrationHealthStatusTest {

    @Test
    fun `healthy status is correctly identified`() {
        val status = IntegrationHealthStatus.Healthy()

        assertTrue(status.isHealthy())
        assertFalse(status.isDegraded())
        assertFalse(status.isUnhealthy())
        assertEquals("HEALTHY", status.status)
    }

    @Test
    fun `degraded status is correctly identified`() {
        val status = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("api_service"),
            message = "Retry detected",
            lastSuccessfulRequest = Date()
        )

        assertFalse(status.isHealthy())
        assertTrue(status.isDegraded())
        assertFalse(status.isUnhealthy())
        assertEquals("DEGRADED", status.status)
        assertTrue(status.affectedComponents.contains("api_service"))
    }

    @Test
    fun `unhealthy status is correctly identified`() {
        val status = IntegrationHealthStatus.Unhealthy(
            affectedComponents = listOf("api_service"),
            message = "API request failed",
            errorCause = "HTTP 500"
        )

        assertFalse(status.isHealthy())
        assertFalse(status.isDegraded())
        assertTrue(status.isUnhealthy())
        assertEquals("UNHEALTHY", status.status)
    }

    @Test
    fun `circuit open status is correctly identified`() {
        val status = IntegrationHealthStatus.CircuitOpen(
            service = "api_service",
            failureCount = 5,
            openSince = Date()
        )

        assertFalse(status.isHealthy())
        assertFalse(status.isDegraded())
        assertTrue(status.isUnhealthy())
        assertEquals("CIRCUIT_OPEN", status.status)
        assertEquals(5, status.failureCount)
    }

    @Test
    fun `rate limited status is correctly identified`() {
        val status = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/v1/users",
            requestCount = 65,
            limitExceededAt = Date()
        )

        assertFalse(status.isHealthy())
        assertFalse(status.isDegraded())
        assertTrue(status.isUnhealthy())
        assertEquals("RATE_LIMITED", status.status)
        assertEquals(65, status.requestCount)
    }
}

@RunWith(MockitoJUnitRunner::class)
class IntegrationHealthTrackerTest {

    private lateinit var tracker: IntegrationHealthTracker

    @Before
    fun setup() {
        tracker = IntegrationHealthTracker()
    }

    @Test
    fun `tracker records successful requests`() {
        tracker.recordRequest(150, true)

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.requestMetrics.successfulRequests)
        assertEquals(0, metrics.requestMetrics.failedRequests)
    }

    @Test
    fun `tracker records failed requests`() {
        tracker.recordRequest(200, false)

        val metrics = tracker.generateMetrics()
        assertEquals(0, metrics.requestMetrics.successfulRequests)
        assertEquals(1, metrics.requestMetrics.failedRequests)
    }

    @Test
    fun `tracker records retries`() {
        tracker.recordRetry()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.requestMetrics.retriedRequests)
    }

    @Test
    fun `tracker records timeout errors`() {
        tracker.recordTimeoutError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.timeoutErrors)
    }

    @Test
    fun `tracker records connection errors`() {
        tracker.recordConnectionError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.connectionErrors)
    }

    @Test
    fun `tracker records circuit breaker errors`() {
        tracker.recordCircuitBreakerError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.circuitBreakerErrors)
    }

    @Test
    fun `tracker records rate limit errors`() {
        tracker.recordRateLimitError()

        val metrics = tracker.generateMetrics()
        assertEquals(1, metrics.errorMetrics.rateLimitErrors)
    }

    @Test
    fun `tracker records HTTP errors`() {
        tracker.recordHttpError(500)
        tracker.recordHttpError(500)
        tracker.recordHttpError(404)

        val metrics = tracker.generateMetrics()
        assertEquals(2, metrics.errorMetrics.httpErrors[500])
        assertEquals(1, metrics.errorMetrics.httpErrors[404])
    }

    @Test
    fun `tracker calculates correct average response time`() = runTest {
        tracker.recordRequest(100, true)
        tracker.recordRequest(200, true)
        tracker.recordRequest(300, true)

        val metrics = tracker.generateMetrics()
        assertEquals(200.0, metrics.requestMetrics.averageResponseTimeMs, 0.01)
    }

    @Test
    fun `tracker calculates correct min response time`() = runTest {
        tracker.recordRequest(100, true)
        tracker.recordRequest(200, true)
        tracker.recordRequest(300, true)

        val metrics = tracker.generateMetrics()
        assertEquals(100, metrics.requestMetrics.minResponseTimeMs)
    }

    @Test
    fun `tracker calculates correct max response time`() = runTest {
        tracker.recordRequest(100, true)
        tracker.recordRequest(200, true)
        tracker.recordRequest(300, true)

        val metrics = tracker.generateMetrics()
        assertEquals(300, metrics.requestMetrics.maxResponseTimeMs)
    }

    @Test
    fun `reset clears all tracking data`() {
        tracker.recordRequest(150, true)
        tracker.recordRetry()
        tracker.recordTimeoutError()

        tracker.reset()
        val metrics = tracker.generateMetrics()

        assertEquals(0, metrics.requestMetrics.totalRequests)
        assertEquals(0, metrics.errorMetrics.totalErrors)
    }

    @Test
    fun `metrics is healthy when no errors`() {
        tracker.recordRequest(150, true)

        val metrics = tracker.generateMetrics()
        assertTrue(metrics.isHealthy())
    }

    @Test
    fun `metrics is unhealthy when circuit breaker open`() {
        val metrics = IntegrationHealthMetrics(
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = CircuitBreakerState.OPEN,
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
                totalErrors = 5,
                timeoutErrors = 0,
                connectionErrors = 0,
                httpErrors = emptyMap(),
                circuitBreakerErrors = 5,
                rateLimitErrors = 0,
                unknownErrors = 0
            )
        )

        assertFalse(metrics.isHealthy())
    }
}
