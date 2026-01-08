package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class AnnouncementRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : AnnouncementRepository {
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    private val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES
    private val cache = ConcurrentHashMap<String, Announcement>()

    override suspend fun getAnnouncements(forceRefresh: Boolean): Result<List<Announcement>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return Result.success(cache.values.toList())
        }

        return executeWithCircuitBreaker { apiService.getAnnouncements() }
    }

    override suspend fun getCachedAnnouncements(): Result<List<Announcement>> {
        return try {
            Result.success(cache.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            cache.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private suspend fun <T : Any> executeWithCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            ).getOrThrow()
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> Result.success(circuitBreakerResult.value)
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(com.example.iurankomplek.network.model.NetworkError.CircuitBreakerError())
        }
    }
}
