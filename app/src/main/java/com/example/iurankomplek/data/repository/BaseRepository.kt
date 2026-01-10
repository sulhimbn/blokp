package com.example.iurankomplek.data.repository

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.data.api.models.ApiResponse

abstract class BaseRepository {
    
    protected val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    
    protected val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES
    
    protected suspend fun <T : Any> executeWithCircuitBreaker(
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
            is CircuitBreakerResult.CircuitOpen -> Result.failure(NetworkError.CircuitBreakerError())
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerV1(
        apiCall: suspend () -> retrofit2.Response<ApiResponse<T>>
    ): Result<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            )
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> {
                val response = circuitBreakerResult.value
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(NetworkError.HttpError(
                        code = com.example.iurankomplek.network.model.ApiErrorCode.fromHttpCode(response.code),
                        userMessage = "API request failed",
                        httpCode = response.code
                    ))
                }
            }
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(NetworkError.CircuitBreakerError())
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerV2(
        apiCall: suspend () -> retrofit2.Response<ApiListResponse<T>>
    ): Result<List<T>> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            )
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> {
                val response = circuitBreakerResult.value
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(NetworkError.HttpError(
                        code = com.example.iurankomplek.network.model.ApiErrorCode.fromHttpCode(response.code),
                        userMessage = "API request failed",
                        httpCode = response.code
                    ))
                }
            }
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(NetworkError.CircuitBreakerError())
        }
    }
}

typealias BaseRepositoryLegacy = BaseRepository
