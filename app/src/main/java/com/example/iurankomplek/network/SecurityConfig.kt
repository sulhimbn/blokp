package com.example.iurankomplek.network

import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor
import com.example.iurankomplek.BuildConfig
import com.example.iurankomplek.utils.Constants

object SecurityConfig {
    
    fun getSecureOkHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add("api.apispreadsheets.com", Constants.Security.CERTIFICATE_PINNER)
                    .build()
            )
            .connectTimeout(Constants.Network.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.Network.READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(getSecurityInterceptor())
        
        // Add logging interceptor only for debug builds
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            clientBuilder.addInterceptor(loggingInterceptor)
        }
        
        return clientBuilder.build()
    }
    
    private fun getSecurityInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            // Add security headers
            val requestWithHeaders = originalRequest.newBuilder()
                .addHeader("X-Content-Type-Options", "nosniff")
                .addHeader("X-Frame-Options", "DENY")
                .addHeader("X-XSS-Protection", "1; mode=block")
                .addHeader("Referrer-Policy", "strict-origin-when-cross-origin")
                .addHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()")
                .build()

            chain.proceed(requestWithHeaders)
        }
    }
}