package com.example.iurankomplek.network.retry

import com.example.iurankomplek.utils.Constants
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.pow

data class RetryConfig(
    val maxRetries: Int = Constants.Network.MAX_RETRIES,
    val initialDelayMs: Long = Constants.Network.INITIAL_RETRY_DELAY_MS,
    val maxDelayMs: Long = Constants.Network.MAX_RETRY_DELAY_MS,
    val maxTotalRetryDurationMs: Long = Constants.Network.MAX_RETRY_DELAY_MS * 3,
    val backoffMultiplier: Double = 2.0,
    val jitterMs: Long = 500L
)

data class RetryMetrics(
    val totalRetries: Int,
    val successfulRetries: Int,
    val failedRetries: Int,
    val totalRetryDurationMs: Long,
    val avgDelayMs: Long,
    val maxDelayMs: Long
)

class RetryBudget(
    private val config: RetryConfig = RetryConfig()
) {
    private val totalRetries = AtomicInteger(0)
    private val successfulRetries = AtomicInteger(0)
    private val failedRetries = AtomicInteger(0)
    private val totalRetryDuration = AtomicLong(0)
    private val maxDelayUsed = AtomicLong(0)
    private val totalDelaySum = AtomicLong(0)
    
    fun canRetry(currentRetry: Int, totalElapsedMs: Long): Boolean {
        if (currentRetry >= config.maxRetries) return false
        if (totalElapsedMs >= config.maxTotalRetryDurationMs) return false
        return true
    }
    
    fun recordRetry(delayMs: Long, success: Boolean) {
        totalRetries.incrementAndGet()
        totalRetryDuration.addAndGet(delayMs)
        totalDelaySum.addAndGet(delayMs)
        
        if (delayMs > maxDelayUsed.get()) {
            maxDelayUsed.set(delayMs)
        }
        
        if (success) {
            successfulRetries.incrementAndGet()
        } else {
            failedRetries.incrementAndGet()
        }
    }
    
    fun calculateDelay(currentRetry: Int): Long {
        val exponentialDelay = (config.initialDelayMs * config.backoffMultiplier.pow((currentRetry - 1).toDouble())).toLong()
        val jitter = (kotlin.random.Random.nextDouble() * config.jitterMs).toLong()
        return minOf(exponentialDelay + jitter, config.maxDelayMs)
    }
    
    fun getMetrics(): RetryMetrics {
        return RetryMetrics(
            totalRetries = totalRetries.get(),
            successfulRetries = successfulRetries.get(),
            failedRetries = failedRetries.get(),
            totalRetryDurationMs = totalRetryDuration.get(),
            avgDelayMs = if (totalRetries.get() > 0) totalDelaySum.get() / totalRetries.get() else 0L,
            maxDelayMs = maxDelayUsed.get()
        )
    }
    
    fun reset() {
        totalRetries.set(0)
        successfulRetries.set(0)
        failedRetries.set(0)
        totalRetryDuration.set(0)
        maxDelayUsed.set(0)
        totalDelaySum.set(0)
    }
}

class RetryBudgetExhaustedException(
    val totalDurationMs: Long,
    val maxDurationMs: Long
) : Exception("Retry budget exhausted after ${totalDurationMs}ms (max: ${maxDurationMs}ms)")
