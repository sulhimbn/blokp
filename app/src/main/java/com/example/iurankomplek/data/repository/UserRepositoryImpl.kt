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
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : UserRepository, BaseRepository() {

    override suspend fun getUsers(forceRefresh: Boolean): OperationResult<UserResponse> {
        return try {
            if (!forceRefresh) {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                if (usersWithFinancials.isNotEmpty()) {
                    val dtoList = EntityMapper.toLegacyDtoList(usersWithFinancials)
                    val userResponse = UserResponse(dtoList)
                    return OperationResult.Success(userResponse)
                }
            }

            val result = executeWithCircuitBreakerV1 { apiService.getUsers() }
            if (result is OperationResult.Success) {
                saveUsersToCache(result.data)
            }
            result
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun getCachedUsers(): OperationResult<UserResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val dtoList = EntityMapper.toLegacyDtoList(usersWithFinancials)
            val userResponse = UserResponse(dtoList)
            OperationResult.Success(userResponse)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache(): OperationResult<Unit> {
        return try {
            CacheManager.getUserDao().deleteAll()
            CacheManager.getFinancialRecordDao().deleteAll()
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    private suspend fun saveUsersToCache(response: UserResponse) {
        val userFinancialPairs = EntityMapper.fromLegacyDtoList(response.data)
        com.example.iurankomplek.data.cache.CacheHelper.saveEntityWithFinancialRecords(
            userFinancialPairs
        )
    }
}