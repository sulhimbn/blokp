package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response
import java.security.SecureRandom

object IdempotencyKeyGenerator {
    private val SECURE_RANDOM = SecureRandom()

    fun generate(): String {
        val timestamp = System.currentTimeMillis()
        val random = SECURE_RANDOM.nextInt()
        return "idk_${timestamp}_${kotlin.math.abs(random)}"
    }
}

class IdempotencyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.method != "GET") {
            val idempotencyKey = IdempotencyKeyGenerator.generate()
            val requestWithIdempotency = request.newBuilder()
                .header("X-Idempotency-Key", idempotencyKey)
                .tag(String::class.java, idempotencyKey)
                .build()

            return chain.proceed(requestWithIdempotency)
        }

        return chain.proceed(request)
    }
}
