package com.example.iurankomplek.data.repository

import retrofit2.Response
import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreakerRegistry
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.network.resilience.CircuitBreakerState
import com.example.iurankomplek.network.timeout.TimeoutManager
import com.example.iurankomplek.network.timeout.TimeoutResult
import com.example.iurankomplek.network.retry.RetryBudget
import com.example.iurankomplek.network.retry.RetryBudgetExhaustedException
import com.example.iurankomplek.data.api.models.ApiListResponse
import com.example.iurankomplek.data.api.models.ApiResponse
import com.example.iurankomplek.utils.OperationResult
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

abstract class BaseRepositoryV3(
    private val apiService: ApiServiceV1
) {
    
    protected val circuitBreakerRegistry = CircuitBreakerRegistry()
    protected val timeoutManager = TimeoutManager
    
    protected fun getRetryBudget(): RetryBudget = RetryBudget()
    
    protected suspend fun <T : Any> executeWithResilience(
        endpoint: String,
        apiCall: suspend () -> Response<T>
    ): OperationResult<T> {
        val circuitBreakerResult = circuitBreakerRegistry.execute(endpoint) {
            executeWithTimeoutAndRetry(endpoint, apiCall)
        }
        
        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> {
                val body = circuitBreakerResult.value.body()
                if (body != null) {
                    OperationResult.Success(body)
                } else {
                    OperationResult.Error(NetworkError.UnknownNetworkError("Empty response body", null), "Response body is null")
                }
            }
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open for $endpoint")
        }
    }
    
    protected suspend fun <T : Any> executeWithResilienceV1(
        endpoint: String,
        apiCall: suspend () -> Response<ApiResponse<T>>
    ): OperationResult<T> {
        val circuitBreakerResult = circuitBreakerRegistry.execute(endpoint) {
            val response = executeWithTimeoutAndRetry(endpoint, apiCall)
            response.body()?.data
        }
        
        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> {
                val value = circuitBreakerResult.value
                if (value != null) {
                    OperationResult.Success(value)
                } else {
                    OperationResult.Error(NetworkError.UnknownNetworkError("Empty response data", null), "Response data is null")
                }
            }
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open for $endpoint")
        }
    }
    
    protected suspend fun <T : Any> executeWithResilienceV2(
        endpoint: String,
        apiCall: suspend () -> Response<ApiListResponse<T>>
    ): OperationResult<List<T>> {
        val circuitBreakerResult = circuitBreakerRegistry.execute(endpoint) {
            val response = executeWithTimeoutAndRetry(endpoint, apiCall)
            response.body()?.data
        }
        
        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> {
                val value = circuitBreakerResult.value
                if (value != null) {
                    OperationResult.Success(value)
                } else {
                    OperationResult.Error(NetworkError.UnknownNetworkError("Empty response data", null), "Response data is null")
                }
            }
            is CircuitBreakerResult.Failure -> OperationResult.Error(circuitBreakerResult.exception, circuitBreakerResult.exception.message ?: "Unknown error")
            is CircuitBreakerResult.CircuitOpen -> OperationResult.Error(NetworkError.CircuitBreakerError(), "Circuit breaker open for $endpoint")
        }
    }
    
    private suspend fun <T> executeWithTimeoutAndRetry(
        endpoint: String,
        apiCall: suspend () -> T
    ): T {
        val timeoutResult = timeoutManager.withTimeout(endpoint) {
            executeWithRetryBudget(endpoint, apiCall)
        }
        
        return when (timeoutResult) {
            is TimeoutResult.Success -> timeoutResult.value
            is TimeoutResult.Timeout -> throw NetworkError.TimeoutError(
                userMessage = "Request to $endpoint timed out after ${timeoutResult.timeoutMs}ms",
                timeoutDuration = timeoutResult.timeoutMs
            )
        }
    }
    
    private suspend fun <T> executeWithRetryBudget(
        endpoint: String,
        apiCall: suspend () -> T
    ): T {
        val retryBudget = getRetryBudget()
        var currentRetry = 0
        var totalElapsedMs = 0L
        var lastException: Exception? = null
        val startTime = System.currentTimeMillis()
        
        while (retryBudget.canRetry(currentRetry, totalElapsedMs)) {
            try {
                val result = apiCall()
                retryBudget.recordRetry(0L, true)
                return result
            } catch (e: Exception) {
                lastException = e
                currentRetry++
                
                if (!shouldRetry(e)) {
                    break
                }
                
                val delayMs = retryBudget.calculateDelay(currentRetry)
                retryBudget.recordRetry(delayMs, false)
                kotlinx.coroutines.delay(delayMs)
                
                totalElapsedMs = System.currentTimeMillis() - startTime
            }
        }
        
        throw lastException ?: NetworkError.UnknownNetworkError(
            userMessage = "Unknown error occurred",
            originalException = lastException
        )
    }
    
    private fun shouldRetry(e: Exception): Boolean {
        return when (e) {
            is SocketTimeoutException -> true
            is UnknownHostException -> true
            is SSLException -> true
            is HttpException -> {
                val code = e.code()
                code in 408..429 || code / 100 == 5
            }
            is NetworkError.TimeoutError -> true
            is NetworkError.ConnectionError -> true
            else -> false
        }
    }
    
    fun getCircuitBreakerState(endpoint: String): CircuitBreakerState? {
        return circuitBreakerRegistry.getState(endpoint)
    }
    
    fun getTimeoutStats(endpoint: String) = timeoutManager.getTimeoutStats(endpoint)
    
    fun getRetryStats(endpoint: String) = getRetryBudget().getMetrics()
}
