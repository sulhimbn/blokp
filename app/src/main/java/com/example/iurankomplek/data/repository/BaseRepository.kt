package com.example.iurankomplek.data.repository

import retrofit2.Response
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.data.api.models.ApiListResponse
import com.example.iurankomplek.data.api.models.ApiResponse
import com.example.iurankomplek.utils.OperationResult

abstract class BaseRepository {

    protected val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker

    protected val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES

    protected suspend fun <T : Any> executeWithCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<T>
    ): OperationResult<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            try {
                com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                    apiCall = apiCall,
                    maxRetries = maxRetries
                )
            } catch (e: retrofit2.HttpException) {
                throw com.example.iurankomplek.network.model.NetworkError.HttpError(
                    code = com.example.iurankomplek.network.model.ApiErrorCode.fromHttpCode(e.code()),
                    userMessage = "API request failed",
                    httpCode = e.code()
                )
            }
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> OperationResult.Success(circuitBreakerResult.value)
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open")
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerV1(
        apiCall: suspend () -> retrofit2.Response<ApiResponse<T>>
    ): OperationResult<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            )
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> OperationResult.Success(circuitBreakerResult.value.data)
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open")
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerV2(
        apiCall: suspend () -> retrofit2.Response<ApiListResponse<T>>
    ): OperationResult<List<T>> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            )
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> OperationResult.Success(circuitBreakerResult.value.data)
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open")
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerAndFallback(
        apiCall: suspend () -> retrofit2.Response<T>,
        fallbackStrategy: FallbackStrategy<T>?,
        config: FallbackConfig = FallbackConfig()
    ): OperationResult<T> {
        val fallbackManager = FallbackManager<T>(
            fallbackStrategy = fallbackStrategy,
            config = config
        )

        return fallbackManager.executeWithFallback {
            executeWithCircuitBreaker(apiCall)
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerV1AndFallback(
        apiCall: suspend () -> retrofit2.Response<ApiResponse<T>>,
        fallbackStrategy: FallbackStrategy<T>?,
        config: FallbackConfig = FallbackConfig()
    ): OperationResult<T> {
        val fallbackManager = FallbackManager<T>(
            fallbackStrategy = fallbackStrategy,
            config = config
        )

        return fallbackManager.executeWithFallback {
            executeWithCircuitBreakerV1(apiCall)
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerV2AndFallback(
        apiCall: suspend () -> retrofit2.Response<ApiListResponse<T>>,
        fallbackStrategy: FallbackStrategy<List<T>>?,
        config: FallbackConfig = FallbackConfig()
    ): OperationResult<List<T>> {
        val fallbackManager = FallbackManager<List<T>>(
            fallbackStrategy = fallbackStrategy,
            config = config
        )

        return fallbackManager.executeWithFallback {
            executeWithCircuitBreakerV2(apiCall)
        }
    }
}

typealias BaseRepositoryLegacy = BaseRepository
