package com.example.iurankomplek.network.timeout

import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

enum class TimeoutProfile {
    FAST,
    NORMAL,
    SLOW
}

data class TimeoutConfig(
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val writeTimeoutMs: Long,
    val totalTimeoutMs: Long
)

data class TimeoutMetrics(
    val endpoint: String,
    val timeoutMs: Long,
    val timedOut: Boolean,
    val executionTimeMs: Long
)

object TimeoutManager {
    private val metrics = mutableListOf<TimeoutMetrics>()
    
    fun getTimeoutConfig(endpoint: String): TimeoutConfig {
        val profile = getProfileForEndpoint(endpoint)
        return when (profile) {
            TimeoutProfile.FAST -> TimeoutConfig(
                connectTimeoutMs = Constants.Network.FAST_TIMEOUT_MS,
                readTimeoutMs = Constants.Network.FAST_TIMEOUT_MS,
                writeTimeoutMs = Constants.Network.FAST_TIMEOUT_MS,
                totalTimeoutMs = Constants.Network.FAST_TIMEOUT_MS
            )
            TimeoutProfile.NORMAL -> TimeoutConfig(
                connectTimeoutMs = Constants.Network.NORMAL_TIMEOUT_MS,
                readTimeoutMs = Constants.Network.NORMAL_TIMEOUT_MS,
                writeTimeoutMs = Constants.Network.NORMAL_TIMEOUT_MS,
                totalTimeoutMs = Constants.Network.NORMAL_TIMEOUT_MS
            )
            TimeoutProfile.SLOW -> TimeoutConfig(
                connectTimeoutMs = Constants.Network.SLOW_TIMEOUT_MS,
                readTimeoutMs = Constants.Network.SLOW_TIMEOUT_MS,
                writeTimeoutMs = Constants.Network.SLOW_TIMEOUT_MS,
                totalTimeoutMs = Constants.Network.SLOW_TIMEOUT_MS
            )
        }
    }
    
    fun getProfileForEndpoint(endpoint: String): TimeoutProfile {
        return when {
            endpoint.contains("/health") -> TimeoutProfile.FAST
            endpoint.contains("/status") -> TimeoutProfile.FAST
            endpoint.contains("/payments/initiate") -> TimeoutProfile.SLOW
            endpoint.contains("/payments/") && endpoint.contains("/confirm") -> TimeoutProfile.SLOW
            endpoint.contains("/payments") -> TimeoutProfile.NORMAL
            endpoint.contains("/vendors") -> TimeoutProfile.NORMAL
            endpoint.contains("/work-orders") -> TimeoutProfile.NORMAL
            endpoint.contains("/announcements") -> TimeoutProfile.NORMAL
            endpoint.contains("/messages") -> TimeoutProfile.NORMAL
            endpoint.contains("/community-posts") -> TimeoutProfile.NORMAL
            endpoint.contains("/users") -> TimeoutProfile.NORMAL
            endpoint.contains("/pemanfaatan") -> TimeoutProfile.NORMAL
            else -> TimeoutProfile.NORMAL
        }
    }
    
    suspend fun <T> withTimeout(
        endpoint: String,
        block: suspend () -> T
    ): TimeoutResult<T> {
        val config = getTimeoutConfig(endpoint)
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = withTimeout(config.totalTimeoutMs) {
                block()
            }
            val executionTime = System.currentTimeMillis() - startTime
            recordMetric(endpoint, config.totalTimeoutMs, false, executionTime)
            TimeoutResult.Success(result)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            val executionTime = System.currentTimeMillis() - startTime
            recordMetric(endpoint, config.totalTimeoutMs, true, executionTime)
            TimeoutResult.Timeout(config.totalTimeoutMs)
        }
    }
    
    suspend fun <T> withTimeoutOrNull(
        endpoint: String,
        block: suspend () -> T
    ): T? {
        val config = getTimeoutConfig(endpoint)
        val startTime = System.currentTimeMillis()
        
        return withTimeoutOrNull(config.totalTimeoutMs) {
            val result = block()
            val executionTime = System.currentTimeMillis() - startTime
            recordMetric(endpoint, config.totalTimeoutMs, false, executionTime)
            result
        }?.also {
            val executionTime = System.currentTimeMillis() - startTime
            recordMetric(endpoint, config.totalTimeoutMs, true, executionTime)
        }
    }
    
    private fun recordMetric(endpoint: String, timeoutMs: Long, timedOut: Boolean, executionTimeMs: Long) {
        synchronized(metrics) {
            metrics.add(TimeoutMetrics(endpoint, timeoutMs, timedOut, executionTimeMs))
            if (metrics.size > 1000) {
                metrics.removeAt(0)
            }
        }
    }
    
    fun getMetrics(): List<TimeoutMetrics> = synchronized(metrics) { metrics.toList() }
    
    fun clearMetrics() = synchronized(metrics) { metrics.clear() }
    
    fun getTimeoutStats(endpoint: String? = null): TimeoutStats {
        val relevantMetrics = endpoint?.let { ep ->
            metrics.filter { it.endpoint == ep }
        } ?: metrics
        
        return TimeoutStats(
            totalCalls = relevantMetrics.size,
            timeouts = relevantMetrics.count { it.timedOut },
            avgExecutionTimeMs = relevantMetrics.map { it.executionTimeMs }.average().toLong(),
            maxExecutionTimeMs = relevantMetrics.maxOfOrNull { it.executionTimeMs } ?: 0L
        )
    }
}

sealed class TimeoutResult<out T> {
    data class Success<T>(val value: T) : TimeoutResult<T>()
    data class Timeout(val timeoutMs: Long) : TimeoutResult<Nothing>()
}

data class TimeoutStats(
    val totalCalls: Int,
    val timeouts: Int,
    val avgExecutionTimeMs: Long,
    val maxExecutionTimeMs: Long
) {
    val timeoutRate: Double get() = if (totalCalls > 0) timeouts.toDouble() / totalCalls else 0.0
}
