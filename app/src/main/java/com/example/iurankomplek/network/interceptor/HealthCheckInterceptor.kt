package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.health.IntegrationHealthMonitor
import com.example.iurankomplek.utils.Constants
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class HealthCheckInterceptor(
    private val healthMonitor: IntegrationHealthMonitor = IntegrationHealthMonitor.getInstance(),
    private val enableLogging: Boolean = false
) : Interceptor {
    
    private val TAG = "HealthCheckInterceptor"
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = getEndpointKey(request)
        val startTime = System.currentTimeMillis()
        
        return try {
            val response = chain.proceed(request)
            val responseTimeMs = System.currentTimeMillis() - startTime
            val success = response.isSuccessful
            val httpCode = response.code

            recordHealth(endpoint, responseTimeMs, success, httpCode)

            if (enableLogging) {
                logRequest(endpoint, responseTimeMs, success, httpCode)
            }

            when {
                httpCode == 429 -> {
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        healthMonitor.recordRateLimitExceeded(endpoint, getRequestCount())
                    }
                }
                httpCode >= 500 -> {
                    if (httpCode == 503) {
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            healthMonitor.recordCircuitBreakerOpen("api_service")
                        }
                    }
                }
            }

            response
        } catch (e: IOException) {
            val responseTimeMs = System.currentTimeMillis() - startTime
            recordHealth(endpoint, responseTimeMs, false, null)

            if (enableLogging) {
                logError(endpoint, responseTimeMs, e)
            }

            throw e
        }
    }
    
    private fun recordHealth(endpoint: String, responseTimeMs: Long, success: Boolean, httpCode: Int?) {
        if (isHealthEndpoint(endpoint)) {
            return
        }

        if (!success) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                healthMonitor.recordRetry(endpoint)
            }
        }
    }
    
    private fun isHealthEndpoint(endpoint: String): Boolean {
        return endpoint.contains("/health")
    }
    
    private fun getEndpointKey(request: Request): String {
        return "${request.method}:${request.url.encodedPath}"
    }
    
    private fun getRequestCount(): Int {
        var requestCount = 0
        kotlinx.coroutines.runBlocking {
            requestCount = healthMonitor.getDetailedHealthReport().metrics.requestMetrics.totalRequests
        }
        return requestCount
    }
    
    private fun logRequest(endpoint: String, responseTimeMs: Long, success: Boolean, httpCode: Int) {
        android.util.Log.d(TAG, String.format(
            "Request: %s, Time: %dms, Success: %s, Status: %d",
            endpoint, responseTimeMs, success, httpCode
        ))
    }
    
    private fun logError(endpoint: String, responseTimeMs: Long, error: IOException) {
        android.util.Log.e(TAG, String.format(
            "Request Failed: %s, Time: %dms, Error: %s",
            endpoint, responseTimeMs, error.message
        ))
    }
}
