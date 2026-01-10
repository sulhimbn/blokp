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
import kotlinx.coroutines.flow.first
import kotlin.math.pow
import retrofit2.HttpException

class PemanfaatanRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : PemanfaatanRepository, BaseRepository() {

    override suspend fun getPemanfaatan(forceRefresh: Boolean): OperationResult<PemanfaatanResponse> {
        return try {
            if (!forceRefresh) {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                if (usersWithFinancials.isNotEmpty()) {
                    val dtoListResult = EntityMapper.toLegacyDtoList(usersWithFinancials)
                    val pemanfaatanResponse = PemanfaatanResponse(dtoListResult.getOrThrow())
                    return OperationResult.Success(pemanfaatanResponse)
                }
            }

            val result = executeWithCircuitBreaker { apiService.getPemanfaatan() }
            if (result is OperationResult.Success) {
                savePemanfaatanToCache(result.data)
            }
            result
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun getCachedPemanfaatan(): OperationResult<PemanfaatanResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val dtoListResult = EntityMapper.toLegacyDtoList(usersWithFinancials)
            val pemanfaatanResponse = PemanfaatanResponse(dtoListResult.getOrThrow())
            OperationResult.Success(pemanfaatanResponse)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache(): OperationResult<Unit> {
        return try {
            CacheManager.getFinancialRecordDao().deleteAll()
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    private suspend fun savePemanfaatanToCache(response: PemanfaatanResponse) {
        val userFinancialPairs = EntityMapper.fromLegacyDtoList(response.data)
        com.example.iurankomplek.data.cache.CacheHelper.saveEntityWithFinancialRecords(
            userFinancialPairs
        )
    }
}