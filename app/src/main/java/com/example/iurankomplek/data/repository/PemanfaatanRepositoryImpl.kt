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
) : PemanfaatanRepository, BaseRepository {

    override suspend fun getPemanfaatan(forceRefresh: Boolean): Result<PemanfaatanResponse> {
        return cacheFirstStrategy(
            getFromCache = {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                val mappingResult = EntityMapper.toLegacyDtoList(usersWithFinancials)
                if (mappingResult.isSuccess) {
                    val pemanfaatanResponse = PemanfaatanResponse(mappingResult.getOrThrow())
                    if (mappingResult.getOrThrow().isEmpty()) null else pemanfaatanResponse
                } else {
                    null
                }
            },
            getFromNetwork = {
                val circuitBreakerResult = executeWithCircuitBreaker {
                    apiService.getPemanfaatan()
                }
                circuitBreakerResult.getOrThrow()
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
                savePemanfaatanToCache(response)
            },
            forceRefresh = forceRefresh
        )
    }
    
    override suspend fun getCachedPemanfaatan(): Result<PemanfaatanResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val mappingResult = EntityMapper.toLegacyDtoList(usersWithFinancials)
            if (mappingResult.isSuccess) {
                val pemanfaatanResponse = PemanfaatanResponse(mappingResult.getOrThrow())
                Result.success(pemanfaatanResponse)
            } else {
                Result.failure(mappingResult.exceptionOrNull() ?: Exception("Mapping failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearCache(): Result<Unit> {
        return try {
            CacheManager.getFinancialRecordDao().deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun savePemanfaatanToCache(response: PemanfaatanResponse) {
        val userFinancialPairs = EntityMapper.fromLegacyDtoList(response.data)
        com.example.iurankomplek.data.cache.CacheHelper.saveEntityWithFinancialRecords(
            userFinancialPairs
        )
    }
}