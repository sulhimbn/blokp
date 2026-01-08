package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.model.CreateCommunityPostRequest
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class CommunityPostRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : CommunityPostRepository {
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    private val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES
    private val cache = ConcurrentHashMap<String, CommunityPost>()

    override suspend fun getCommunityPosts(forceRefresh: Boolean): Result<List<CommunityPost>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return Result.success(cache.values.toList())
        }

        return executeWithCircuitBreaker { apiService.getCommunityPosts() }
    }

    override suspend fun createCommunityPost(
        authorId: String,
        title: String,
        content: String,
        category: String
    ): Result<CommunityPost> {
        val request = CreateCommunityPostRequest(
            authorId = authorId,
            title = title,
            content = content,
            category = category
        )
        return executeWithCircuitBreaker {
            apiService.createCommunityPost(request)
        }
    }

    override suspend fun getCachedCommunityPosts(): Result<List<CommunityPost>> {
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
            )
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> Result.success(circuitBreakerResult.value)
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(com.example.iurankomplek.network.model.NetworkError.CircuitBreakerError())
        }
    }
}
