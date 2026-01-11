package com.example.iurankomplek.network.resilience

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CircuitBreakerTest {
    
    private lateinit var circuitBreaker: CircuitBreaker
    
    @Before
    fun setUp() {
        circuitBreaker = CircuitBreaker(
            failureThreshold = 3,
            successThreshold = 2,
            timeout = 1000L,
            halfOpenMaxCalls = 3
        )
    }
    
    @After
    fun tearDown() {
        circuitBreaker.reset()
    }
    
    @Test
    fun `execute returns success on successful operation`() = runTest {
        val result = circuitBreaker.execute { "Success" }
        
        assertTrue(result is CircuitBreakerResult.Success)
        assertEquals("Success", (result as CircuitBreakerResult.Success).value)
    }
    
    @Test
    fun `execute returns failure on failed operation`() = runTest {
        val exception = RuntimeException("Test error")
        val result = circuitBreaker.execute { throw exception }
        
        assertTrue(result is CircuitBreakerResult.Failure)
        assertEquals(exception, (result as CircuitBreakerResult.Failure).exception)
    }
    
    @Test
    fun `circuit remains closed with successful calls`() = runTest {
        repeat(5) {
            val result = circuitBreaker.execute { "Success" }
            assertTrue(result is CircuitBreakerResult.Success)
        }
        
        assertEquals(CircuitBreakerState.Closed, circuitBreaker.getState())
    }
    
    @Test
    fun `circuit opens after failure threshold is reached`() = runTest {
        val exception = RuntimeException("Test error")
        
        repeat(3) {
            circuitBreaker.execute { throw exception }
        }
        
        assertEquals(CircuitBreakerState.Open, circuitBreaker.getState())
    }
    
    @Test
    fun `circuit open returns CircuitOpen result`() = runTest {
        val exception = RuntimeException("Test error")
        
        repeat(3) {
            circuitBreaker.execute { throw exception }
        }
        
        val result = circuitBreaker.execute { "Should not execute" }
        assertTrue(result is CircuitBreakerResult.CircuitOpen)
    }
    
    @Test
    fun `circuit transitions to half-open after timeout`() = runTest {
        val exception = RuntimeException("Test error")
        
        repeat(3) {
            circuitBreaker.execute { throw exception }
        }
        
        assertEquals(CircuitBreakerState.Open, circuitBreaker.getState())
        
        delay(1500L)
        
        val result = circuitBreaker.execute { "Half-open success" }
        assertTrue(result is CircuitBreakerResult.Success)
        assertEquals(CircuitBreakerState.HalfOpen, circuitBreaker.getState())
    }
    
    @Test
    fun `circuit closes after success threshold in half-open state`() = runTest {
        val exception = RuntimeException("Test error")
        
        repeat(3) {
            circuitBreaker.execute { throw exception }
        }
        
        assertEquals(CircuitBreakerState.Open, circuitBreaker.getState())
        
        delay(1500L)
        
        repeat(2) {
            val result = circuitBreaker.execute { "Success" }
            assertTrue(result is CircuitBreakerResult.Success)
        }
        
        assertEquals(CircuitBreakerState.Closed, circuitBreaker.getState())
    }
    
    @Test
    fun `circuit opens again on failure in half-open state`() = runTest {
        val exception = RuntimeException("Test error")
        
        repeat(3) {
            circuitBreaker.execute { throw exception }
        }
        
        assertEquals(CircuitBreakerState.Open, circuitBreaker.getState())
        
        delay(1500L)
        
        val firstSuccess = circuitBreaker.execute { "Success" }
        assertTrue(firstSuccess is CircuitBreakerResult.Success)
        
        val failureResult = circuitBreaker.execute { throw exception }
        assertTrue(failureResult is CircuitBreakerResult.Failure)
        
        assertEquals(CircuitBreakerState.Open, circuitBreaker.getState())
    }
    
    @Test
    fun `failure count resets on successful call in closed state`() = runTest {
        val exception = RuntimeException("Test error")
        
        circuitBreaker.execute { throw exception }
        circuitBreaker.execute { throw exception }
        
        assertEquals(2, circuitBreaker.getFailureCount())
        
        circuitBreaker.execute { "Success" }
        
        assertEquals(0, circuitBreaker.getFailureCount())
    }
    
    @Test
    fun `reset returns circuit to closed state`() = runTest {
        val exception = RuntimeException("Test error")
        
        repeat(3) {
            circuitBreaker.execute { throw exception }
        }
        
        assertEquals(CircuitBreakerState.Open, circuitBreaker.getState())
        
        circuitBreaker.reset()
        
        assertEquals(CircuitBreakerState.Closed, circuitBreaker.getState())
        assertEquals(0, circuitBreaker.getFailureCount())
    }
    
    @Test
    fun `circuit breaker handles suspend functions correctly`() = runTest {
        var executionCount = 0
        
        val result = circuitBreaker.execute {
            executionCount++
            delay(100)
            "Async result"
        }
        
        assertTrue(result is CircuitBreakerResult.Success)
        assertEquals("Async result", (result as CircuitBreakerResult.Success).value)
        assertEquals(1, executionCount)
    }
    
    @Test
    fun `half-open max calls limit works correctly`() = runTest {
        val exception = RuntimeException("Test error")
        
        repeat(3) {
            circuitBreaker.execute { throw exception }
        }
        
        assertEquals(CircuitBreakerState.Open, circuitBreaker.getState())
        
        delay(1500L)
        
        repeat(3) {
            val result = circuitBreaker.execute { "Partial success" }
            assertTrue(result is CircuitBreakerResult.Success)
        }
        
        assertEquals(CircuitBreakerState.HalfOpen, circuitBreaker.getState())
    }
    
    @Test
    fun `last failure time is updated on failure`() = runTest {
        val exception = RuntimeException("Test error")
        
        val beforeTime = System.currentTimeMillis()
        
        circuitBreaker.execute { throw exception }
        
        val afterTime = System.currentTimeMillis()
        
        assertTrue(circuitBreaker.getLastFailureTime() >= beforeTime)
        assertTrue(circuitBreaker.getLastFailureTime() <= afterTime)
    }
}
