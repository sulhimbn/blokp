package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.ErrorHandler

class PemanfaatanRepositoryImpl(
    private val apiService: ApiService
) : BaseNetworkRepository(), PemanfaatanRepository {
    
    override val errorHandler = ErrorHandler()
    
    companion object {
        private const val CACHE_KEY_PEMANFAATAN = "pemanfaatan_list"
    }

    override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
        return getCachedOrNetwork(
            cacheKey = CACHE_KEY_PEMANFAATAN,
            ttlMs = 5 * 60 * 1000L // 5 minutes TTL
        ) {
            executeWithRetry(
                operation = { apiService.getPemanfaatan() },
                transform = { it }
            )
        }
    }
}
