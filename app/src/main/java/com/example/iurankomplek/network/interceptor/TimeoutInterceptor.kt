package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

enum class TimeoutProfile {
    FAST,
    NORMAL,
    SLOW
}

object TimeoutProfileConfig {
    fun getTimeoutMs(profile: TimeoutProfile): Long {
        return when (profile) {
            TimeoutProfile.FAST -> Constants.Network.FAST_TIMEOUT_MS
            TimeoutProfile.NORMAL -> Constants.Network.NORMAL_TIMEOUT_MS
            TimeoutProfile.SLOW -> Constants.Network.SLOW_TIMEOUT_MS
        }
    }

    fun getTimeoutForPath(path: String): TimeoutProfile {
        return when {
            path.contains("/health") -> TimeoutProfile.FAST
            path.contains("/status") -> TimeoutProfile.FAST
            path.contains("/payments/initiate") -> TimeoutProfile.SLOW
            path.contains("/payments") -> TimeoutProfile.NORMAL
            path.contains("/vendors") -> TimeoutProfile.NORMAL
            path.contains("/work-orders") -> TimeoutProfile.NORMAL
            path.contains("/announcements") -> TimeoutProfile.NORMAL
            path.contains("/messages") -> TimeoutProfile.NORMAL
            path.contains("/community-posts") -> TimeoutProfile.NORMAL
            path.contains("/users") -> TimeoutProfile.NORMAL
            path.contains("/pemanfaatan") -> TimeoutProfile.NORMAL
            else -> TimeoutProfile.NORMAL
        }
    }
}

class TimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val profile = TimeoutProfileConfig.getTimeoutForPath(path)
        val timeoutMs = TimeoutProfileConfig.getTimeoutMs(profile)
        val timeoutSeconds = timeoutMs / 1000L

        return chain.withReadTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .withWriteTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .proceed(request)
    }
}
