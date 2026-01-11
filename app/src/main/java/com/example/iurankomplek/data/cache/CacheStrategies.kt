package com.example.iurankomplek.data.cache
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import java.util.Date

data class CacheResult<T>(
    val data: T?,
    val isFromCache: Boolean,
    val isFresh: Boolean
)

sealed class CacheError {
    data class NetworkError(val exception: Exception) : CacheError()
    data class DatabaseError(val exception: Exception) : CacheError()
    data class ValidationError(val message: String) : CacheError()
    object EmptyCache : CacheError()
}

suspend fun <T> cacheFirstStrategy(
    getFromCache: suspend () -> T?,
    getFromNetwork: suspend () -> T,
    isCacheFresh: suspend (T) -> Boolean = { true },
    saveToCache: suspend (T) -> Unit = {},
    forceRefresh: Boolean = false
): Result<T> {
    return try {
        if (!forceRefresh) {
            val cachedData = getFromCache()
            if (cachedData != null && isCacheFresh(cachedData)) {
                return Result.success(cachedData)
            }
        }
        
        val networkData = getFromNetwork()
        saveToCache(networkData)
        Result.success(networkData)
    } catch (e: Exception) {
        try {
            val cachedData = getFromCache()
            if (cachedData != null) {
                return Result.success(cachedData)
            }
        } catch (cacheEx: Exception) {
        }
        Result.failure(e)
    }
}

suspend fun <T> networkFirstStrategy(
    getFromNetwork: suspend () -> T,
    saveToCache: suspend (T) -> Unit = {},
    getFromCache: suspend () -> T? = { null }
): Result<T> {
    return try {
        val networkData = getFromNetwork()
        saveToCache(networkData)
        Result.success(networkData)
    } catch (e: Exception) {
        try {
            val cachedData = getFromCache()
            if (cachedData != null) {
                return Result.success(cachedData)
            }
        } catch (cacheEx: Exception) {
        }
        Result.failure(e)
    }
}