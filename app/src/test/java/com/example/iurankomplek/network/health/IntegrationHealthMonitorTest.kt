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
