package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.cache.cacheFirstStrategy
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.network.resilience.CircuitBreakerState
import com.example.iurankomplek.utils.Result
import kotlinx.coroutines.flow.first
import kotlin.math.pow
import retrofit2.HttpException

class PemanfaatanRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : PemanfaatanRepository, BaseRepository() {

    override suspend fun getPemanfaatan(forceRefresh: Boolean): Result<PemanfaatanResponse> {
        return try {
            if (!forceRefresh) {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                if (usersWithFinancials.isNotEmpty()) {
                    val pemanfaatanResponse = PemanfaatanResponse(EntityMapper.toLegacyDtoList(usersWithFinancials).getOrThrow())
                    return Result.Success(pemanfaatanResponse)
                }
            }

            val result = executeWithCircuitBreaker { apiService.getPemanfaatan() }
            if (result is Result.Success) {
                savePemanfaatanToCache(result.data)
            }
            result
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun getCachedPemanfaatan(): Result<PemanfaatanResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val pemanfaatanResponse = PemanfaatanResponse(EntityMapper.toLegacyDtoList(usersWithFinancials).getOrThrow())
            Result.Success(pemanfaatanResponse)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            CacheManager.getFinancialRecordDao().deleteAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }
    
    private suspend fun savePemanfaatanToCache(response: PemanfaatanResponse) {
        val userFinancialPairs = EntityMapper.fromLegacyDtoList(response.data)
        com.example.iurankomplek.data.cache.CacheHelper.saveEntityWithFinancialRecords(
            userFinancialPairs
        )
    }
}