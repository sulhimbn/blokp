package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.ErrorHandler

class PemanfaatanRepositoryImpl(
    private val apiService: ApiService
) : BaseNetworkRepository(), PemanfaatanRepository {
    
    override val errorHandler = ErrorHandler()
    
    override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
        return executeWithRetry(
            operation = { apiService.getPemanfaatan() },
            transform = { it }
        )
    }
}