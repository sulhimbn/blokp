package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiService
import retrofit2.Response

class PemanfaatanRepositoryImpl(
    private val apiService: ApiService
) : PemanfaatanRepository {
    override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
        return try {
            val response: Response<PemanfaatanResponse> = apiService.getPemanfaatan()
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Result.success(responseBody)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("API request failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}