package com.example.iurankomplek.network

import com.example.iurankomplek.network.health.IntegrationHealthMonitor
import com.example.iurankomplek.network.model.HealthCheckRequest
import com.example.iurankomplek.network.model.HealthCheckResponse
import com.example.iurankomplek.network.model.ComponentHealth
import com.example.iurankomplek.network.model.HealthDiagnostics
import com.example.iurankomplek.network.model.RateLimitStats
import com.example.iurankomplek.network.model.HealthMetrics
import com.example.iurankomplek.network.resilience.CircuitBreakerState
import com.example.iurankomplek.BuildConfig
import java.util.Date

class HealthService(
    private val healthMonitor: IntegrationHealthMonitor = IntegrationHealthMonitor.getInstance()
) {
    private val appStartTime = System.currentTimeMillis()
    
    suspend fun getHealth(request: HealthCheckRequest): HealthCheckResponse {
        val healthReport = healthMonitor.getDetailedHealthReport()
        
        val components = buildComponentHealthMap(healthReport)
        val diagnostics = if (request.includeDiagnostics) {
            buildDiagnostics(healthReport)
        } else {
            null
        }
        
        val metrics = if (request.includeMetrics) {
            buildMetrics(healthReport)
        } else {
            null
        }
        
        return HealthCheckResponse(
            status = healthReport.overallStatus.status,
            version = BuildConfig.VERSION_NAME,
            uptimeMs = System.currentTimeMillis() - appStartTime,
            components = components,
            timestamp = System.currentTimeMillis(),
            diagnostics = diagnostics,
            metrics = metrics
        )
    }
    
    private fun buildComponentHealthMap(healthReport: IntegrationHealthMonitor.HealthReport): Map<String, ComponentHealth> {
        val components = mutableMapOf<String, ComponentHealth>()
        
        healthReport.componentHealth.forEach { (componentName, status) ->
            components[componentName] = ComponentHealth(
                status = status.getStatusValue(),
                healthy = status.isHealthy(),
                message = status.getMessage(),
                details = null
            )
        }
        
        return components
    }
    
    private fun buildDiagnostics(healthReport: IntegrationHealthMonitor.HealthReport): HealthDiagnostics {
        val circuitBreakerStats = healthReport.circuitBreakerStats
        val circuitBreakerState = (circuitBreakerStats["state"] as? CircuitBreakerState)?.name ?: "UNKNOWN"
        val circuitBreakerFailures = (circuitBreakerStats["failureCount"] as? Int) ?: 0
        
        val rateLimitStatsMap = mutableMapOf<String, RateLimitStats>()
        healthReport.rateLimiterStats.forEach { (endpoint, stats) ->
            rateLimitStatsMap[endpoint] = RateLimitStats(
                requestCount = stats.getRequestCount(),
                lastRequestTime = stats.getLastRequestTime()
            )
        }
        
        return HealthDiagnostics(
            circuitBreakerState = circuitBreakerState,
            circuitBreakerFailures = circuitBreakerFailures,
            rateLimitStats = rateLimitStatsMap
        )
    }
    
    private fun buildMetrics(healthReport: IntegrationHealthMonitor.HealthReport): HealthMetrics {
        val metrics = healthReport.metrics
        
        val totalRequests = metrics.requestMetrics.totalRequests
        val successCount = metrics.requestMetrics.successfulRequests
        val failureCount = metrics.requestMetrics.failedRequests
        val retriedRequests = metrics.requestMetrics.retriedRequests
        val avgResponseTime = metrics.requestMetrics.averageResponseTimeMs
        val timeoutCount = metrics.errorMetrics.timeoutErrors
        val rateLimitCount = metrics.errorMetrics.rateLimitErrors
        
        val successRate = if (totalRequests > 0) {
            (successCount.toDouble() / totalRequests.toDouble()) * 100.0
        } else {
            100.0
        }
        
        val errorRate = if (totalRequests > 0) {
            (failureCount.toDouble() / totalRequests.toDouble()) * 100.0
        } else {
            0.0
        }
        
        return HealthMetrics(
            healthScore = metrics.getHealthScore(),
            totalRequests = totalRequests,
            successRate = String.format("%.2f", successRate).toDouble(),
            averageResponseTimeMs = avgResponseTime,
            errorRate = String.format("%.2f", errorRate).toDouble(),
            timeoutCount = timeoutCount,
            rateLimitViolations = rateLimitCount
        )
    }
    
    companion object {
        @Volatile
        private var instance: HealthService? = null
        
        fun getInstance(): HealthService {
            return instance ?: synchronized(this) {
                instance ?: HealthService().also { instance = it }
            }
        }
    }
}
