package com.example.iurankomplek

import com.example.iurankomplek.utils.RateLimiter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class RateLimiterTest {
    
    private lateinit var rateLimiter: RateLimiter
    
    @Before
    fun setup() {
        // Create rate limiter: 5 requests per second
        rateLimiter = RateLimiter.perSecond(5)
    }
    
    // ========== Factory Methods Tests ==========
    
    @Test
    fun `perSecond creates rate limiter with correct config`() {
        val limiter = RateLimiter.perSecond(10)
        val (maxRequests, timeWindowMs, tokensPerSecond) = limiter.getConfig()
        
        assertEquals(10, maxRequests)
        assertEquals(1000L, timeWindowMs)
        assertEquals(10.0, tokensPerSecond, 0.01)
    }
    
    @Test
    fun `perMinute creates rate limiter with correct config`() {
        val limiter = RateLimiter.perMinute(60)
        val (maxRequests, timeWindowMs, tokensPerSecond) = limiter.getConfig()
        
        assertEquals(60, maxRequests)
        assertEquals(60000L, timeWindowMs)
        assertEquals(1.0, tokensPerSecond, 0.01)
    }
    
    @Test
    fun `custom creates rate limiter with correct config`() {
        val limiter = RateLimiter.custom(10, 5000L)
        val (maxRequests, timeWindowMs, tokensPerSecond) = limiter.getConfig()
        
        assertEquals(10, maxRequests)
        assertEquals(5000L, timeWindowMs)
        assertEquals(2.0, tokensPerSecond, 0.01) // 10 tokens per 5 seconds = 2 per second
    }
    
    // ========== Token Acquisition Tests ==========
    
    @Test
    fun `tryAcquire returns true when tokens available`() = runBlocking {
        val (allowed, waitTime) = rateLimiter.tryAcquire()
        
        assertTrue("First request should be allowed", allowed)
        assertNull("Wait time should be null when allowed", waitTime)
    }
    
    @Test
    fun `tryAcquire returns true for initial burst`() = runBlocking {
        // Should allow 5 requests immediately (initial burst)
        var allowedCount = 0
        repeat(5) {
            val (allowed, _) = rateLimiter.tryAcquire()
            if (allowed) allowedCount++
        }
        
        assertEquals("Should allow 5 requests initially", 5, allowedCount)
    }
    
    @Test
    fun `tryAcquire returns false when tokens exhausted`() = runBlocking {
        // Consume all 5 initial tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        // Sixth request should be blocked
        val (allowed, waitTime) = rateLimiter.tryAcquire()
        
        assertFalse("Should be rate limited", allowed)
        assertNotNull("Wait time should be provided", waitTime)
        assertTrue("Wait time should be positive", waitTime!! > 0)
    }
    
    @Test
    fun `tryAcquire refills tokens over time`() = runBlocking {
        // Consume all 5 initial tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        // Wait for token refill (1 second for 5 requests/second = 1 request per 200ms)
        Thread.sleep(500L)
        
        // Should have approximately 2-3 tokens refilled
        val (allowed, _) = rateLimiter.tryAcquire()
        assertTrue("Should allow request after refill", allowed)
    }
    
    @Test
    fun `tryAcquire calculates correct wait time`() = runBlocking {
        // Consume all 5 initial tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        val (allowed, waitTime) = rateLimiter.tryAcquire()
        
        assertFalse("Should be rate limited", allowed)
        assertNotNull("Wait time should be provided", waitTime)
        
        // For 5 requests/second, wait time should be around 200ms
        val expectedWaitMs = 1000.0 / 5.0 // 200ms
        assertTrue("Wait time should be close to expected", 
            abs(waitTime!! - expectedWaitMs) < 50)
    }
    
    // ========== Available Tokens Tests ==========
    
    @Test
    fun `getAvailableTokens returns initial capacity`() = runBlocking {
        val availableTokens = rateLimiter.getAvailableTokens()
        
        assertEquals(5, availableTokens)
    }
    
    @Test
    fun `getAvailableTokens decreases after request`() = runBlocking {
        rateLimiter.tryAcquire()
        
        val availableTokens = rateLimiter.getAvailableTokens()
        
        assertEquals(4, availableTokens)
    }
    
    @Test
    fun `getAvailableTokens returns zero when exhausted`() = runBlocking {
        repeat(5) { rateLimiter.tryAcquire() }
        
        val availableTokens = rateLimiter.getAvailableTokens()
        
        assertEquals(0, availableTokens)
    }
    
    @Test
    fun `getAvailableTokens increases after refill`() = runBlocking {
        // Consume all 5 initial tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        // Wait for refill
        Thread.sleep(500L)
        
        val availableTokens = rateLimiter.getAvailableTokens()
        
        assertTrue("Should have tokens after refill", availableTokens > 0)
        assertTrue("Should not exceed max capacity", availableTokens <= 5)
    }
    
    // ========== Time to Next Token Tests ==========
    
    @Test
    fun `getTimeToNextToken returns zero when tokens available`() = runBlocking {
        val timeToNextToken = rateLimiter.getTimeToNextToken()
        
        assertEquals(0L, timeToNextToken)
    }
    
    @Test
    fun `getTimeToNextToken returns positive wait time when exhausted`() = runBlocking {
        // Consume all 5 initial tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        val timeToNextToken = rateLimiter.getTimeToNextToken()
        
        assertTrue("Wait time should be positive", timeToNextToken > 0)
    }
    
    @Test
    fun `getTimeToNextToken decreases over time`() = runBlocking {
        // Consume all 5 initial tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        val initialWaitTime = rateLimiter.getTimeToNextToken()
        
        // Wait a bit
        Thread.sleep(100L)
        
        val laterWaitTime = rateLimiter.getTimeToNextToken()
        
        assertTrue("Wait time should decrease", laterWaitTime < initialWaitTime)
    }
    
    // ========== Reset Tests ==========
    
    @Test
    fun `reset restores initial token capacity`() = runBlocking {
        // Consume some tokens
        repeat(3) { rateLimiter.tryAcquire() }
        
        rateLimiter.reset()
        
        val availableTokens = rateLimiter.getAvailableTokens()
        assertEquals(5, availableTokens)
    }
    
    @Test
    fun `reset allows immediate requests after exhaustion`() = runBlocking {
        // Consume all 5 initial tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        // Sixth request should be blocked
        var (allowed, _) = rateLimiter.tryAcquire()
        assertFalse("Should be rate limited", allowed)
        
        // Reset
        rateLimiter.reset()
        
        // Should be allowed again
        val (allowedAfterReset, _) = rateLimiter.tryAcquire()
        assertTrue("Should be allowed after reset", allowedAfterReset)
    }
    
    // ========== Configuration Tests ==========
    
    @Test
    fun `getConfig returns correct parameters`() = runBlocking {
        val (maxRequests, timeWindowMs, tokensPerSecond) = rateLimiter.getConfig()
        
        assertEquals(5, maxRequests)
        assertEquals(1000L, timeWindowMs)
        assertEquals(5.0, tokensPerSecond, 0.01)
    }
    
    // ========== Edge Cases Tests ==========
    
    @Test
    fun `handles single request per second`() = runBlocking {
        val limiter = RateLimiter.perSecond(1)
        
        // First request should be allowed
        var (allowed, _) = limiter.tryAcquire()
        assertTrue("First request allowed", allowed)
        
        // Second request should be blocked
        val result2 = limiter.tryAcquire()
        allowed = result2.first
        assertFalse("Second request blocked", allowed)
        
        // Wait for refill
        Thread.sleep(1100L)
        
        // Should be allowed again
        val result3 = limiter.tryAcquire()
        allowed = result3.first
        assertTrue("Request allowed after refill", allowed)
    }
    
    @Test
    fun `handles high rate limit`() = runBlocking {
        val limiter = RateLimiter.perSecond(100)
        
        // Should allow 100 requests immediately
        var allowedCount = 0
        repeat(100) {
            val (allowed, _) = limiter.tryAcquire()
            if (allowed) allowedCount++
        }
        
        assertEquals(100, allowedCount)
    }
    
    @Test
    fun `handles per minute rate limit`() = runBlocking {
        val limiter = RateLimiter.perMinute(10)
        
        // Should allow 10 requests immediately
        var allowedCount = 0
        repeat(10) {
            val (allowed, _) = limiter.tryAcquire()
            if (allowed) allowedCount++
        }
        
        assertEquals(10, allowedCount)
    }
    
    @Test
    fun `prevents token accumulation beyond max`() = runBlocking {
        val limiter = RateLimiter.perSecond(5)
        
        // Consume all tokens
        repeat(5) { limiter.tryAcquire() }
        
        // Wait for 3 seconds (should refill 15 tokens, but max is 5)
        Thread.sleep(3000L)
        
        val availableTokens = limiter.getAvailableTokens()
        assertEquals("Tokens should not exceed max capacity", 5, availableTokens)
    }
    
    @Test
    fun `handles concurrent requests safely`() = runBlocking {
        val limiter = RateLimiter.perSecond(10)
        
        // Simulate concurrent access
        val results = mutableListOf<Boolean>()
        val jobs = List(20) {
            kotlinx.coroutines.async {
                val (allowed, _) = limiter.tryAcquire()
                results.add(allowed)
            }
        }
        
        jobs.forEach { it.await() }
        
        // Exactly 10 requests should be allowed (initial burst)
        val allowedCount = results.count { it }
        assertEquals(10, allowedCount)
    }
    
    // ========== MultiLevelRateLimiter Tests ==========
    
    @Test
    fun `MultiLevelRateLimiter enforces both limits`() = runBlocking {
        val limiter = MultiLevelRateLimiter.standard(
            requestsPerSecond = 5,
            requestsPerMinute = 10
        )
        
        // Should allow 5 requests (limited by per-second limit)
        var allowedCount = 0
        repeat(10) {
            val (allowed, _) = limiter.tryAcquire()
            if (allowed) allowedCount++
        }
        
        assertEquals("Should be limited by per-second limit", 5, allowedCount)
    }
    
    @Test
    fun `MultiLevelRateLimiter status returns correct tokens`() = runBlocking {
        val limiter = MultiLevelRateLimiter.standard(
            requestsPerSecond = 5,
            requestsPerMinute = 10
        )
        
        val status = limiter.getStatus()
        
        assertEquals("Should have status for both limiters", 2, status.size)
        assertEquals("First limiter should have 5 tokens", 5, status[0])
        assertEquals("Second limiter should have 10 tokens", 10, status[1])
    }
    
    @Test
    fun `MultiLevelRateLimiter reset works`() = runBlocking {
        val limiter = MultiLevelRateLimiter.standard(
            requestsPerSecond = 5,
            requestsPerMinute = 10
        )
        
        // Consume tokens
        repeat(5) { limiter.tryAcquire() }
        
        limiter.reset()
        
        val status = limiter.getStatus()
        assertEquals("First limiter should be reset", 5, status[0])
        assertEquals("Second limiter should be reset", 10, status[1])
    }
}
