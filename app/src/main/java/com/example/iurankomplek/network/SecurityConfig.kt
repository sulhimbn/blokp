package com.example.iurankomplek.network

import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor
import com.example.iurankomplek.BuildConfig

object SecurityConfig {
    private const val CERTIFICATE_PINNER = "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0=" // Add backup pin like: ;sha256/ACTUAL_BACKUP_CERT_PIN_HERE when available
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    
    fun getSecureOkHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add("api.apispreadsheets.com", CERTIFICATE_PINNER)
                    .build()
            )
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
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
                .build()
            
            chain.proceed(requestWithHeaders)
        }
    }
}