package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.cache.cacheFirstStrategy
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import kotlinx.coroutines.flow.first
import kotlin.math.pow
import retrofit2.HttpException

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
                withCircuitBreaker { apiService.getUsers() }
                    .getOrThrow()
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
    
    private suspend fun <T : Any> withCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            var currentRetry = 0
            var lastException: Exception? = null
            
            while (currentRetry <= maxRetries) {
                try {
                    val response = apiCall()
                    if (response.isSuccessful) {
                        response.body()?.let { return@execute it }
                            ?: throw Exception("Response body is null")
                    } else {
                        val isRetryable = isRetryableError(response.code())
                        if (currentRetry < maxRetries && isRetryable) {
                            val delayMillis = calculateDelay(currentRetry + 1)
                            kotlinx.coroutines.delay(delayMillis)
                            currentRetry++
                        } else {
                            throw HttpException(response)
                        }
                    }
                } catch (e: NetworkError) {
                    lastException = e
                    if (shouldRetryOnNetworkError(e, currentRetry, maxRetries)) {
                        val delayMillis = calculateDelay(currentRetry + 1)
                        kotlinx.coroutines.delay(delayMillis)
                        currentRetry++
                    } else {
                        break
                    }
                } catch (e: Exception) {
                    lastException = e
                    if (shouldRetryOnException(e, currentRetry, maxRetries)) {
                        val delayMillis = calculateDelay(currentRetry + 1)
                        kotlinx.coroutines.delay(delayMillis)
                        currentRetry++
                    } else {
                        break
                    }
                }
            }
            
            throw lastException ?: Exception("Unknown error occurred")
        }
        
        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> Result.success(circuitBreakerResult.value)
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(NetworkError.CircuitBreakerError())
        }
    }
    
    private fun isRetryableError(httpCode: Int): Boolean {
        return httpCode in 408..429 || httpCode / 100 == 5
    }
    
    private fun shouldRetryOnNetworkError(error: NetworkError, currentRetry: Int, maxRetries: Int): Boolean {
        if (currentRetry >= maxRetries) return false
        
        return when (error) {
            is NetworkError.TimeoutError,
            is NetworkError.ConnectionError -> true
            is NetworkError.HttpError -> {
                error.httpCode in listOf(408, 429) || error.httpCode / 100 == 5
            }
            else -> false
        }
    }
    
    private fun shouldRetryOnException(e: Exception, currentRetry: Int, maxRetries: Int): Boolean {
        if (currentRetry >= maxRetries) return false
        
        return when (e) {
            is java.net.SocketTimeoutException,
            is java.net.UnknownHostException,
            is javax.net.ssl.SSLException -> true
            else -> false
        }
    }
    
    private fun calculateDelay(currentRetry: Int): Long {
        val exponentialDelay = (com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS * 2.0.pow((currentRetry - 1).toDouble())).toLong()
        val jitter = (kotlin.random.Random.nextDouble() * com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS).toLong()
        return minOf(exponentialDelay + jitter, com.example.iurankomplek.utils.Constants.Network.MAX_RETRY_DELAY_MS)
    }
}