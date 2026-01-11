package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.utils.Constants
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class RequestPriorityInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val priority = determinePriority(request)
        val requestWithPriority = request.newBuilder()
            .tag(RequestPriority::class.java, priority)
            .header("X-Priority", priority.name)
            .build()

        return chain.proceed(requestWithPriority)
    }

    private fun determinePriority(request: Request): RequestPriority {
        val path = request.url.encodedPath

        return when {
            path.contains("/payments/") && path.contains("/confirm") -> RequestPriority.CRITICAL
            path.contains("/payments/initiate") -> RequestPriority.CRITICAL
            path.contains("/payments/") && path.contains("/status") -> RequestPriority.HIGH
            path.contains("/auth/") -> RequestPriority.CRITICAL
            path.contains("/login") -> RequestPriority.CRITICAL
            path.contains("/logout") -> RequestPriority.HIGH
            path.contains("/users") && request.method == "POST" -> RequestPriority.HIGH
            path.contains("/vendors") && request.method == "POST" -> RequestPriority.HIGH
            path.contains("/work-orders") && request.method == "POST" -> RequestPriority.HIGH
            path.contains("/messages") && request.method == "POST" -> RequestPriority.HIGH
            path.contains("/community-posts") && request.method == "POST" -> RequestPriority.HIGH
            path.contains("/health") -> RequestPriority.CRITICAL
            path.contains("/api/v1/users") -> RequestPriority.NORMAL
            path.contains("/api/v1/pemanfaatan") -> RequestPriority.NORMAL
            path.contains("/api/v1/vendors") -> RequestPriority.NORMAL
            path.contains("/api/v1/work-orders") -> RequestPriority.NORMAL
            path.contains("/api/v1/announcements") -> RequestPriority.LOW
            path.contains("/api/v1/messages") -> RequestPriority.NORMAL
            path.contains("/api/v1/community-posts") -> RequestPriority.NORMAL
            path.contains("/background-sync") -> RequestPriority.BACKGROUND
            path.contains("/analytics") -> RequestPriority.BACKGROUND
            else -> RequestPriority.NORMAL
        }
    }
}
