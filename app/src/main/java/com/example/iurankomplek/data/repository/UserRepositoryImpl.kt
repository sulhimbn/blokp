package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.network.resilience.CircuitBreakerState
import com.example.iurankomplek.utils.Result
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : UserRepository, BaseRepository() {

    override suspend fun getUsers(forceRefresh: Boolean): Result<UserResponse> {
        return try {
            if (!forceRefresh) {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                if (usersWithFinancials.isNotEmpty()) {
                    val userResponse = UserResponse(EntityMapper.toLegacyDtoList(usersWithFinancials).getOrThrow())
                    return Result.Success(userResponse)
                }
            }

            val result = executeWithCircuitBreakerV1 { apiService.getUsers() }
            if (result is Result.Success) {
                saveUsersToCache(result.data)
            }
            result
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun getCachedUsers(): Result<UserResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val userResponse = UserResponse(EntityMapper.toLegacyDtoList(usersWithFinancials).getOrThrow())
            Result.Success(userResponse)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            CacheManager.getUserDao().deleteAll()
            CacheManager.getFinancialRecordDao().deleteAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }

    private suspend fun saveUsersToCache(response: UserResponse) {
        val userFinancialPairs = EntityMapper.fromLegacyDtoList(response.data)
        com.example.iurankomplek.data.cache.CacheHelper.saveEntityWithFinancialRecords(
            userFinancialPairs
        )
    }
}