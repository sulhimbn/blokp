package com.example.iurankomplek.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.security.SecureRandom
import kotlin.math.abs

object RequestIdGenerator {
    private val SECURE_RANDOM = SecureRandom()

    fun generate(): String {
        val timestamp = System.currentTimeMillis()
        val random = SECURE_RANDOM.nextInt(10000)
        return "${timestamp}-${abs(random)}"
    }
}

class RequestIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = RequestIdGenerator.generate()

        val requestWithId = request.newBuilder()
            .header("X-Request-ID", requestId)
            .tag(String::class.java, requestId)
            .build()

        return chain.proceed(requestWithId)
    }
}
