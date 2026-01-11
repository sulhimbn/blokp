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

    private val fallbackManager = FallbackManager<PemanfaatanResponse>(
        fallbackStrategy = CachedFinancialDataFallback(),
        config = FallbackConfig(enableFallback = true, fallbackTimeoutMs = 5000L, logFallbackUsage = true)
    )

    override suspend fun getPemanfaatan(forceRefresh: Boolean): OperationResult<PemanfaatanResponse> {
        return fallbackManager.executeWithFallback(
            primaryOperation = {
                if (!forceRefresh) {
                    if (CacheManager.isFinancialCacheFresh()) {
                        val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                        if (usersWithFinancials.isNotEmpty()) {
                            val dtoList = EntityMapper.toLegacyDtoList(usersWithFinancials)
                            val pemanfaatanResponse = PemanfaatanResponse(dtoList)
                            return@executeWithFallback OperationResult.Success(pemanfaatanResponse)
                        }
                    }
                }
                
                val result = executeWithCircuitBreakerV1 { apiService.getPemanfaatan() }
                if (result is OperationResult.Success) {
                    savePemanfaatanToCache(result.data)
                }
                result
            }
        )
    }

    override suspend fun getCachedPemanfaatan(): OperationResult<PemanfaatanResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val dtoList = EntityMapper.toLegacyDtoList(usersWithFinancials)
            val pemanfaatanResponse = PemanfaatanResponse(dtoList)
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

    private class CachedFinancialDataFallback : CachedDataFallback<PemanfaatanResponse>() {
        override suspend fun getCachedData(): PemanfaatanResponse? {
            return try {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                if (usersWithFinancials.isNotEmpty()) {
                    val dtoList = EntityMapper.toLegacyDtoList(usersWithFinancials)
                    PemanfaatanResponse(dtoList)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}