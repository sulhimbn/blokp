package com.example.iurankomplek.network.health

import com.example.iurankomplek.network.resilience.CircuitBreakerState
import com.example.iurankomplek.utils.Constants
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

data class IntegrationHealthMetrics(
    val timestamp: Date = Date(),
    val circuitBreakerMetrics: CircuitBreakerMetrics,
    val rateLimiterMetrics: RateLimiterMetrics,
    val requestMetrics: RequestMetrics,
    val errorMetrics: ErrorMetrics
) {
    data class CircuitBreakerMetrics(
        val state: CircuitBreakerState,
        val failureCount: Int,
        val successCount: Int,
        val lastFailureTime: Date?,
        val lastSuccessTime: Date?,
        val lastStateChange: Date?
    )

    data class RateLimiterMetrics(
        val totalRequests: Int,
        val requestsInLastMinute: Int,
        val requestsInLastSecond: Int,
        val rateLimitExceededCount: Int,
        val perEndpointStats: Map<String, EndpointStats>
    ) {
        data class EndpointStats(
            val requestCount: Int,
            val lastRequestTime: Date?,
            val rateLimitExceeded: Boolean
        )
    }

    data class RequestMetrics(
        val totalRequests: Int,
        val successfulRequests: Int,
        val failedRequests: Int,
        val retriedRequests: Int,
        val averageResponseTimeMs: Double,
        val minResponseTimeMs: Long,
        val maxResponseTimeMs: Long,
        val p95ResponseTimeMs: Long,
        val p99ResponseTimeMs: Long
    )

    data class ErrorMetrics(
        val totalErrors: Int,
        val timeoutErrors: Int,
        val connectionErrors: Int,
        val httpErrors: Map<Int, Int>,
        val circuitBreakerErrors: Int,
        val rateLimitErrors: Int,
        val unknownErrors: Int
    )

    fun isHealthy(): Boolean {
        return circuitBreakerMetrics.state != CircuitBreakerState.OPEN &&
               rateLimiterMetrics.rateLimitExceededCount == 0 &&
               errorMetrics.circuitBreakerErrors == 0
    }

    fun getHealthScore(): Double {
        var score = 100.0

        score -= when (circuitBreakerMetrics.state) {
            CircuitBreakerState.OPEN -> 50.0
            CircuitBreakerState.HALF_OPEN -> 25.0
            CircuitBreakerState.CLOSED -> 0.0
        }

        score -= (rateLimiterMetrics.rateLimitExceededCount * 10.0).coerceAtMost(30.0)

        score -= (errorMetrics.circuitBreakerErrors * 15.0).coerceAtMost(45.0)

        val failureRate = requestMetrics.failedRequests.toDouble() /
                        requestMetrics.totalRequests.toDouble().coerceAtLeast(1.0)
        score -= (failureRate * 50.0).coerceAtMost(40.0)

        return score.coerceAtLeast(0.0)
    }
}

class IntegrationHealthTracker {
    private val totalRequests = AtomicInteger(0)
    private val successfulRequests = AtomicInteger(0)
    private val failedRequests = AtomicInteger(0)
    private val retriedRequests = AtomicInteger(0)

    private val timeoutErrors = AtomicInteger(0)
    private val connectionErrors = AtomicInteger(0)
    private val circuitBreakerErrors = AtomicInteger(0)
    private val rateLimitErrors = AtomicInteger(0)
    private val unknownErrors = AtomicInteger(0)

    private val httpErrorCounts = ConcurrentHashMap<Int, AtomicInteger>()

    private val responseTimes = ConcurrentHashMap<String, MutableList<Long>>()

    private val lastSuccessfulRequest = AtomicLong(0)
    private val lastFailedRequest = AtomicLong(0)

    fun recordRequest(responseTimeMs: Long, success: Boolean) {
        totalRequests.incrementAndGet()
        if (success) {
            successfulRequests.incrementAndGet()
            lastSuccessfulRequest.set(System.currentTimeMillis())
        } else {
            failedRequests.incrementAndGet()
            lastFailedRequest.set(System.currentTimeMillis())
        }

        val key = "response_times"
        synchronized(responseTimes) {
            val times = responseTimes.getOrPut(key) { mutableListOf() }
            times.add(responseTimeMs)
            if (times.size > 1000) {
                times.removeAt(0)
            }
        }
    }

    fun recordRetry() {
        retriedRequests.incrementAndGet()
    }

    fun recordTimeoutError() {
        timeoutErrors.incrementAndGet()
    }

    fun recordConnectionError() {
        connectionErrors.incrementAndGet()
    }

    fun recordCircuitBreakerError() {
        circuitBreakerErrors.incrementAndGet()
    }

    fun recordRateLimitError() {
        rateLimitErrors.incrementAndGet()
    }

    fun recordHttpError(httpCode: Int) {
        val counter = httpErrorCounts.getOrPut(httpCode) { AtomicInteger(0) }
        counter.incrementAndGet()
    }

    fun recordUnknownError() {
        unknownErrors.incrementAndGet()
    }

    fun generateMetrics(): IntegrationHealthMetrics {
        val allResponseTimes = responseTimes.values.flatten()
        val sortedTimes = allResponseTimes.sorted()

        val requestMetrics = IntegrationHealthMetrics.RequestMetrics(
            totalRequests = totalRequests.get(),
            successfulRequests = successfulRequests.get(),
            failedRequests = failedRequests.get(),
            retriedRequests = retriedRequests.get(),
            averageResponseTimeMs = if (allResponseTimes.isNotEmpty()) {
                allResponseTimes.average()
            } else 0.0,
            minResponseTimeMs = sortedTimes.firstOrNull() ?: 0,
            maxResponseTimeMs = sortedTimes.lastOrNull() ?: 0,
            p95ResponseTimeMs = if (sortedTimes.isNotEmpty()) {
                sortedTimes[(sortedTimes.size * 0.95).toInt().coerceAtMost(sortedTimes.size - 1)]
            } else 0,
            p99ResponseTimeMs = if (sortedTimes.isNotEmpty()) {
                sortedTimes[(sortedTimes.size * 0.99).toInt().coerceAtMost(sortedTimes.size - 1)]
            } else 0
        )

        val httpErrorsMap = httpErrorCounts.mapValues { it.value.get() }.toMap()

        val errorMetrics = IntegrationHealthMetrics.ErrorMetrics(
            totalErrors = failedRequests.get(),
            timeoutErrors = timeoutErrors.get(),
            connectionErrors = connectionErrors.get(),
            httpErrors = httpErrorsMap,
            circuitBreakerErrors = circuitBreakerErrors.get(),
            rateLimitErrors = rateLimitErrors.get(),
            unknownErrors = unknownErrors.get()
        )

        return IntegrationHealthMetrics(
            timestamp = Date(),
            circuitBreakerMetrics = IntegrationHealthMetrics.CircuitBreakerMetrics(
                state = com.example.iurankomplek.network.ApiConfig.getCircuitBreakerState(),
                failureCount = 0,
                successCount = 0,
                lastFailureTime = null,
                lastSuccessTime = null,
                lastStateChange = null
            ),
            rateLimiterMetrics = IntegrationHealthMetrics.RateLimiterMetrics(
                totalRequests = totalRequests.get(),
                requestsInLastMinute = 0,
                requestsInLastSecond = 0,
                rateLimitExceededCount = rateLimitErrors.get(),
                perEndpointStats = emptyMap()
            ),
            requestMetrics = requestMetrics,
            errorMetrics = errorMetrics
        )
    }

    fun reset() {
        totalRequests.set(0)
        successfulRequests.set(0)
        failedRequests.set(0)
        retriedRequests.set(0)
        timeoutErrors.set(0)
        connectionErrors.set(0)
        circuitBreakerErrors.set(0)
        rateLimitErrors.set(0)
        unknownErrors.set(0)
        httpErrorCounts.clear()
        responseTimes.clear()
        lastSuccessfulRequest.set(0)
        lastFailedRequest.set(0)
    }
}
