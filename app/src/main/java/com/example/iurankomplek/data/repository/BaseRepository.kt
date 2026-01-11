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

    protected suspend fun <T : Any> executeWithCircuitBreakerV1(
        apiCall: suspend () -> retrofit2.Response<ApiResponse<T>>
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
            is CircuitBreakerResult.Success -> OperationResult.Success(circuitBreakerResult.value.data)
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open")
        }
    }

    protected suspend fun <T : Any> executeWithCircuitBreakerV2(
        apiCall: suspend () -> retrofit2.Response<ApiListResponse<T>>
    ): OperationResult<List<T>> {
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
            is CircuitBreakerResult.Success -> OperationResult.Success(circuitBreakerResult.value.data)
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open")
        }
}

typealias BaseRepositoryLegacy = BaseRepository
