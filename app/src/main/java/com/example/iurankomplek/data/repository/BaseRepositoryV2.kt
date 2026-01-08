package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.repository.cache.CacheStrategy
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult

/**
 * Enhanced base repository with unified caching strategy.
 * Provides consistent error handling, circuit breaker protection,
 * and pluggable caching for all repositories.
 *
 * @param T Type of data cached by this repository
 *
 * Design Principles:
 * - DRY: Circuit breaker and retry logic in one place
 * - Strategy Pattern: Pluggable cache implementations
 * - Consistency: All repos use same error handling
 * - Simplicity: Simple repos and complex repos unified
 */
abstract class BaseRepository<T> {

    protected val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker

    protected val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES

    /**
     * Cache strategy for this repository.
     * Can be InMemoryCacheStrategy, DatabaseCacheStrategy, or NoCacheStrategy.
     */
    protected abstract val cacheStrategy: CacheStrategy<T>

    /**
     * Fetch data with unified caching and error handling.
     *
     * Workflow:
     * 1. Try to get from cache (unless forceRefresh)
     * 2. Check if cache is valid
     * 3. If valid, return cached data
     * 4. Otherwise, fetch from network with circuit breaker
     * 5. Update cache on success
     * 6. Return result
     *
     * @param cacheKey Optional key for cache (null = no caching)
     * @param forceRefresh Skip cache and fetch from network
     * @param fromNetwork Network fetch function
     * @param fromCache Optional custom cache fetch (uses cacheStrategy if null)
     * @return Result<T> with data or error
     */
    protected suspend fun <R> fetchWithCache(
        cacheKey: String? = null,
        forceRefresh: Boolean = false,
        fromNetwork: suspend () -> retrofit2.Response<R>,
        fromCache: (suspend () -> T?)? = null
    ): Result<R> {
        val cachedData = fromCache?.invoke() ?: cacheStrategy.get(cacheKey)

        if (cacheStrategy.isValid(cachedData, forceRefresh)) {
            @Suppress("UNCHECKED_CAST")
            return Result.success(cachedData as R)
        }

        val networkResult = executeWithCircuitBreaker(fromNetwork)

        networkResult.onSuccess { data ->
            @Suppress("UNCHECKED_CAST")
            cacheStrategy.put(cacheKey, data as T)
        }

        return networkResult
    }

    /**
     * Execute API call with circuit breaker and retry logic.
     *
     * @param apiCall Retrofit API call
     * @return Result<T> with data or error
     */
    protected suspend fun <T> executeWithCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            )
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> Result.success(circuitBreakerResult.value)
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(
                com.example.iurankomplek.network.model.NetworkError.CircuitBreakerError()
            )
        }
    }

    /**
     * Clear cache for this repository.
     * Delegates to cache strategy.
     */
    protected suspend fun clearCache() {
        cacheStrategy.clear()
    }
}
