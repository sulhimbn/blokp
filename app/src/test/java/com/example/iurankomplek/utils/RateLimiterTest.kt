package com.example.iurankomplek.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for RateLimiter and MultiLevelRateLimiter
 * 
 * Tests cover:
 * - Happy paths (normal rate limiting)
 * - Edge cases (empty bucket, burst capacity, time boundaries)
 * - Thread safety (concurrent access)
 * - Configuration (per-second, per-minute, custom limits)
 * - Multi-level rate limiting (combined limits)
 * - Token bucket algorithm correctness
 */
class RateLimiterTest {
    
    private lateinit var rateLimiter: RateLimiter
    
    @Before
    fun setUp() {
        // Create rate limiter: 10 requests per second
        rateLimiter = RateLimiter.perSecond(10)
    }
    
    // ===== RATE LIMITER FACTORY TESTS =====
    
    @Test
    fun `perSecond factory creates correct rate limiter`() {
        val limiter = RateLimiter.perSecond(5)
        val config = limiter.getConfig()
        
        assertEquals(5, config.first)
        assertEquals(1000L, config.second)
        assertEquals(5.0, config.third, 0.01)
    }
    
    @Test
    fun `perMinute factory creates correct rate limiter`() {
        val limiter = RateLimiter.perMinute(60)
        val config = limiter.getConfig()
        
        assertEquals(60, config.first)
        assertEquals(Constants.Network.ONE_MINUTE_MS, config.second)
        assertEquals(1.0, config.third, 0.01)
    }
    
    @Test
    fun `custom factory creates correct rate limiter`() {
        val limiter = RateLimiter.custom(100, 5000L)
        val config = limiter.getConfig()
        
        assertEquals(100, config.first)
        assertEquals(5000L, config.second)
        assertEquals(20.0, config.third, 0.01)
    }
    
    // ===== TRY ACQUIRE TESTS =====
    
    @Test
    fun `tryAcquire with available tokens returns true and null wait time`() = runBlocking {
        val (allowed, waitTime) = rateLimiter.tryAcquire()
        
        assertTrue(allowed)
        assertNull(waitTime)
    }
    
    @Test
    fun `tryAcquire within limit returns true repeatedly`() = runBlocking {
        repeat(10) {
            val (allowed, waitTime) = rateLimiter.tryAcquire()
            assertTrue("Request $it should be allowed", allowed)
            assertNull(waitTime)
        }
    }
    
    @Test
    fun `tryAcquire exceeding limit returns false and wait time`() = runBlocking {
        // Use all 10 tokens
        repeat(10) {
            rateLimiter.tryAcquire()
        }
        
        // Try one more (should be rate limited)
        val (allowed, waitTime) = rateLimiter.tryAcquire()
        
        assertFalse(allowed)
        assertNotNull(waitTime)
        assertTrue(waitTime!! > 0)
    }
    
    @Test
    fun `tryAcquire after refill allows requests again`() = runBlocking {
        // Use all tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        // Verify rate limited
        var (allowed, _) = rateLimiter.tryAcquire()
        assertFalse(allowed)
        
        // Wait for refill (1 second)
        delay(1000)
        
        // Should be allowed again
        (allowed, _) = rateLimiter.tryAcquire()
        assertTrue(allowed)
    }
    
    @Test
    fun `tryAcquire after partial refill allows some requests`() = runBlocking {
        // Use all 10 tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        // Wait for partial refill (500ms = 5 tokens)
        delay(500)
        
        // Should allow 5 requests
        repeat(5) {
            val (allowed, _) = rateLimiter.tryAcquire()
            assertTrue("Request $it should be allowed after partial refill", allowed)
        }
        
        // 6th request should be limited
        val (allowed, _) = rateLimiter.tryAcquire()
        assertFalse(allowed)
    }
    
    // ===== GET AVAILABLE TOKENS TESTS =====
    
    @Test
    fun `getAvailableTokens initially returns max requests`() = runBlocking {
        val available = rateLimiter.getAvailableTokens()
        assertEquals(10, available)
    }
    
    @Test
    fun `getAvailableTokens after consumption decreases correctly`() = runBlocking {
        // Consume 5 tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        val available = rateLimiter.getAvailableTokens()
        assertEquals(5, available)
    }
    
    @Test
    fun `getAvailableTokens after consumption returns zero when empty`() = runBlocking {
        // Consume all tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        val available = rateLimiter.getAvailableTokens()
        assertEquals(0, available)
    }
    
    @Test
    fun `getAvailableTokens after refill increases correctly`() = runBlocking {
        // Consume all tokens
        repeat(10) { rateLimiter.getAvailableTokens() }
        repeat(10) { rateLimiter.tryAcquire() }
        
        // Wait for partial refill (500ms = 5 tokens)
        delay(500)
        
        val available = rateLimiter.getAvailableTokens()
        assertEquals(5, available)
    }
    
    @Test
    fun `getAvailableTokens caps at max requests`() = runBlocking {
        // Wait longer than time window
        delay(2000)
        
        val available = rateLimiter.getAvailableTokens()
        assertEquals(10, available)
    }
    
    // ===== GET TIME TO NEXT TOKEN TESTS =====
    
    @Test
    fun `getTimeToNextToken with available tokens returns zero`() = runBlocking {
        val waitTime = rateLimiter.getTimeToNextToken()
        assertEquals(0L, waitTime)
    }
    
    @Test
    fun `getTimeToNextToken after consumption returns positive wait time`() = runBlocking {
        // Consume all tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        val waitTime = rateLimiter.getTimeToNextToken()
        assertTrue(waitTime > 0)
    }
    
    @Test
    fun `getTimeToNextToken after partial wait decreases`() = runBlocking {
        // Consume all tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        val waitTime1 = rateLimiter.getTimeToNextToken()
        
        // Wait 500ms
        delay(500)
        
        val waitTime2 = rateLimiter.getTimeToNextToken()
        
        assertTrue(waitTime2 < waitTime1)
    }
    
    @Test
    fun `getTimeToNextToken after refill returns zero`() = runBlocking {
        // Consume all tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        // Wait for full refill
        delay(1000)
        
        val waitTime = rateLimiter.getTimeToNextToken()
        assertEquals(0L, waitTime)
    }
    
    // ===== RESET TESTS =====
    
    @Test
    fun `reset restores all tokens`() = runBlocking {
        // Consume all tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        assertEquals(0, rateLimiter.getAvailableTokens())
        
        // Reset
        rateLimiter.reset()
        
        assertEquals(10, rateLimiter.getAvailableTokens())
    }
    
    @Test
    fun `reset updates last refill timestamp`() = runBlocking {
        // Consume tokens
        repeat(5) { rateLimiter.tryAcquire() }
        
        // Wait some time
        delay(500)
        
        // Reset
        rateLimiter.reset()
        
        // Verify available tokens (should be 10, not 5 + refill)
        assertEquals(10, rateLimiter.getAvailableTokens())
    }
    
    @Test
    fun `reset after rate limit allows requests immediately`() = runBlocking {
        // Use all tokens
        repeat(10) { rateLimiter.tryAcquire() }
        
        // Verify rate limited
        var (allowed, _) = rateLimiter.tryAcquire()
        assertFalse(allowed)
        
        // Reset
        rateLimiter.reset()
        
        // Should be allowed immediately
        (allowed, _) = rateLimiter.tryAcquire()
        assertTrue(allowed)
    }
    
    // ===== BOUNDARY CONDITION TESTS =====
    
    @Test
    fun `tryAcquire with single request rate limiter works correctly`() = runBlocking {
        val singleLimiter = RateLimiter.perSecond(1)
        
        // First request should be allowed
        val (allowed1, _) = singleLimiter.tryAcquire()
        assertTrue(allowed1)
        
        // Second request should be limited
        val (allowed2, waitTime) = singleLimiter.tryAcquire()
        assertFalse(allowed2)
        assertNotNull(waitTime)
        
        // Wait for token refill
        delay(1100)
        
        // Request should be allowed again
        val (allowed3, _) = singleLimiter.tryAcquire()
        assertTrue(allowed3)
    }
    
    @Test
    fun `tryAcquire with large max requests handles correctly`() = runBlocking {
        val largeLimiter = RateLimiter.perSecond(1000)
        
        // Should allow 1000 requests
        repeat(1000) {
            val (allowed, _) = largeLimiter.tryAcquire()
            assertTrue("Request $it should be allowed", allowed)
        }
        
        // 1001st request should be limited
        val (allowed, _) = largeLimiter.tryAcquire()
        assertFalse(allowed)
    }
    
    @Test
    fun `tryAcquire with custom time window works correctly`() = runBlocking {
        // 10 requests per 100ms
        val customLimiter = RateLimiter.custom(10, 100L)
        
        // Should allow 10 requests
        repeat(10) {
            val (allowed, _) = customLimiter.tryAcquire()
            assertTrue(allowed)
        }
        
        // Wait for refill (100ms)
        delay(100)
        
        // Should allow again
        val (allowed, _) = customLimiter.tryAcquire()
        assertTrue(allowed)
    }
    
    @Test
    fun `getAvailableTokens never exceeds max requests`() = runBlocking {
        // Wait much longer than time window
        delay(5000)
        
        val available = rateLimiter.getAvailableTokens()
        assertEquals(10, available)
    }
    
    // ===== THREAD SAFETY TESTS =====
    
    @Test
    fun `tryAcquire is thread-safe under concurrent access`() = runBlocking {
        val limiter = RateLimiter.perSecond(100)
        var successCount = 0
        
        // Launch 200 concurrent requests
        val jobs = (1..200).map {
            launch {
                val (allowed, _) = limiter.tryAcquire()
                if (allowed) {
                    successCount++
                }
            }
        }
        
        jobs.forEach { it.join() }
        
        // Should allow exactly 100 requests (rate limit)
        assertEquals(100, successCount)
    }
    
    @Test
    fun `getAvailableTokens is thread-safe under concurrent access`() = runBlocking {
        val limiter = RateLimiter.perSecond(100)
        
        // Launch 50 concurrent token checks
        val jobs = (1..50).map {
            launch {
                limiter.getAvailableTokens()
            }
        }
        
        jobs.forEach { it.join() }
        
        // Should complete without exceptions
        assertTrue(true)
    }
    
    @Test
    fun `reset is thread-safe under concurrent access`() = runBlocking {
        val limiter = RateLimiter.perSecond(10)
        
        // Consume all tokens
        repeat(10) { limiter.tryAcquire() }
        
        // Launch concurrent resets and acquires
        val jobs = (1..10).map {
            launch {
                if (it % 2 == 0) {
                    limiter.reset()
                } else {
                    limiter.tryAcquire()
                }
            }
        }
        
        jobs.forEach { it.join() }
        
        // Should complete without exceptions
        assertTrue(true)
    }
    
    // ===== MULTI-LEVEL RATE LIMITER TESTS =====
    
    @Test
    fun `MultiLevelRateLimiter standard creates correct limiters`() = runBlocking {
        val multiLimiter = MultiLevelRateLimiter.standard(10, 60)
        
        // Should have 2 limiters (per-second and per-minute)
        val status = multiLimiter.getStatus()
        assertEquals(2, status.size)
    }
    
    @Test
    fun `MultiLevelRateLimiter tryAcquire allows when all limiters allow`() = runBlocking {
        val multiLimiter = MultiLevelRateLimiter.standard(10, 60)
        
        // Should be allowed (both limiters have tokens)
        val (allowed, waitTime) = multiLimiter.tryAcquire()
        assertTrue(allowed)
        assertNull(waitTime)
    }
    
    @Test
    fun `MultiLevelRateLimiter tryAcquire denies when one limiter denies`() = runBlocking {
        val multiLimiter = MultiLevelRateLimiter.standard(1, 60)
        
        // First request allowed
        multiLimiter.tryAcquire()
        
        // Second request denied (per-second limiter empty)
        val (allowed, waitTime) = multiLimiter.tryAcquire()
        assertFalse(allowed)
        assertNotNull(waitTime)
    }
    
    @Test
    fun `MultiLevelRateLimiter tryAcquire denies when all limiters deny`() = runBlocking {
        val multiLimiter = MultiLevelRateLimiter.standard(1, 1)
        
        // First request allowed
        multiLimiter.tryAcquire()
        
        // Second request denied (both limiters empty)
        val (allowed, waitTime) = multiLimiter.tryAcquire()
        assertFalse(allowed)
        assertNotNull(waitTime)
    }
    
    @Test
    fun `MultiLevelRateLimiter after per-second refill still respects per-minute limit`() = runBlocking {
        val multiLimiter = MultiLevelRateLimiter.standard(1, 2)
        
        // Use 1 request (allowed)
        multiLimiter.tryAcquire()
        
        // Wait for per-second refill (1 second)
        delay(1100)
        
        // Second request allowed (per-second refilled)
        val (allowed1, _) = multiLimiter.tryAcquire()
        assertTrue(allowed1)
        
        // Wait for per-second refill again
        delay(1100)
        
        // Third request denied (per-minute limit reached)
        val (allowed2, _) = multiLimiter.tryAcquire()
        assertFalse(allowed2)
    }
    
    @Test
    fun `MultiLevelRateLimiter getStatus returns correct token counts`() = runBlocking {
        val multiLimiter = MultiLevelRateLimiter.standard(10, 60)
        
        // Use 5 tokens
        repeat(5) { multiLimiter.tryAcquire() }
        
        val status = multiLimiter.getStatus()
        
        assertEquals(5, status[0]) // Per-second: 10 - 5 = 5
        assertEquals(55, status[1]) // Per-minute: 60 - 5 = 55
    }
    
    @Test
    fun `MultiLevelRateLimiter reset restores all tokens`() = runBlocking {
        val multiLimiter = MultiLevelRateLimiter.standard(10, 60)
        
        // Use 5 tokens
        repeat(5) { multiLimiter.tryAcquire() }
        
        // Reset
        multiLimiter.reset()
        
        val status = multiLimiter.getStatus()
        
        assertEquals(10, status[0]) // Per-second restored
        assertEquals(60, status[1]) // Per-minute restored
    }
    
    // ===== BURST CAPACITY TESTS =====
    
    @Test
    fun `RateLimiter allows burst requests up to max tokens`() = runBlocking {
        val burstLimiter = RateLimiter.perSecond(10)
        
        // Should allow burst of 10 requests immediately
        var allowedCount = 0
        repeat(10) {
            val (allowed, _) = burstLimiter.tryAcquire()
            if (allowed) allowedCount++
        }
        
        assertEquals(10, allowedCount)
    }
    
    @Test
    fun `RateLimiter denies burst exceeding max tokens`() = runBlocking {
        val burstLimiter = RateLimiter.perSecond(10)
        
        // Try 11 requests (should deny 11th)
        var allowedCount = 0
        repeat(11) {
            val (allowed, _) = burstLimiter.tryAcquire()
            if (allowed) allowedCount++
        }
        
        assertEquals(10, allowedCount)
    }
    
    // ===== CONSISTENCY TESTS =====
    
    @Test
    fun `RateLimiter token accounting is consistent`() = runBlocking {
        // Start with 10 tokens
        assertEquals(10, rateLimiter.getAvailableTokens())
        
        // Use 5 tokens
        repeat(5) { rateLimiter.tryAcquire() }
        assertEquals(5, rateLimiter.getAvailableTokens())
        
        // Use 3 more tokens
        repeat(3) { rateLimiter.tryAcquire() }
        assertEquals(2, rateLimiter.getAvailableTokens())
        
        // Use remaining 2 tokens
        repeat(2) { rateLimiter.tryAcquire() }
        assertEquals(0, rateLimiter.getAvailableTokens())
    }
    
    @Test
    fun `RateLimiter wait time calculation is accurate`() = runBlocking {
        val limiter = RateLimiter.perSecond(10)
        
        // Use all tokens
        repeat(10) { limiter.tryAcquire() }
        
        val waitTime = limiter.getTimeToNextToken()
        
        // Wait for next token
        delay(waitTime)
        
        // Should now be allowed
        val (allowed, _) = limiter.tryAcquire()
        assertTrue(allowed)
    }
}
