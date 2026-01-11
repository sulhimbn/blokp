package com.example.iurankomplek.network.retry

import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay

class RetryBudgetTest {
    
    @Test
    fun `canRetry returns true for initial retry`() {
        val budget = RetryBudget()
        assertTrue(budget.canRetry(0, 0))
    }
    
    @Test
    fun `canRetry returns true when under max retries`() {
        val budget = RetryBudget()
        assertTrue(budget.canRetry(1, 1000))
        assertTrue(budget.canRetry(2, 2000))
    }
    
    @Test
    fun `canRetry returns false when at max retries`() {
        val budget = RetryBudget()
        assertFalse(budget.canRetry(3, 3000))
    }
    
    @Test
    fun `canRetry returns false when total duration exceeded`() {
        val budget = RetryBudget()
        val maxDuration = 90000L
        assertFalse(budget.canRetry(1, maxDuration))
    }
    
    @Test
    fun `recordRetry increments total retries`() {
        val budget = RetryBudget()
        budget.recordRetry(1000, true)
        
        val metrics = budget.getMetrics()
        assertEquals(1, metrics.totalRetries)
    }
    
    @Test
    fun `recordRetry increments successful retries on success`() {
        val budget = RetryBudget()
        budget.recordRetry(1000, true)
        budget.recordRetry(2000, true)
        
        val metrics = budget.getMetrics()
        assertEquals(2, metrics.successfulRetries)
    }
    
    @Test
    fun `recordRetry increments failed retries on failure`() {
        val budget = RetryBudget()
        budget.recordRetry(1000, false)
        budget.recordRetry(2000, false)
        
        val metrics = budget.getMetrics()
        assertEquals(2, metrics.failedRetries)
    }
    
    @Test
    fun `recordRetry tracks total retry duration`() {
        val budget = RetryBudget()
        budget.recordRetry(1000, true)
        budget.recordRetry(2000, false)
        budget.recordRetry(1500, true)
        
        val metrics = budget.getMetrics()
        assertEquals(4500L, metrics.totalRetryDurationMs)
    }
    
    @Test
    fun `recordRetry tracks max delay used`() {
        val budget = RetryBudget()
        budget.recordRetry(1000, true)
        budget.recordRetry(2000, false)
        budget.recordRetry(1500, true)
        
        val metrics = budget.getMetrics()
        assertEquals(2000L, metrics.maxDelayMs)
    }
    
    @Test
    fun `calculateDelay returns exponential backoff`() {
        val budget = RetryBudget()
        
        val delay1 = budget.calculateDelay(1)
        val delay2 = budget.calculateDelay(2)
        val delay3 = budget.calculateDelay(3)
        
        assertTrue(delay2 > delay1)
        assertTrue(delay3 > delay2)
    }
    
    @Test
    fun `calculateDelay includes jitter`() {
        val budget = RetryBudget()
        
        val delay1 = budget.calculateDelay(1)
        val delay2 = budget.calculateDelay(1)
        
        assertNotEquals(delay1, delay2)
    }
    
    @Test
    fun `calculateDelay respects max delay`() {
        val budget = RetryBudget(
            RetryConfig(
                maxDelayMs = 1000
            )
        )
        
        val delay = budget.calculateDelay(10)
        assertTrue(delay <= 1000)
    }
    
    @Test
    fun `getMetrics calculates average delay correctly`() {
        val budget = RetryBudget()
        budget.recordRetry(1000, true)
        budget.recordRetry(2000, false)
        budget.recordRetry(3000, true)
        
        val metrics = budget.getMetrics()
        assertEquals(2000L, metrics.avgDelayMs)
    }
    
    @Test
    fun `getMetrics returns zero when no retries recorded`() {
        val budget = RetryBudget()
        
        val metrics = budget.getMetrics()
        assertEquals(0, metrics.totalRetries)
        assertEquals(0, metrics.successfulRetries)
        assertEquals(0, metrics.failedRetries)
        assertEquals(0L, metrics.totalRetryDurationMs)
        assertEquals(0L, metrics.avgDelayMs)
        assertEquals(0L, metrics.maxDelayMs)
    }
    
    @Test
    fun `reset clears all metrics`() {
        val budget = RetryBudget()
        budget.recordRetry(1000, true)
        budget.recordRetry(2000, false)
        
        budget.reset()
        
        val metrics = budget.getMetrics()
        assertEquals(0, metrics.totalRetries)
        assertEquals(0L, metrics.totalRetryDurationMs)
    }
    
    @Test
    fun `RetryBudgetExhaustedException has correct message`() {
        val exception = RetryBudgetExhaustedException(
            totalDurationMs = 90000L,
            maxDurationMs = 90000L
        )
        
        assertEquals("Retry budget exhausted after 90000ms (max: 90000ms)", exception.message)
    }
    
    @Test
    fun `custom RetryConfig is respected`() {
        val customConfig = RetryConfig(
            maxRetries = 5,
            maxDelayMs = 5000,
            maxTotalRetryDurationMs = 120000L
        )
        
        val budget = RetryBudget(customConfig)
        
        assertTrue(budget.canRetry(4, 50000))
        assertFalse(budget.canRetry(5, 50000))
        assertFalse(budget.canRetry(2, 120000))
        
        val delay = budget.calculateDelay(10)
        assertTrue(delay <= 5000)
    }
    
    @Test
    fun `exponential backoff formula is correct`() {
        val budget = RetryBudget(
            RetryConfig(
                initialDelayMs = 1000,
                backoffMultiplier = 2.0
            )
        )
        
        val delay1 = budget.calculateDelay(1)
        val delay2 = budget.calculateDelay(2)
        val delay3 = budget.calculateDelay(3)
        
        assertEquals(1000L, delay1, 500.0)
        assertEquals(2000L, delay2, 500.0)
        assertEquals(4000L, delay3, 500.0)
    }
}
