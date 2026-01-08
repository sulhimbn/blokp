package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.model.ApiErrorCode
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.MultiLevelRateLimiter
import com.example.iurankomplek.utils.RateLimiter
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class RateLimiterInterceptor(
    private val maxRequestsPerSecond: Int = Constants.Network.MAX_REQUESTS_PER_SECOND,
    private val maxRequestsPerMinute: Int = Constants.Network.MAX_REQUESTS_PER_MINUTE,
    private val enableLogging: Boolean = false,
    private val tag: String = "RateLimiterInterceptor",
    private val useTokenBucket: Boolean = true  // SECURITY: Use token bucket for better burst handling
) : Interceptor {

    // Rate limiter using token bucket algorithm (better burst handling)
    private val multiLevelRateLimiter = MultiLevelRateLimiter.standard(
        requestsPerSecond = maxRequestsPerSecond,
        requestsPerMinute = maxRequestsPerMinute
    )
    
    // Fallback: Simple sliding window rate limiter (for compatibility)
    private val requestTimestamps = ConcurrentLinkedQueue<Long>()
    private val endpointCounters = ConcurrentHashMap<String, EndpointStats>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = getEndpointKey(request)

        // SECURITY: Primary rate limiting using token bucket algorithm
        if (useTokenBucket) {
            val (allowed, waitTime) = runBlocking {
                multiLevelRateLimiter.tryAcquire()
            }
            
            if (!allowed) {
                val error = NetworkError.HttpError(
                    code = ApiErrorCode.RATE_LIMIT_EXCEEDED,
                    userMessage = "Too many requests. Please try again in ${waitTime}ms.",
                    httpCode = 429,
                    details = "Rate limit exceeded for endpoint: $endpoint. Retry after: ${waitTime}ms"
                )
                if (enableLogging) {
                    logRateLimitExceeded(endpoint, error, waitTime)
                }
                throw error
            }
        } else {
            // SECURITY: Fallback to sliding window algorithm (for compatibility)
            if (!checkRateLimit(endpoint)) {
                val error = NetworkError.HttpError(
                    code = ApiErrorCode.RATE_LIMIT_EXCEEDED,
                    userMessage = "Too many requests. Please slow down.",
                    httpCode = 429,
                    details = "Rate limit exceeded for endpoint: $endpoint"
                )
                if (enableLogging) {
                    logRateLimitExceeded(endpoint, error, null)
                }
                throw error
            }
        }

        updateEndpointStats(endpoint)

        return chain.proceed(request)
    }

    private fun checkRateLimit(endpoint: String): Boolean {
        val currentTime = System.currentTimeMillis()

        synchronized(requestTimestamps) {
            cleanupOldTimestamps(currentTime)

            if (requestTimestamps.size >= maxRequestsPerMinute) {
                return false
            }

            if (isRequestTooFast(currentTime)) {
                return false
            }

            requestTimestamps.add(currentTime)
        }

        updateEndpointStats(endpoint)

        return true
    }

    private fun cleanupOldTimestamps(currentTime: Long) {
        val oneMinuteAgo = currentTime - Constants.Network.ONE_MINUTE_MS
        while (requestTimestamps.isNotEmpty()) {
            val oldestTimestamp = requestTimestamps.peek()
            if (oldestTimestamp != null && oldestTimestamp < oneMinuteAgo) {
                requestTimestamps.poll()
            } else {
                break
            }
        }
    }

    private fun isRequestTooFast(currentTime: Long): Boolean {
        if (requestTimestamps.isEmpty()) return false

        val lastTimestamp = requestTimestamps.last()
        val timeSinceLastRequest = currentTime - lastTimestamp
        val minInterval = com.example.iurankomplek.utils.Constants.Network.MILLISECONDS_PER_SECOND / maxRequestsPerSecond

        return timeSinceLastRequest < minInterval
    }

    private fun updateEndpointStats(endpoint: String) {
        val stats = endpointCounters.getOrPut(endpoint) { EndpointStats() }
        stats.increment()
    }

    private fun getEndpointKey(request: okhttp3.Request): String {
        return "${request.method}:${request.url.encodedPath}"
    }

    private fun logRateLimitExceeded(endpoint: String, error: NetworkError, waitTime: Long?) {
        android.util.Log.w(tag, buildString {
            append("Rate limit exceeded\n")
            append("Endpoint: $endpoint\n")
            append("Max requests/second: $maxRequestsPerSecond\n")
            append("Max requests/minute: $maxRequestsPerMinute\n")
            append("Algorithm: ${if (useTokenBucket) "Token Bucket" else "Sliding Window"}\n")
            append("Wait time: ${waitTime ?: "N/A"}ms\n")
            append("Error: ${error.userMessage}")
        })
    }

    fun getEndpointStats(endpoint: String): EndpointStats? {
        return endpointCounters[endpoint]
    }

    fun getAllStats(): Map<String, EndpointStats> {
        return endpointCounters.toMap()
    }

    fun reset() {
        runBlocking {
            multiLevelRateLimiter.reset()
        }
        synchronized(requestTimestamps) {
            requestTimestamps.clear()
        }
        endpointCounters.clear()
    }
    
    /**
     * Gets current rate limiter status for monitoring.
     * 
     * SECURITY: Provides visibility into rate limiting behavior for debugging.
     * 
     * @return Pair of (perSecondTokens, perMinuteTokens) or null if using sliding window
     */
    suspend fun getRateLimiterStatus(): Pair<Int, Int>? {
        return if (useTokenBucket) {
            val status = multiLevelRateLimiter.getStatus()
            Pair(status[0], status[1])
        } else {
            null
        }
    }
    
    /**
     * Gets time to wait before next token is available.
     * 
     * SECURITY: Useful for intelligent retry logic.
     * 
     * @return Wait time in milliseconds, or null if using sliding window
     */
    suspend fun getTimeToNextToken(): Long? {
        return if (useTokenBucket) {
            // Get maximum wait time from both limiters
            val (secondLimiter, minuteLimiter) = Pair(
                RateLimiter.perSecond(maxRequestsPerSecond),
                RateLimiter.perMinute(maxRequestsPerMinute)
            )
            maxOf(
                secondLimiter.getTimeToNextToken(),
                minuteLimiter.getTimeToNextToken()
            )
        } else {
            null
        }
    }

    class EndpointStats {
        private val requestCount = AtomicInteger(0)
        private val lastRequestTime = AtomicLong(0)

        fun increment() {
            requestCount.incrementAndGet()
            lastRequestTime.set(System.currentTimeMillis())
        }

        fun getRequestCount(): Int = requestCount.get()

        fun getLastRequestTime(): Long = lastRequestTime.get()
    }
}
