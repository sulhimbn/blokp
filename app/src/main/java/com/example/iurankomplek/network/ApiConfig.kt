package com.example.iurankomplek.network

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.iurankomplek.BuildConfig

object ApiConfig {
    // Use mock API in debug mode or when running in Docker
    private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    private const val BASE_URL = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
    }
    
    private fun getCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.apispreadsheets.com", "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0=")
            .build()
    }
    
    fun getApiService(): ApiService {
        val okHttpClientBuilder = OkHttpClient.Builder()
        
        // Only apply certificate pinning for production API (not mock API)
        if (!USE_MOCK_API) {
            okHttpClientBuilder.certificatePinner(getCertificatePinner())
        }
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}