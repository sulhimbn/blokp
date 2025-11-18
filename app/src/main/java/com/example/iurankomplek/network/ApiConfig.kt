package com.example.iurankomplek.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    // Use mock API in debug mode or when running in Docker
    private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    
    // Base URL for users endpoint
    private const val BASE_URL_USERS = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/users"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/users"  // Assuming real API supports this path
    }
    
    // Base URL for pemanfaatan endpoint
    private const val BASE_URL_PEMANFAATAN = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/pemanfaatan"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/pemanfaatan"  // Assuming real API supports this path
    }

    private val userRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_USERS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val pemanfaatanRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PEMANFAATAN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getUserApiService(): ApiService = userRetrofit.create(ApiService::class.java)
    fun getPemanfaatanApiService(): ApiService = pemanfaatanRetrofit.create(ApiService::class.java)
}