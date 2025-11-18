package com.example.iurankomplek.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    // Use mock API in debug mode or when running in Docker
    private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    private const val BASE_URL = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
    }
    
    fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}