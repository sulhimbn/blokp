package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.model.ApiErrorCode
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.utils.Constants
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
    private val tag: String = "RateLimiterInterceptor"
) : Interceptor {

    private val requestTimestamps = ConcurrentLinkedQueue<Long>()
    private val endpointCounters = ConcurrentHashMap<String, EndpointStats>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = getEndpointKey(request)

        if (!checkRateLimit(endpoint)) {
            val error = NetworkError.HttpError(
                code = ApiErrorCode.RATE_LIMIT_EXCEEDED,
                userMessage = "Too many requests. Please slow down.",
                httpCode = 429,
                details = "Rate limit exceeded for endpoint: $endpoint"
            )
            if (enableLogging) {
                logRateLimitExceeded(endpoint, error)
            }
            throw error
        }

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
        val minInterval = 1000L / maxRequestsPerSecond

        return timeSinceLastRequest < minInterval
    }

    private fun updateEndpointStats(endpoint: String) {
        val stats = endpointCounters.getOrPut(endpoint) { EndpointStats() }
        stats.increment()
    }

    private fun getEndpointKey(request: okhttp3.Request): String {
        return "${request.method}:${request.url.encodedPath}"
    }

    private fun logRateLimitExceeded(endpoint: String, error: NetworkError) {
        android.util.Log.w(tag, buildString {
            append("Rate limit exceeded\n")
            append("Endpoint: $endpoint\n")
            append("Max requests/second: $maxRequestsPerSecond\n")
            append("Max requests/minute: $maxRequestsPerMinute\n")
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
        synchronized(requestTimestamps) {
            requestTimestamps.clear()
        }
        endpointCounters.clear()
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
