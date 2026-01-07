package com.example.iurankomplek.data.repository

object CommunityPostRepositoryFactory {
    private var instance: CommunityPostRepository? = null

    fun getInstance(): CommunityPostRepository {
        return instance ?: synchronized(this) {
            instance ?: CommunityPostRepositoryImpl(
                com.example.iurankomplek.network.ApiConfig.getApiService()
            ).also { instance = it }
        }
    }

    fun getMockInstance(): CommunityPostRepository {
        return getInstance()
    }

    fun reset() {
        instance = null
    }
}
