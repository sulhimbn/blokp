package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.PemanfaatanResponse

interface PemanfaatanRepository {
    suspend fun getPemanfaatan(forceRefresh: Boolean = false): Result<PemanfaatanResponse>
    suspend fun getCachedPemanfaatan(): Result<PemanfaatanResponse>
    suspend fun clearCache(): Result<Unit>
}