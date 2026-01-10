package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.utils.Result

interface PemanfaatanRepository {
    suspend fun getPemanfaatan(forceRefresh: Boolean = false): Result<PemanfaatanResponse>
    suspend fun getCachedPemanfaatan(): Result<PemanfaatanResponse>
    suspend fun clearCache(): Result<Unit>
}