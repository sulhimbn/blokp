package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.cache.cacheFirstStrategy
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
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
                val dataItemList = usersWithFinancials.map { EntityMapper.toLegacyDto(it) }
                val userResponse = UserResponse(dataItemList)
                if (dataItemList.isEmpty()) null else userResponse
            },
            getFromNetwork = {
                circuitBreaker.execute {
                    com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                        apiCall = { apiService.getUsers() },
                        maxRetries = maxRetries
                    )
                }.getOrThrow()
            },
            isCacheFresh = { response ->
                if (response.data.isNotEmpty()) {
                    val usersWithFinancials = CacheManager.getUserDao()
                        .getAllUsersWithFinancialRecords()
                        .first()
                    if (usersWithFinancials.isNotEmpty()) {
                        val latestUpdate = usersWithFinancials
                            .maxOfOrNull { it.user.updatedAt.time }
                            ?: return@cacheFirstStrategy false
                        CacheManager.isCacheFresh(latestUpdate)
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
            val dataItemList = usersWithFinancials.map { EntityMapper.toLegacyDto(it) }
            val userResponse = UserResponse(dataItemList)
            Result.success(userResponse)
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