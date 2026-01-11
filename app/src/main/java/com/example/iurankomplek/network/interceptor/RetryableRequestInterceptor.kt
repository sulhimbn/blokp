package com.example.iurankomplek.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

object RetryableRequestTag

class RetryableRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val isRetryable = isRetryable(request)

        return if (isRetryable) {
            val requestWithTag = request.newBuilder()
                .tag(Boolean::class.java, true)
                .build()
            chain.proceed(requestWithTag)
        } else {
            chain.proceed(request)
        }
    }

    private fun isRetryable(request: okhttp3.Request): Boolean {
        val method = request.method.uppercase()

        return when (method) {
            "GET", "HEAD", "OPTIONS" -> true
            "POST", "PUT", "DELETE", "PATCH" -> {
                val retryHeader = request.header("X-Retryable")?.lowercase()
                retryHeader == "true"
            }
            else -> false
        }
    }
}
