package com.example.iurankomplek.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object  ApiConfig {
    private const val BASE_URL = "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/\n" +
            "\n"
    fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}