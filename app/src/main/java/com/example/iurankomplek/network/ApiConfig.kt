package com.example.iurankomplek.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.iurankomplek.BuildConfig

object ApiConfig {
    // Use mock API in debug mode or when running in Docker
    private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    private val BASE_URL = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
    }
    
    fun getApiService(): ApiService {
        val okHttpClient = if (!USE_MOCK_API) {
            // Use secure client for production
            SecurityConfig.getSecureOkHttpClient()
        } else {
            // For debug/mock, use basic client but log warning
            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(Constants.Network.CONNECT_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(Constants.Network.READ_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
            
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