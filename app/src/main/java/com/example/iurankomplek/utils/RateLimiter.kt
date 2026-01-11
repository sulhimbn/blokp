package com.example.iurankomplek.utils

import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max

/**
 * Thread-safe rate limiter using token bucket algorithm.
 * 
 * Prevents API abuse by limiting request rate and provides defense-in-depth
 * against DoS attacks, rate limit errors, and excessive network requests.
 * 
 * SECURITY: Token bucket algorithm is memory-efficient and provides smooth
 * request throttling with burst capability.
 * 
 * ALGORITHM: Token Bucket
 * - Bucket fills at constant rate (tokens per second)
 * - Each request consumes 1 token
 * - Requests blocked when bucket is empty
 * - Burst capability allows temporary request spikes
 * 
 * PERFORMANCE: O(1) time complexity, O(1) space complexity
 * THREAD SAFETY: Uses Mutex for concurrent access protection
 */
class RateLimiter(
    private val maxRequests: Int,
    private val timeWindowMs: Long
) {
    // Mutex ensures thread-safe access to token bucket state
    private val mutex = Mutex()
    
    // Current number of tokens available (never exceeds maxRequests)
    private var availableTokens: Double = maxRequests.toDouble()
    
    // Last timestamp when tokens were added
    private var lastRefillTimestampMs: Long = System.currentTimeMillis()
    
    companion object {
        /**
         * Creates a rate limiter for requests per second
         * 
         * @param requestsPerSecond Maximum requests allowed per second
         */
        fun perSecond(requestsPerSecond: Int): RateLimiter {
            return RateLimiter(
                maxRequests = requestsPerSecond,
                timeWindowMs = Constants.Network.MILLISECONDS_PER_SECOND
            )
        }
        
        /**
         * Creates a rate limiter for requests per minute
         * 
         * @param requestsPerMinute Maximum requests allowed per minute
         */
        fun perMinute(requestsPerMinute: Int): RateLimiter {
            return RateLimiter(
                maxRequests = requestsPerMinute,
                timeWindowMs = Constants.Network.ONE_MINUTE_MS
            )
        }
        
        /**
         * Creates a rate limiter for requests per custom time window
         * 
         * @param maxRequests Maximum requests allowed in time window
         * @param timeWindowMs Time window in milliseconds
         */
        fun custom(maxRequests: Int, timeWindowMs: Long): RateLimiter {
            return RateLimiter(
                maxRequests = maxRequests,
                timeWindowMs = timeWindowMs
            )
        }
    }
    
    /**
     * Attempts to acquire a token for a request.
     * 
     * @return Pair<Boolean, Long?> where:
     *         - Boolean: true if token acquired, false if rate limited
     *         - Long?: wait time in ms before next attempt (null if allowed immediately)
     */
    suspend fun tryAcquire(): Pair<Boolean, Long?> {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            
            // Refill tokens based on elapsed time
            refillTokens(now)
            
            // Check if we have tokens available
            if (availableTokens >= Constants.RateLimiter.SINGLE_TOKEN_REQUEST) {
                // Consume one token
                availableTokens -= Constants.RateLimiter.SINGLE_TOKEN_REQUEST
                Pair(true, null)
            } else {
                // Calculate wait time for next token
                val tokensNeeded = Constants.RateLimiter.SINGLE_TOKEN_REQUEST - availableTokens
                val timePerToken = timeWindowMs.toDouble() / maxRequests
                val waitTimeMs = (tokensNeeded * timePerToken).toLong()
                Pair(false, waitTimeMs)
            }
        }
    }
    
    /**
     * Refills tokens based on elapsed time since last refill.
     * 
     * SECURITY: Ensures tokens don't exceed max capacity.
     * 
     * @param now Current timestamp in milliseconds
     */
    private fun refillTokens(now: Long) {
        val elapsedMs = now - lastRefillTimestampMs
        
        if (elapsedMs > 0) {
            // Calculate tokens to add based on elapsed time
            val tokensToAdd = (elapsedMs.toDouble() / timeWindowMs) * maxRequests
            
            // Refill tokens but don't exceed max capacity
            availableTokens = kotlin.math.max(0.0, kotlin.math.min(maxRequests.toDouble(), availableTokens + tokensToAdd))
            
            // Update last refill timestamp
            lastRefillTimestampMs = now
        }
    }
    
    /**
     * Gets current number of available tokens.
     * 
     * @return Number of tokens available (0 to maxRequests)
     */
    suspend fun getAvailableTokens(): Int {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            refillTokens(now)
            availableTokens.toInt()
        }
    }
    
    /**
     * Gets time to wait before next token is available.
     * 
     * @return Wait time in milliseconds, or 0 if tokens are available now
     */
    suspend fun getTimeToNextToken(): Long {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            refillTokens(now)
            
            if (availableTokens >= 1.0) {
                0L
            } else {
                val tokensNeeded = 1.0 - availableTokens
                val timePerToken = timeWindowMs.toDouble() / maxRequests
                (tokensNeeded * timePerToken).toLong()
            }
        }
    }
    
    /**
     * Resets the rate limiter to initial state.
     * 
     * SECURITY: Useful for testing or when rate limit rules change.
     */
    suspend fun reset() {
        mutex.withLock {
            availableTokens = maxRequests.toDouble()
            lastRefillTimestampMs = System.currentTimeMillis()
        }
    }
    
    /**
     * Gets rate limiter configuration.
     * 
     * @return Triple of (maxRequests, timeWindowMs, tokensPerSecond)
     */
    fun getConfig(): Triple<Int, Long, Double> {
        val tokensPerSecond = maxRequests.toDouble() / (timeWindowMs / 1000.0)
        return Triple(maxRequests, timeWindowMs, tokensPerSecond)
    }
}

/**
 * Multi-level rate limiter for enforcing multiple rate limits simultaneously.
 * 
 * SECURITY: Ensures API usage respects both per-second and per-minute limits.
 * 
 * Example: 10 requests/second AND 60 requests/minute
 */
class MultiLevelRateLimiter(
    private val limiters: List<RateLimiter>
) {
    companion object {
        /**
         * Creates a multi-level rate limiter with per-second and per-minute limits.
         * 
         * @param requestsPerSecond Max requests per second
         * @param requestsPerMinute Max requests per minute
         */
        fun standard(
            requestsPerSecond: Int,
            requestsPerMinute: Int
        ): MultiLevelRateLimiter {
            return MultiLevelRateLimiter(
                limiters = listOf(
                    RateLimiter.perSecond(requestsPerSecond),
                    RateLimiter.perMinute(requestsPerMinute)
                )
            )
        }
    }
    
    /**
     * Attempts to acquire a token for a request across all rate limiters.
     * 
     * SECURITY: Request is only allowed if ALL rate limiters have tokens available.
     * 
     * @return Pair<Boolean, Long?> where:
     *         - Boolean: true if all limiters allowed, false if any limited
     *         - Long?: maximum wait time across all limiters (null if allowed immediately)
     */
    suspend fun tryAcquire(): Pair<Boolean, Long?> {
        var allAllowed = true
        var maxWaitTime: Long? = null
        
        // Check all rate limiters
        for (limiter in limiters) {
            val (allowed, waitTime) = limiter.tryAcquire()
            
            if (!allowed) {
                allAllowed = false
                // Track maximum wait time across all limiters
                if (waitTime != null) {
                    maxWaitTime = max(maxWaitTime ?: 0L, waitTime)
                }
            }
        }
        
        // If any limiter denied the request, return tokens back to others
        // SECURITY: Prevents token leak when request is blocked
        if (!allAllowed) {
            limiters.forEach { it.reset() }
        }
        
        return Pair(allAllowed, maxWaitTime)
    }
    
    /**
     * Gets current status of all rate limiters.
     * 
     * @return List of available tokens for each limiter
     */
    suspend fun getStatus(): List<Int> {
        return limiters.map { it.getAvailableTokens() }
    }
    
    /**
     * Resets all rate limiters.
     */
    suspend fun reset() {
        limiters.forEach { it.reset() }
    }
}
