package com.example.iurankomplek.data.repository

object MessageRepositoryFactory {
    private var instance: MessageRepository? = null

    fun getInstance(): MessageRepository {
        return instance ?: synchronized(this) {
            instance ?: MessageRepositoryImpl(
                com.example.iurankomplek.network.ApiConfig.getApiService()
            ).also { instance = it }
        }
    }

    fun getMockInstance(): MessageRepository {
        return getInstance()
    }

    fun reset() {
        instance = null
    }
}
