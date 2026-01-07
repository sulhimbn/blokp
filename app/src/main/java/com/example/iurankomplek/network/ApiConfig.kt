package com.example.iurankomplek.network

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.iurankomplek.BuildConfig
import java.util.concurrent.TimeUnit

object ApiConfig {
    // Use mock API in debug mode or when running in Docker
    private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    private val BASE_URL = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
    }
    
    // Connection pool for efficient HTTP connection reuse
    private val connectionPool = ConnectionPool(
        5, // Max idle connections
        5, // Keep alive duration in minutes
        TimeUnit.MINUTES
    )
    
    // Cache Retrofit instances to avoid recreation
    @Volatile
    private var apiServiceInstance: ApiService? = null
    
    fun getApiService(): ApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: createApiService().also { apiServiceInstance = it }
        }
    }
    
    private fun createApiService(): ApiService {
        val okHttpClient = if (!USE_MOCK_API) {
            // Use secure client for production
            SecurityConfig.getSecureOkHttpClient()
                .newBuilder()
                .connectionPool(connectionPool)
                .build()
        } else {
            // For debug/mock, use basic client but log warning
            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(Constants.Network.CONNECT_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(Constants.Network.READ_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
                .connectionPool(connectionPool)
            
            // Add logging interceptor only for debug builds
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                }
                clientBuilder.addInterceptor(loggingInterceptor)
            }
            
            clientBuilder.build()
        }
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}