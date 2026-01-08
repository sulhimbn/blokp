package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.cache.CacheHelper
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.data.repository.cache.CacheStrategy
import com.example.iurankomplek.data.repository.cache.DatabaseCacheStrategy
import com.example.iurankomplek.data.api.models.UserResponse
import kotlinx.coroutines.flow.first

/**
 * Refactored UserRepository using unified repository pattern and ApiServiceV1.
 * Extends BaseRepositoryV2 for consistent error handling and caching.
 * Uses ApiServiceV1 with API versioning and ApiResponse<T> unwrapping.
 *
 * BEFORE (Module 88):
 * - Used legacy ApiService (no API versioning)
 * - No ApiResponse<T> unwrapping
 * - No request ID tracking
 *
 * AFTER (Phase 2 - API Migration):
 * - Uses ApiServiceV1 (api/v1 endpoints)
 * - ApiResponse<T> unwrapping with error handling
 * - Request ID tracking (X-Request-ID header)
 * - Consistent with API standardization (Module 60)
 */
class UserRepositoryV2(
    private val apiServiceV1: com.example.iurankomplek.network.ApiServiceV1
) : UserRepository, BaseRepositoryV2<UserResponse>() {

    override val cacheStrategy: CacheStrategy<UserResponse> =
        DatabaseCacheStrategy(
            cacheGetter = {
                val usersWithFinancials = CacheManager.getUserDao()
                    .getAllUsersWithFinancialRecords().first()
                val dataItemList = usersWithFinancials.map { EntityMapper.toLegacyDto(it) }
                if (dataItemList.isEmpty()) null else UserResponse(dataItemList)
            }
        )

    override suspend fun getUsers(forceRefresh: Boolean): Result<UserResponse> {
        return fetchWithCache(
            cacheKey = "users",
            forceRefresh = forceRefresh,
            fromNetwork = {
                val response = apiServiceV1.getUsers()
                if (!response.isSuccessful || response.body() == null) {
                    throw Exception("API request failed: ${response.code()}")
                }
                val apiResponse = response.body()!!
                if (apiResponse.error != null) {
                    throw ApiException(
                        message = apiResponse.error.message ?: "Unknown API error",
                        code = apiResponse.error.code,
                        requestId = apiResponse.request_id
                    )
                }
                apiResponse.data ?: UserResponse(emptyList())
            }
        )
    }

    override suspend fun getCachedUsers(): Result<UserResponse> {
        return try {
            val cached = cacheStrategy.get(null)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No cached users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            val database = CacheManager.getDatabase()
            database.withTransaction {
                CacheManager.getUserDao().deleteAll()
                CacheManager.getFinancialRecordDao().deleteAll()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveUsersToCache(response: UserResponse) {
        val userFinancialPairs = EntityMapper.fromLegacyDtoList(response.data)
        CacheHelper.saveEntityWithFinancialRecords(userFinancialPairs)
    }
}

class ApiException(
    val message: String,
    val code: String? = null,
    val requestId: String? = null
) : Exception(message)
