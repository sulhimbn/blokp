package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.cache.cacheFirstStrategy
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.network.resilience.CircuitBreakerState
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : UserRepository {
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    private val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES
    
    override suspend fun getUsers(forceRefresh: Boolean): Result<UserResponse> {
        return cacheFirstStrategy(
            getFromCache = {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                val mappingResult = EntityMapper.toLegacyDtoList(usersWithFinancials)
                if (mappingResult.isSuccess) {
                    val userResponse = UserResponse(mappingResult.getOrThrow())
                    if (mappingResult.getOrThrow().isEmpty()) null else userResponse
                } else {
                    null
                }
            },
            getFromNetwork = {
                val circuitBreakerResult = circuitBreaker.execute {
                    com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                        apiCall = { apiService.getUsers() },
                        maxRetries = maxRetries
                    )
                }
                when (circuitBreakerResult) {
                    is CircuitBreakerResult.Success -> circuitBreakerResult.value
                    is CircuitBreakerResult.Failure -> throw circuitBreakerResult.exception
                    is CircuitBreakerResult.CircuitOpen -> throw NetworkError.CircuitBreakerError()
                }
            },
            isCacheFresh = { response ->
                if (response.data.isNotEmpty()) {
                    val latestUpdate = CacheManager.getUserDao().getLatestUpdatedAt()
                    if (latestUpdate != null) {
                        CacheManager.isCacheFresh(latestUpdate.time)
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
            saveToCache = { response ->
                saveUsersToCache(response)
            },
            forceRefresh = forceRefresh
        )
    }
    
    override suspend fun getCachedUsers(): Result<UserResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val mappingResult = EntityMapper.toLegacyDtoList(usersWithFinancials)
            if (mappingResult.isSuccess) {
                val userResponse = UserResponse(mappingResult.getOrThrow())
                Result.success(userResponse)
            } else {
                Result.failure(mappingResult.exceptionOrNull() ?: Exception("Mapping failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearCache(): Result<Unit> {
        return try {
            CacheManager.getUserDao().deleteAll()
            CacheManager.getFinancialRecordDao().deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun saveUsersToCache(response: UserResponse) {
        val userFinancialPairs = EntityMapper.fromLegacyDtoList(response.data)
        com.example.iurankomplek.data.cache.CacheHelper.saveEntityWithFinancialRecords(
            userFinancialPairs
        )
    }
}