package com.example.iurankomplek.data.repository

import retrofit2.Response
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.data.api.models.ApiListResponse
import com.example.iurankomplek.data.api.models.ApiResponse
import com.example.iurankomplek.utils.OperationResult
import kotlin.Result

abstract class BaseRepository {

    protected val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker

    protected val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES

    protected suspend fun <T : Any> executeWithCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<T>
    ): OperationResult<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            )
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
            is CircuitBreakerResult.Success -> {
                val response = circuitBreakerResult.value
                if (response.isSuccessful && response.body() != null) {
                    OperationResult.Success(response.body()!!.data)
                } else {
                    OperationResult.Error(
                        NetworkError.HttpError(
                            code = com.example.iurankomplek.network.model.ApiErrorCode.fromHttpCode(response.code()),
                            userMessage = "API request failed",
                            httpCode = response.code()
                        ),
                        "API request failed"
                    )
                }
            }
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
            is CircuitBreakerResult.Success -> {
                val response = circuitBreakerResult.value
                if (response.isSuccessful && response.body() != null) {
                    OperationResult.Success(response.body()!!.data)
                } else {
                    OperationResult.Error(
                        NetworkError.HttpError(
                            code = com.example.iurankomplek.network.model.ApiErrorCode.fromHttpCode(response.code()),
                            userMessage = "API request failed",
                            httpCode = response.code()
                        ),
                        "API request failed"
                    )
                }
            }
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open")
        }
    }
}

typealias BaseRepositoryLegacy = BaseRepository
