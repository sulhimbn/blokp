package com.example.iurankomplek.data.repository

import com.example.iurankomplek.network.ApiConfig

object UserRepositoryFactory {
    private var instance: UserRepository? = null

    fun getInstance(): UserRepository {
        return instance ?: synchronized(this) {
            instance ?: createInstance().also { instance = it }
        }
    }

    private fun createInstance(): UserRepository {
        val apiService = ApiConfig.getApiService()
        return UserRepositoryImpl(apiService)
    }
}
