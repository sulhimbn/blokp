package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.cache.cacheFirstStrategy
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import kotlinx.coroutines.flow.first
import kotlin.math.pow
import retrofit2.HttpException

class PemanfaatanRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : PemanfaatanRepository {
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    private val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES

    override suspend fun getPemanfaatan(forceRefresh: Boolean): Result<PemanfaatanResponse> {
        return cacheFirstStrategy(
            getFromCache = {
                val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
                val dataItemList = usersWithFinancials.map { EntityMapper.toLegacyDto(it) }
                val pemanfaatanResponse = PemanfaatanResponse(dataItemList)
                if (dataItemList.isEmpty()) null else pemanfaatanResponse
            },
            getFromNetwork = {
                circuitBreaker.execute {
                    com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                        apiCall = { apiService.getPemanfaatan() },
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
                savePemanfaatanToCache(response)
            },
            forceRefresh = forceRefresh
        )
    }
    
    override suspend fun getCachedPemanfaatan(): Result<PemanfaatanResponse> {
        return try {
            val usersWithFinancials = CacheManager.getUserDao().getAllUsersWithFinancialRecords().first()
            val dataItemList = usersWithFinancials.map { EntityMapper.toLegacyDto(it) }
            val pemanfaatanResponse = PemanfaatanResponse(dataItemList)
            Result.success(pemanfaatanResponse)
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
        val userDao = CacheManager.getUserDao()
        val financialRecordDao = CacheManager.getFinancialRecordDao()

        if (userFinancialPairs.isEmpty()) {
            return
        }

        val now = java.util.Date()

        val emails = userFinancialPairs.map { it.first.email }
        val existingUsers = userDao.getUsersByEmails(emails)
        val userMap = existingUsers.associateBy { it.email }

        val usersToInsert = mutableListOf<UserEntity>()
        val usersToUpdate = mutableListOf<UserEntity>()
        val userIdToFinancialMap = mutableMapOf<Long, FinancialRecordEntity>()

        userFinancialPairs.forEach { (user, financial) ->
            val existingUser = userMap[user.email]
            if (existingUser != null) {
                val userId = existingUser.id
                usersToUpdate.add(user.copy(id = userId, updatedAt = now))
                userIdToFinancialMap[userId] = financial
            } else {
                usersToInsert.add(user)
            }
        }

        if (usersToInsert.isNotEmpty()) {
            val insertedIds = userDao.insertAll(usersToInsert)
            usersToInsert.forEachIndexed { index, user ->
                userIdToFinancialMap[insertedIds[index]] = userFinancialPairs[index].second
            }
        }

        if (usersToUpdate.isNotEmpty()) {
            userDao.updateAll(usersToUpdate)
        }

        val userIds = userIdToFinancialMap.keys.toList()
        val existingFinancials = financialRecordDao.getFinancialRecordsByUserIds(userIds)
        val financialMap = existingFinancials.associateBy { it.userId }

        val financialsToInsert = mutableListOf<FinancialRecordEntity>()
        val financialsToUpdate = mutableListOf<FinancialRecordEntity>()

        userIdToFinancialMap.forEach { (userId, financial) ->
            val existingFinancial = financialMap[userId]
            if (existingFinancial != null) {
                financialsToUpdate.add(financial.copy(
                    id = existingFinancial.id,
                    userId = userId,
                    updatedAt = now
                ))
            } else {
                financialsToInsert.add(financial.copy(userId = userId, updatedAt = now))
            }
        }

        if (financialsToInsert.isNotEmpty()) {
            financialRecordDao.insertAll(financialsToInsert)
        }

        if (financialsToUpdate.isNotEmpty()) {
            financialRecordDao.updateAll(financialsToUpdate)
        }
    }
}