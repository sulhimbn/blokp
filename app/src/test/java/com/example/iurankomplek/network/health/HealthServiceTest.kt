package com.example.iurankomplek.network.health

import com.example.iurankomplek.network.HealthService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.time.Duration

class HealthServiceTest {
    
    private lateinit var healthService: HealthService
    private lateinit var mockHealthMonitor: IntegrationHealthMonitor
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        healthService = HealthService(mockHealthMonitor)
    }
    
    @Test
    fun `getHealth with basic request returns healthy status`() = runTest {
        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(createHealthyReport())
        
        val request = HealthCheckRequest(
            includeDiagnostics = false,
            includeMetrics = false
        )
        
        val response = healthService.getHealth(request)
        
        assertEquals("HEALTHY", response.status)
        assertNotNull(response.version)
        assertTrue(response.uptimeMs > 0)
        assertNotNull(response.components)
        assertEquals(4, response.components.size)
        assertFalse(response.components["circuit_breaker"]!!.healthy)
        assertTrue(response.components["circuit_breaker"]!!.healthy)
        assertNull(response.diagnostics)
        assertNull(response.metrics)
    }
    
    @Test
    fun `getHealth with diagnostics returns detailed diagnostics`() = runTest {
        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(createHealthyReport())
        
        val request = HealthCheckRequest(
            includeDiagnostics = true,
            includeMetrics = false
        )
        
        val response = healthService.getHealth(request)
        
        assertNotNull(response.diagnostics)
        assertEquals("CLOSED", response.diagnostics?.circuitBreakerState)
        assertEquals(0, response.diagnostics?.circuitBreakerFailures)
        assertTrue(response.diagnostics?.rateLimitStats?.isNotEmpty() ?: false)
    }
    
    @Test
    fun `getHealth with metrics returns performance metrics`() = runTest {
        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(createHealthyReport())
        
        val request = HealthCheckRequest(
            includeDiagnostics = false,
            includeMetrics = true
        )
        
        val response = healthService.getHealth(request)
        
        assertNotNull(response.metrics)
        assertTrue(response.metrics?.healthScore ?: 0.0 > 0.0)
        assertTrue(response.metrics?.healthScore ?: 0.0 <= 100.0)
        assertTrue(response.metrics?.totalRequests ?: 0 >= 0)
        assertTrue(response.metrics?.successRate ?: 0.0 >= 0.0)
        assertTrue(response.metrics?.successRate ?: 0.0 <= 100.0)
    }
    
    @Test
    fun `getHealth with degraded status returns degraded response`() = runTest {
        val degradedReport = createHealthyReport().copy(
            overallStatus = IntegrationHealthStatus.Degraded(
                affectedComponents = listOf("api_service"),
                message = "API service degraded",
                lastSuccessfulRequest = System.currentTimeMillis()
            )
        )
        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(degradedReport)
        
        val request = HealthCheckRequest(includeDiagnostics = false, includeMetrics = false)
        val response = healthService.getHealth(request)
        
        assertEquals("DEGRADED", response.status)
        assertFalse(response.components["api_service"]!!.healthy)
    }
    
    @Test
    fun `getHealth with unhealthy status returns unhealthy response`() = runTest {
        val unhealthyReport = createHealthyReport().copy(
            overallStatus = IntegrationHealthStatus.Unhealthy(
                affectedComponents = listOf("api_service", "network"),
                message = "Integration system unhealthy",
                errorCause = "Network connection failed"
            )
        )
        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(unhealthyReport)
        
        val request = HealthCheckRequest(includeDiagnostics = false, includeMetrics = false)
        val response = healthService.getHealth(request)
        
        assertEquals("UNHEALTHY", response.status)
        assertFalse(response.components["api_service"]!!.healthy)
        assertFalse(response.components["network"]!!.healthy)
    }
    
    @Test
    fun `getHealth includes all components in response`() = runTest {
        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(createHealthyReport())
        
        val request = HealthCheckRequest(includeDiagnostics = true, includeMetrics = true)
        val response = healthService.getHealth(request)
        
        assertTrue(response.components.containsKey("circuit_breaker"))
        assertTrue(response.components.containsKey("rate_limiter"))
        assertTrue(response.components.containsKey("api_service"))
        assertTrue(response.components.containsKey("network"))
    }
    
    @Test
    fun `getHealth calculates uptime correctly`() = runTest {
        `when`(mockHealthMonitor.getDetailedHealthReport()).thenReturn(createHealthyReport())
        
        Thread.sleep(100)
        val request = HealthCheckRequest(includeDiagnostics = false, includeMetrics = false)
        val response = healthService.getHealth(request)
        
        assertTrue(response.uptimeMs >= 100)
        assertTrue(response.uptimeMs < 10000)
    }
    
    private fun createHealthyReport(): IntegrationHealthMonitor.HealthReport {
        val mockMetrics = mock(IntegrationHealthMetrics::class.java)
        `when`(mockMetrics.getHealthScore()).thenReturn(95.5)
        `when`(mockMetrics.requestMetrics).thenReturn(
            IntegrationHealthRequestMetrics(
                totalRequests = 100,
                successfulRequests = 95,
                failedRequests = 5,
                retriedRequests = 3,
                averageResponseTimeMs = 150.0
            )
        )
        `when`(mockMetrics.errorMetrics).thenReturn(
            IntegrationHealthErrorMetrics(
                timeoutErrors = 1,
                rateLimitErrors = 2,
                otherErrors = 2
            )
        )
        
        return IntegrationHealthMonitor.HealthReport(
            timestamp = System.currentTimeMillis(),
            overallStatus = IntegrationHealthStatus.Healthy(),
            healthScore = 95.5,
            metrics = mockMetrics,
            circuitBreakerStats = mapOf(
                "state" to com.example.iurankomplek.network.resilience.CircuitBreakerState.Closed,
                "failureCount" to 0,
                "successCount" to 95,
                "lastFailureTime" to 0L
            ),
            rateLimiterStats = mapOf(
                "GET:/api/v1/users" to createEndpointStats(),
                "GET:/api/v1/pemanfaatan" to createEndpointStats()
            ),
            componentHealth = mapOf(
                "circuit_breaker" to IntegrationHealthStatus.Healthy(),
                "rate_limiter" to IntegrationHealthStatus.Healthy(),
                "api_service" to IntegrationHealthStatus.Healthy(),
                "network" to IntegrationHealthStatus.Healthy()
            ),
            recommendations = listOf("System is healthy and operational")
        )
    }
    
    private fun createEndpointStats(): com.example.iurankomplek.network.interceptor.RateLimiterInterceptor.EndpointStats {
        val stats = mock(com.example.iurankomplek.network.interceptor.RateLimiterInterceptor.EndpointStats::class.java)
        `when`(stats.getRequestCount()).thenReturn(50)
        `when`(stats.getLastRequestTime()).thenReturn(System.currentTimeMillis() - 5000)
        return stats
    }
}

class IntegrationHealthRequestMetrics(
    val totalRequests: Int,
    val successfulRequests: Int,
    val failedRequests: Int,
    val retriedRequests: Int,
    val averageResponseTimeMs: Double
)

class IntegrationHealthErrorMetrics(
    val timeoutErrors: Int,
    val rateLimitErrors: Int,
    val otherErrors: Int
)
