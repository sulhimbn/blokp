package com.example.iurankomplek.data.repository

object AnnouncementRepositoryFactory {
    private var instance: AnnouncementRepository? = null

    fun getInstance(): AnnouncementRepository {
        return instance ?: synchronized(this) {
            instance ?: AnnouncementRepositoryImpl(
                com.example.iurankomplek.network.ApiConfig.getApiService()
            ).also { instance = it }
        }
    }

    fun getMockInstance(): AnnouncementRepository {
        return getInstance()
    }

    fun reset() {
        instance = null
    }
}
