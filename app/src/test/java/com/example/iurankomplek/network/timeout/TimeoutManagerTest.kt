package com.example.iurankomplek.network.timeout

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.delay

class TimeoutManagerTest {
    
    @Test
    fun `getProfileForEndpoint returns FAST for health endpoint`() {
        val profile = TimeoutManager.getProfileForEndpoint("/api/v1/health")
        assertEquals(TimeoutProfile.FAST, profile)
    }
    
    @Test
    fun `getProfileForEndpoint returns NORMAL for users endpoint`() {
        val profile = TimeoutManager.getProfileForEndpoint("/api/v1/users")
        assertEquals(TimeoutProfile.NORMAL, profile)
    }
    
    @Test
    fun `getProfileForEndpoint returns SLOW for payments initiate endpoint`() {
        val profile = TimeoutManager.getProfileForEndpoint("/api/v1/payments/initiate")
        assertEquals(TimeoutProfile.SLOW, profile)
    }
    
    @Test
    fun `getProfileForEndpoint returns NORMAL for unknown endpoint`() {
        val profile = TimeoutManager.getProfileForEndpoint("/api/v1/unknown")
        assertEquals(TimeoutProfile.NORMAL, profile)
    }
    
    @Test
    fun `getTimeoutConfig returns correct timeouts for FAST profile`() = runTest {
        val config = TimeoutManager.getTimeoutConfig("/api/v1/health")
        assertEquals(5000L, config.connectTimeoutMs)
        assertEquals(5000L, config.readTimeoutMs)
        assertEquals(5000L, config.writeTimeoutMs)
        assertEquals(5000L, config.totalTimeoutMs)
    }
    
    @Test
    fun `getTimeoutConfig returns correct timeouts for NORMAL profile`() = runTest {
        val config = TimeoutManager.getTimeoutConfig("/api/v1/users")
        assertEquals(30000L, config.connectTimeoutMs)
        assertEquals(30000L, config.readTimeoutMs)
        assertEquals(30000L, config.writeTimeoutMs)
        assertEquals(30000L, config.totalTimeoutMs)
    }
    
    @Test
    fun `getTimeoutConfig returns correct timeouts for SLOW profile`() = runTest {
        val config = TimeoutManager.getTimeoutConfig("/api/v1/payments/initiate")
        assertEquals(60000L, config.connectTimeoutMs)
        assertEquals(60000L, config.readTimeoutMs)
        assertEquals(60000L, config.writeTimeoutMs)
        assertEquals(60000L, config.totalTimeoutMs)
    }
    
    @Test
    fun `withTimeout returns success for fast operation`() = runTest {
        val result = TimeoutManager.withTimeout("/api/v1/users") {
            "success"
        }
        
        assertTrue(result is TimeoutResult.Success)
        assertEquals("success", (result as TimeoutResult.Success).value)
    }
    
    @Test
    fun `withTimeout returns timeout for slow operation`() = runTest {
        TimeoutManager.clearMetrics()
        
        val result = TimeoutManager.withTimeout("/api/v1/health") {
            delay(10000)
            "should not reach here"
        }
        
        assertTrue(result is TimeoutResult.Timeout)
        val timeoutResult = result as TimeoutResult.Timeout
        assertEquals(5000L, timeoutResult.timeoutMs)
    }
    
    @Test
    fun `withTimeout records metrics on success`() = runTest {
        TimeoutManager.clearMetrics()
        
        TimeoutManager.withTimeout("/api/v1/users") {
            delay(100)
            "result"
        }
        
        val metrics = TimeoutManager.getMetrics()
        assertEquals(1, metrics.size)
        assertFalse(metrics[0].timedOut)
        assertTrue(metrics[0].executionTimeMs >= 100)
    }
    
    @Test
    fun `withTimeout records metrics on timeout`() = runTest {
        TimeoutManager.clearMetrics()
        
        TimeoutManager.withTimeout("/api/v1/health") {
            delay(10000)
            "should not reach here"
        }
        
        val metrics = TimeoutManager.getMetrics()
        assertEquals(1, metrics.size)
        assertTrue(metrics[0].timedOut)
        assertTrue(metrics[0].executionTimeMs >= 5000)
    }
    
    @Test
    fun `getTimeoutStats returns correct statistics`() = runTest {
        TimeoutManager.clearMetrics()
        
        repeat(5) {
            TimeoutManager.withTimeout("/api/v1/users") {
                delay(10)
                "success"
            }
        }
        
        repeat(2) {
            TimeoutManager.withTimeout("/api/v1/health") {
                delay(10000)
                "timeout"
            }
        }
        
        val stats = TimeoutManager.getTimeoutStats()
        assertEquals(7, stats.totalCalls)
        assertEquals(2, stats.timeouts)
        assertTrue(stats.avgExecutionTimeMs > 0)
        assertTrue(stats.maxExecutionTimeMs > 0)
    }
    
    @Test
    fun `getTimeoutStats filters by endpoint`() = runTest {
        TimeoutManager.clearMetrics()
        
        repeat(5) {
            TimeoutManager.withTimeout("/api/v1/users") {
                "success"
            }
        }
        
        repeat(3) {
            TimeoutManager.withTimeout("/api/v1/health") {
                "timeout"
            }
        }
        
        val usersStats = TimeoutManager.getTimeoutStats("/api/v1/users")
        assertEquals(5, usersStats.totalCalls)
        assertEquals(0, usersStats.timeouts)
        
        val healthStats = TimeoutManager.getTimeoutStats("/api/v1/health")
        assertEquals(3, healthStats.totalCalls)
        assertEquals(3, healthStats.timeouts)
    }
    
    @Test
    fun `clearMetrics removes all recorded metrics`() = runTest {
        TimeoutManager.clearMetrics()
        
        TimeoutManager.withTimeout("/api/v1/users") {
            "success"
        }
        
        assertEquals(1, TimeoutManager.getMetrics().size)
        
        TimeoutManager.clearMetrics()
        assertEquals(0, TimeoutManager.getMetrics().size)
    }
    
    @Test
    fun `timeoutRate calculated correctly`() = runTest {
        TimeoutManager.clearMetrics()
        
        repeat(10) {
            TimeoutManager.withTimeout("/api/v1/users") {
                "success"
            }
        }
        
        repeat(5) {
            TimeoutManager.withTimeout("/api/v1/health") {
                delay(10000)
                "timeout"
            }
        }
        
        val stats = TimeoutManager.getTimeoutStats()
        assertEquals(15, stats.totalCalls)
        assertEquals(5, stats.timeouts)
        assertEquals(0.333, stats.timeoutRate, 0.01)
    }
}
