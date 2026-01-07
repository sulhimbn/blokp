package com.example.iurankomplek.data.repository

import com.example.iurankomplek.network.ApiConfig

object VendorRepositoryFactory {
    private var instance: VendorRepository? = null

    fun getInstance(): VendorRepository {
        return instance ?: synchronized(this) {
            instance ?: createInstance().also { instance = it }
        }
    }

    private fun createInstance(): VendorRepository {
        val apiService = ApiConfig.getApiService()
        return VendorRepositoryImpl(apiService)
    }
}
