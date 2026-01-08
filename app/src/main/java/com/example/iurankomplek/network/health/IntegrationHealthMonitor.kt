package com.example.iurankomplek.network.health

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.interceptor.RateLimiterInterceptor
import com.example.iurankomplek.network.resilience.CircuitBreakerState
import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class IntegrationHealthMonitor(
    private val circuitBreaker: com.example.iurankomplek.network.resilience.CircuitBreaker =
        ApiConfig.circuitBreaker,
    private val rateLimiter: RateLimiterInterceptor = ApiConfig.rateLimiter
) {
    private val mutex = Mutex()
    private val componentHealth = ConcurrentHashMap<String, IntegrationHealthStatus>()

    private val tracker = IntegrationHealthTracker()
    private val lastHealthCheck = AtomicLong(0)
    private val healthCheckInterval = Constants.Network.ONE_MINUTE_MS

    private val circuitBreakerFailures = AtomicInteger(0)
    private val rateLimitViolations = AtomicInteger(0)

    init {
        initializeComponentHealth()
    }

    private fun initializeComponentHealth() {
        componentHealth["circuit_breaker"] = IntegrationHealthStatus.Healthy()
        componentHealth["rate_limiter"] = IntegrationHealthStatus.Healthy()
        componentHealth["api_service"] = IntegrationHealthStatus.Healthy()
        componentHealth["network"] = IntegrationHealthStatus.Healthy()
    }

    suspend fun recordRequest(
        endpoint: String,
        responseTimeMs: Long,
        success: Boolean,
        httpCode: Int? = null
    ) {
        tracker.recordRequest(responseTimeMs, success)

        mutex.withLock {
            updateComponentHealthFromRequest(endpoint, success, httpCode)
        }
    }

    suspend fun recordRetry(endpoint: String) {
        tracker.recordRetry()

        mutex.withLock {
            val currentHealth = componentHealth["api_service"]
            if (currentHealth is IntegrationHealthStatus.Healthy) {
                componentHealth["api_service"] = IntegrationHealthStatus.Degraded(
                    affectedComponents = listOf("api_service"),
                    message = "Retry detected for endpoint: $endpoint",
                    lastSuccessfulRequest = Date()
                )
            }
        }
    }

    suspend fun recordCircuitBreakerOpen(service: String) {
        circuitBreakerFailures.incrementAndGet()
        tracker.recordCircuitBreakerError()

        mutex.withLock {
            componentHealth[service] = IntegrationHealthStatus.CircuitOpen(
                service = service,
                failureCount = circuitBreakerFailures.get(),
                openSince = Date()
            )
        }
    }

    suspend fun recordRateLimitExceeded(endpoint: String, requestCount: Int) {
        rateLimitViolations.incrementAndGet()
        tracker.recordRateLimitError()

        mutex.withLock {
            componentHealth["rate_limiter"] = IntegrationHealthStatus.RateLimited(
                endpoint = endpoint,
                requestCount = requestCount,
                limitExceededAt = Date()
            )
        }
    }

    suspend fun checkCircuitBreakerHealth() {
        val state = ApiConfig.getCircuitBreakerState()
        val isHealthy = state != CircuitBreakerState.OPEN

        mutex.withLock {
            if (!isHealthy && componentHealth["circuit_breaker"] !is IntegrationHealthStatus.CircuitOpen) {
                componentHealth["circuit_breaker"] = IntegrationHealthStatus.CircuitOpen(
                    service = "default",
                    failureCount = circuitBreakerFailures.get(),
                    openSince = Date()
                )
            } else if (isHealthy && componentHealth["circuit_breaker"] !is IntegrationHealthStatus.Healthy) {
                componentHealth["circuit_breaker"] = IntegrationHealthStatus.Healthy(
                    message = "Circuit breaker recovered from previous failure"
                )
            }
        }
    }

    suspend fun checkRateLimiterHealth() {
        val stats = ApiConfig.getRateLimiterStats()
        val hasViolations = stats.values.any { it.getRequestCount() >= Constants.Network.MAX_REQUESTS_PER_MINUTE }

        mutex.withLock {
            if (hasViolations && componentHealth["rate_limiter"] !is IntegrationHealthStatus.RateLimited) {
                componentHealth["rate_limiter"] = IntegrationHealthStatus.RateLimited(
                    endpoint = "global",
                    requestCount = stats.values.sumOf { it.getRequestCount() },
                    limitExceededAt = Date()
                )
            } else if (!hasViolations && componentHealth["rate_limiter"] !is IntegrationHealthStatus.Healthy) {
                componentHealth["rate_limiter"] = IntegrationHealthStatus.Healthy(
                    message = "Rate limiter within normal limits"
                )
            }
        }
    }

    suspend fun getCurrentHealth(): IntegrationHealthStatus {
        val metrics = tracker.generateMetrics()

        val unhealthyComponents = componentHealth.filterValues { it.isUnhealthy() }.keys.toList()
        val degradedComponents = componentHealth.filterValues { it.isDegraded() }.keys.toList()

        return when {
            unhealthyComponents.isNotEmpty() -> {
                IntegrationHealthStatus.Unhealthy(
                    affectedComponents = unhealthyComponents,
                    message = "Integration system unhealthy: ${unhealthyComponents.size} component(s) failed",
                    errorCause = "Health check failed at ${Date()}"
                )
            }
            degradedComponents.isNotEmpty() -> {
                IntegrationHealthStatus.Degraded(
                    affectedComponents = degradedComponents,
                    message = "Integration system degraded: ${degradedComponents.size} component(s) degraded",
                    lastSuccessfulRequest = if (lastSuccessfulRequest.get() > 0) Date(lastSuccessfulRequest.get()) else null
                )
            }
            else -> {
                IntegrationHealthStatus.Healthy(
                    message = "All integration systems operational (health score: ${String.format("%.2f", metrics.getHealthScore())}%)"
                )
            }
        }
    }

    suspend fun getDetailedHealthReport(): HealthReport {
        val currentHealth = getCurrentHealth()
        val metrics = tracker.generateMetrics()
        val circuitBreakerStats = ApiConfig.getCircuitBreakerStats()
        val rateLimiterStats = ApiConfig.getRateLimiterStats()

        return HealthReport(
            timestamp = Date(),
            overallStatus = currentHealth,
            healthScore = metrics.getHealthScore(),
            metrics = metrics,
            circuitBreakerStats = circuitBreakerStats,
            rateLimiterStats = rateLimiterStats,
            componentHealth = componentHealth.toMap(),
            recommendations = generateRecommendations(currentHealth, metrics)
        )
    }

    private fun updateComponentHealthFromRequest(
        endpoint: String,
        success: Boolean,
        httpCode: Int?
    ) {
        if (success) {
            if (componentHealth["api_service"] !is IntegrationHealthStatus.Healthy) {
                componentHealth["api_service"] = IntegrationHealthStatus.Healthy(
                    message = "API service recovered successfully"
                )
            }
        } else {
            when {
                httpCode == 408 || httpCode == 504 -> {
                    tracker.recordTimeoutError()
                    componentHealth["api_service"] = IntegrationHealthStatus.Degraded(
                        affectedComponents = listOf("api_service"),
                        message = "Timeout detected for endpoint: $endpoint",
                        lastSuccessfulRequest = if (lastSuccessfulRequest.get() > 0) Date(lastSuccessfulRequest.get()) else null
                    )
                }
                httpCode == 429 -> {
                    tracker.recordRateLimitError()
                    val stats = ApiConfig.getRateLimiterStats()
                    componentHealth["rate_limiter"] = IntegrationHealthStatus.RateLimited(
                        endpoint = endpoint,
                        requestCount = stats.values.sumOf { it.getRequestCount() },
                        limitExceededAt = Date()
                    )
                }
                else -> {
                    componentHealth["api_service"] = IntegrationHealthStatus.Unhealthy(
                        affectedComponents = listOf("api_service"),
                        message = "API request failed for endpoint: $endpoint",
                        errorCause = "HTTP $httpCode"
                    )
                }
            }
        }
    }

    private fun generateRecommendations(
        health: IntegrationHealthStatus,
        metrics: IntegrationHealthMetrics
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when (health) {
            is IntegrationHealthStatus.CircuitOpen -> {
                recommendations.add("Circuit breaker is open for service: ${health.service}")
                recommendations.add("Check external service availability")
                recommendations.add("Review failure logs for root cause")
                recommendations.add("Consider increasing circuit breaker timeout threshold")
            }
            is IntegrationHealthStatus.RateLimited -> {
                recommendations.add("Rate limit exceeded for endpoint: ${health.endpoint}")
                recommendations.add("Implement request batching to reduce API calls")
                recommendations.add("Add client-side caching for frequently accessed data")
                recommendations.add("Review rate limiter configuration")
            }
            is IntegrationHealthStatus.Unhealthy -> {
                recommendations.add("System is unhealthy: ${health.errorCause}")
                recommendations.add("Check network connectivity")
                recommendations.add("Verify API endpoint availability")
                recommendations.add("Review error logs for specific failures")
            }
            is IntegrationHealthStatus.Degraded -> {
                recommendations.add("System performance is degraded")
                recommendations.add("Monitor response times: ${metrics.requestMetrics.averageResponseTimeMs}ms average")
                recommendations.add("Review retry rate: ${metrics.requestMetrics.retriedRequests} retried requests")
                recommendations.add("Check resource utilization")
            }
            is IntegrationHealthStatus.Healthy -> {
                recommendations.add("System is healthy and operational")
                recommendations.add("Continue monitoring for anomalies")
                recommendations.add("Review metrics periodically for optimization opportunities")
            }
        }

        return recommendations
    }

    fun reset() {
        tracker.reset()
        circuitBreakerFailures.set(0)
        rateLimitViolations.set(0)
        initializeComponentHealth()
        lastHealthCheck.set(0)
    }

    data class HealthReport(
        val timestamp: Date,
        val overallStatus: IntegrationHealthStatus,
        val healthScore: Double,
        val metrics: IntegrationHealthMetrics,
        val circuitBreakerStats: Map<String, Any>,
        val rateLimiterStats: Map<String, RateLimiterInterceptor.EndpointStats>,
        val componentHealth: Map<String, IntegrationHealthStatus>,
        val recommendations: List<String>
    )

    companion object {
        @Volatile
        private var instance: IntegrationHealthMonitor? = null

        fun getInstance(): IntegrationHealthMonitor {
            return instance ?: synchronized(this) {
                instance ?: IntegrationHealthMonitor().also { instance = it }
            }
        }
    }
}
