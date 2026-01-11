package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.api.models.PemanfaatanResponse

interface PemanfaatanRepository {
    suspend fun getPemanfaatan(forceRefresh: Boolean = false): OperationResult<PemanfaatanResponse>
    suspend fun getCachedPemanfaatan(): OperationResult<PemanfaatanResponse>
    suspend fun clearCache(): OperationResult<Unit>
}