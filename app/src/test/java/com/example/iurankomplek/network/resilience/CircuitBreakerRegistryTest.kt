package com.example.iurankomplek.network.resilience

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

class CircuitBreakerRegistryTest {
    
    @Test
    fun `getEndpoint creates new circuit breaker for unregistered endpoint`() {
        val registry = CircuitBreakerRegistry()
        val cb = registry.getEndpoint("/api/v1/users")
        
        assertNotNull(cb)
        assertEquals(CircuitBreakerState.Closed, cb.getState())
    }
    
    @Test
    fun `getEndpoint returns same circuit breaker for same endpoint`() {
        val registry = CircuitBreakerRegistry()
        val cb1 = registry.getEndpoint("/api/v1/users")
        val cb2 = registry.getEndpoint("/api/v1/users")
        
        assertSame(cb1, cb2)
    }
    
    @Test
    fun `getEndpoint returns different circuit breaker for different endpoints`() {
        val registry = CircuitBreakerRegistry()
        val cb1 = registry.getEndpoint("/api/v1/users")
        val cb2 = registry.getEndpoint("/api/v1/pemanfaatan")
        
        assertNotSame(cb1, cb2)
    }
    
    @Test
    fun `registerEndpoint stores custom config`() = runTest {
        val registry = CircuitBreakerRegistry()
        val customConfig = CircuitBreakerConfig(
            failureThreshold = 2,
            successThreshold = 1,
            timeoutMs = 10000L,
            halfOpenMaxCalls = 2
        )
        
        registry.registerEndpoint("/api/v1/payments", customConfig)
        
        val cb = registry.getEndpoint("/api/v1/payments")
        assertNotNull(cb)
    }
    
    @Test
    fun `execute returns success on successful call`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        val result = registry.execute("/api/v1/users") {
            "success"
        }
        
        assertTrue(result is CircuitBreakerResult.Success)
        assertEquals("success", (result as CircuitBreakerResult.Success).value)
    }
    
    @Test
    fun `execute returns failure on exception`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        val result = registry.execute("/api/v1/users") {
            throw IOException("Network error")
        }
        
        assertTrue(result is CircuitBreakerResult.Failure)
    }
    
    @Test
    fun `execute trips circuit breaker after threshold failures`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        repeat(5) {
            val result = registry.execute("/api/v1/users") {
                throw IOException("Network error")
            }
            assertTrue(result is CircuitBreakerResult.Failure)
        }
        
        val result = registry.execute("/api/v1/users") {
            "should not execute"
        }
        
        assertTrue(result is CircuitBreakerResult.CircuitOpen)
    }
    
    @Test
    fun `execute allows requests after circuit breaker cooldown`() = runTest {
        val registry = CircuitBreakerRegistry()
        val config = CircuitBreakerConfig(
            failureThreshold = 2,
            timeoutMs = 100L
        )
        registry.registerEndpoint("/api/v1/fast-fail", config)
        
        repeat(2) {
            registry.execute("/api/v1/fast-fail") {
                throw IOException("Network error")
            }
        }
        
        var circuitOpenResult = registry.execute("/api/v1/fast-fail") {
            "should not execute"
        }
        assertTrue(circuitOpenResult is CircuitBreakerResult.CircuitOpen)
        
        delay(150)
        
        val result = registry.execute("/api/v1/fast-fail") {
            "success"
        }
        assertTrue(result is CircuitBreakerResult.Success)
    }
    
    @Test
    fun `getStats returns correct statistics`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        repeat(3) {
            registry.execute("/api/v1/users") {
                "success"
            }
        }
        
        repeat(2) {
            registry.execute("/api/v1/users") {
                throw IOException("Network error")
            }
        }
        
        val stats = registry.getStats("/api/v1/users")
        assertNotNull(stats)
        assertEquals(5, stats.totalCalls)
        assertEquals(3, stats.totalSuccesses)
        assertEquals(2, stats.totalFailures)
    }
    
    @Test
    fun `getAllStats returns all circuit breaker stats`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        registry.execute("/api/v1/users") { "success" }
        registry.execute("/api/v1/pemanfaatan") { throw IOException() }
        
        val allStats = registry.getAllStats()
        assertEquals(2, allStats.size)
        assertTrue(allStats.containsKey("/api/v1/users"))
        assertTrue(allStats.containsKey("/api/v1/pemanfaatan"))
    }
    
    @Test
    fun `getState returns current circuit breaker state`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        registry.execute("/api/v1/users") { "success" }
        assertEquals(CircuitBreakerState.Closed, registry.getState("/api/v1/users"))
        
        repeat(5) {
            registry.execute("/api/v1/users") { throw IOException() }
        }
        assertEquals(CircuitBreakerState.Open, registry.getState("/api/v1/users"))
    }
    
    @Test
    fun `getAllStates returns all circuit breaker states`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        registry.execute("/api/v1/users") { "success" }
        registry.execute("/api/v1/pemanfaatan") { throw IOException() }
        
        val allStates = registry.getAllStates()
        assertEquals(2, allStates.size)
        assertTrue(allStates.containsKey("/api/v1/users"))
        assertTrue(allStates.containsKey("/api/v1/pemanfaatan"))
    }
    
    @Test
    fun `resetEndpoint clears circuit breaker state`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        repeat(5) {
            registry.execute("/api/v1/users") { throw IOException() }
        }
        
        assertEquals(CircuitBreakerState.Open, registry.getState("/api/v1/users"))
        
        registry.resetEndpoint("/api/v1/users")
        
        assertEquals(CircuitBreakerState.Closed, registry.getState("/api/v1/users"))
        assertNull(registry.getStats("/api/v1/users"))
    }
    
    @Test
    fun `resetAll clears all circuit breakers`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        repeat(5) {
            registry.execute("/api/v1/users") { throw IOException() }
        }
        registry.execute("/api/v1/pemanfaatan") { throw IOException() }
        
        registry.resetAll()
        
        assertEquals(CircuitBreakerState.Closed, registry.getState("/api/v1/users"))
        assertEquals(CircuitBreakerState.Closed, registry.getState("/api/v1/pemanfaatan"))
        assertEquals(0, registry.getAllStats().size)
    }
    
    @Test
    fun `getOpenCircuits returns endpoints with open circuits`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        repeat(5) {
            registry.execute("/api/v1/users") { throw IOException() }
        }
        
        val openCircuits = registry.getOpenCircuits()
        assertEquals(1, openCircuits.size)
        assertTrue(openCircuits.contains("/api/v1/users"))
    }
    
    @Test
    fun `getClosedCircuits returns endpoints with closed circuits`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        registry.execute("/api/v1/users") { "success" }
        registry.execute("/api/v1/pemanfaatan") { "success" }
        
        val closedCircuits = registry.getClosedCircuits()
        assertEquals(2, closedCircuits.size)
        assertTrue(closedCircuits.contains("/api/v1/users"))
        assertTrue(closedCircuits.contains("/api/v1/pemanfaatan"))
    }
    
    @Test
    fun `getFailureRate calculates correctly`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        repeat(7) {
            registry.execute("/api/v1/users") { "success" }
        }
        
        repeat(3) {
            registry.execute("/api/v1/users") { throw IOException() }
        }
        
        val failureRate = registry.getFailureRate("/api/v1/users")
        assertEquals(0.3, failureRate, 0.01)
    }
    
    @Test
    fun `getAllFailureRates returns all failure rates`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        repeat(8) {
            registry.execute("/api/v1/users") { "success" }
        }
        
        repeat(2) {
            registry.execute("/api/v1/users") { throw IOException() }
        }
        
        repeat(5) {
            registry.execute("/api/v1/pemanfaatan") { "success" }
        }
        
        repeat(5) {
            registry.execute("/api/v1/pemanfaatan") { throw IOException() }
        }
        
        val allFailureRates = registry.getAllFailureRates()
        assertEquals(0.2, allFailureRates["/api/v1/users"], 0.01)
        assertEquals(0.5, allFailureRates["/api/v1/pemanfaatan"], 0.01)
    }
    
    @Test
    fun `unregisterEndpoint removes circuit breaker`() = runTest {
        val registry = CircuitBreakerRegistry()
        
        registry.execute("/api/v1/users") { "success" }
        
        registry.unregisterEndpoint("/api/v1/users")
        
        assertNull(registry.getState("/api/v1/users"))
        assertNull(registry.getStats("/api/v1/users"))
    }
}
