package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.PemanfaatanResponse

interface PemanfaatanRepository {
    suspend fun getPemanfaatan(): Result<PemanfaatanResponse>
}