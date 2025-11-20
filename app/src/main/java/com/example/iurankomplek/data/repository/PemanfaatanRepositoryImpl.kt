package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.NetworkUtils
import kotlinx.coroutines.delay
import retrofit2.Response

class PemanfaatanRepositoryImpl(
    private val apiService: ApiService
) : PemanfaatanRepository {
    private val maxRetries = 3
    
    override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
        // Check network connectivity before making API call
        // Note: We need to pass context to check network status
        // For now, we'll proceed with the call and handle failures with retries
        var currentRetry = 0
        var lastException: Exception? = null
        
        while (currentRetry <= maxRetries) {
            try {
                val response: Response<PemanfaatanResponse> = apiService.getPemanfaatan()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        return Result.success(responseBody)
                    } else {
                        return Result.failure(Exception("Response body is null"))
                    }
                } else {
                    throw Exception("API request failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                lastException = Exception(e)
                
                if (currentRetry < maxRetries) {
                    // Exponential backoff: delay increases with each retry
                    val delayMillis = (1000L * (currentRetry + 1))
                    delay(delayMillis)
                    currentRetry++
                } else {
                    break // Exit the loop after max retries
                }
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error occurred"))
    }
}