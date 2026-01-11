package com.example.iurankomplek.data.repository

import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.withTimeoutOrNull

sealed class FallbackResult<out T> {
    data class Success<T>(val value: T) : FallbackResult<T>()
    data class FallbackUsed<T>(val value: T, val reason: FallbackReason) : FallbackResult<T>()
    data class Failed(val error: Throwable) : FallbackResult<Nothing>()
}

enum class FallbackReason {
    API_FAILURE,
    CIRCUIT_BREAKER_OPEN,
    TIMEOUT,
    NETWORK_ERROR,
    SERVICE_UNAVAILABLE,
    RATE_LIMIT_EXCEEDED,
    UNKNOWN_ERROR
}

interface FallbackStrategy<T> {
    suspend fun getFallback(): T?
    val isEnabled: Boolean
    val priority: Int
}

data class FallbackConfig(
    val enableFallback: Boolean = true,
    val fallbackTimeoutMs: Long = 5000L,
    val logFallbackUsage: Boolean = true
)

class FallbackManager<T>(
    private val fallbackStrategy: FallbackStrategy<T>?,
    private val config: FallbackConfig = FallbackConfig()
) {
    suspend fun executeWithFallback(
        primaryOperation: suspend () -> OperationResult<T>,
        fallbackOperation: suspend () -> T? = { fallbackStrategy?.getFallback() }
    ): OperationResult<T> {
        return try {
            val result = primaryOperation()
            
            if (result is OperationResult.Success || result is OperationResult.Error) {
                return result
            }
            
            val fallbackResult = tryGetFallback(fallbackOperation)
            
            when (fallbackResult) {
                is FallbackResult.Success -> {
                    if (config.logFallbackUsage) {
                        logFallback("Fallback provided data", FallbackReason.API_FAILURE)
                    }
                    OperationResult.Success(fallbackResult.value)
                }
                is FallbackResult.FallbackUsed -> {
                    if (config.logFallbackUsage) {
                        logFallback("Fallback provided data", fallbackResult.reason)
                    }
                    OperationResult.Success(fallbackResult.value)
                }
                is FallbackResult.Failed -> {
                    OperationResult.Error(fallbackResult.error, "All data sources unavailable")
                }
            }
        } catch (e: Exception) {
            val fallbackResult = tryGetFallback(fallbackOperation)
            
            when (fallbackResult) {
                is FallbackResult.Success -> {
                    if (config.logFallbackUsage) {
                        logFallback("Fallback used after exception", FallbackReason.UNKNOWN_ERROR)
                    }
                    OperationResult.Success(fallbackResult.value)
                }
                is FallbackResult.FallbackUsed -> {
                    if (config.logFallbackUsage) {
                        logFallback("Fallback used after exception", fallbackResult.reason)
                    }
                    OperationResult.Success(fallbackResult.value)
                }
                is FallbackResult.Failed -> {
                    OperationResult.Error(e, "Primary operation failed and fallback unavailable")
                }
            }
        }
    }
    
    private suspend fun <R> tryGetFallback(
        fallbackOperation: suspend () -> R?
    ): FallbackResult<R> {
        if (!config.enableFallback || fallbackOperation == null) {
            return FallbackResult.Failed(
                Exception("Fallback disabled or no fallback provided")
            )
        }
        
        return try {
            val fallbackData = withTimeoutOrNull(config.fallbackTimeoutMs) {
                fallbackOperation()
            }
            
            if (fallbackData != null) {
                FallbackResult.Success(fallbackData)
            } else {
                FallbackResult.Failed(Exception("Fallback returned null"))
            }
        } catch (e: Exception) {
            FallbackResult.Failed(e)
        }
    }
    
    private fun logFallback(message: String, reason: FallbackReason) {
        android.util.Log.d("FallbackManager", "$message (Reason: $reason)")
    }
}

abstract class CachedDataFallback<T> : FallbackStrategy<T> {
    abstract suspend fun getCachedData(): T?
    
    override suspend fun getFallback(): T? = getCachedData()
}

abstract class StaticDataFallback<T>(private val staticData: T) : FallbackStrategy<T> {
    override suspend fun getFallback(): T = staticData
    override val isEnabled: Boolean = true
    override val priority: Int = 1
}

abstract class EmptyDataFallback<T> : FallbackStrategy<T> {
    abstract val emptyValue: T
    
    override suspend fun getFallback(): T = emptyValue
    override val isEnabled: Boolean = true
    override val priority: Int = 99
}

data class CompositeFallbackStrategy<T>(
    private val strategies: List<FallbackStrategy<T>>
) : FallbackStrategy<T> {
    override val isEnabled: Boolean
        get() = strategies.any { it.isEnabled }
    
    override val priority: Int
        get() = strategies.minOfOrNull { it.priority } ?: 0
    
    override suspend fun getFallback(): T? {
        val sortedStrategies = strategies.sortedBy { it.priority }
        
        for (strategy in sortedStrategies) {
            if (strategy.isEnabled) {
                val data = strategy.getFallback()
                if (data != null) {
                    return data
                }
            }
        }
        return null
    }
}
