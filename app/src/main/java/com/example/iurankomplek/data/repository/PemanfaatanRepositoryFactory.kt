package com.example.iurankomplek.data.repository

import com.example.iurankomplek.network.ApiConfig

object PemanfaatanRepositoryFactory {
    private var instance: PemanfaatanRepository? = null

    fun getInstance(): PemanfaatanRepository {
        return instance ?: synchronized(this) {
            instance ?: createInstance().also { instance = it }
        }
    }

    private fun createInstance(): PemanfaatanRepository {
        val apiService = ApiConfig.getApiService()
        return PemanfaatanRepositoryImpl(apiService)
    }
}
