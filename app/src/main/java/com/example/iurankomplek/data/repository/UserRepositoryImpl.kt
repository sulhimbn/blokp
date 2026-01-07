package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlin.math.min
import kotlin.math.pow

class UserRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : UserRepository {
    private val maxRetries = 3
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    
    override suspend fun getUsers(): Result<UserResponse> {
        val circuitBreakerResult = circuitBreaker.execute {
            executeWithRetry()
        }
        
        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> {
                Result.success(circuitBreakerResult.value)
            }
            is CircuitBreakerResult.Failure -> {
                Result.failure(circuitBreakerResult.exception)
            }
            is CircuitBreakerResult.CircuitOpen -> {
                Result.failure(com.example.iurankomplek.network.model.NetworkError.CircuitBreakerError())
            }
        }
    }
    
    private suspend fun executeWithRetry(): UserResponse {
        var currentRetry = 0
        var lastException: Exception? = null
        
        while (currentRetry <= maxRetries) {
            try {
                val response: Response<UserResponse> = apiService.getUsers()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        return responseBody
                    } else {
                        throw Exception("Response body is null")
                    }
                } else {
                    val isRetryable = isRetryableError(response.code())
                    if (currentRetry < maxRetries && isRetryable) {
                        val delayMillis = calculateDelay(currentRetry + 1)
                        kotlinx.coroutines.delay(delayMillis)
                        currentRetry++
                        continue
                    } else {
                        throw Exception("API request failed with code: ${response.code()}")
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
                val isRetryable = isRetryableException(e)
                if (currentRetry < maxRetries && isRetryable) {
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
    
    private fun isRetryableError(httpCode: Int): Boolean {
        return httpCode in 408..429 || httpCode / 100 == 5
    }
    
    private fun isRetryableException(t: Throwable): Boolean {
        return when (t) {
            is SocketTimeoutException,
            is UnknownHostException,
            is SSLException -> true
            else -> false
        }
    }
    
    private fun calculateDelay(currentRetry: Int): Long {
        val exponentialDelay = (com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS * Math.pow(2.0, (currentRetry - 1).toDouble())).toLong()
        val jitter = (Math.random() * com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS).toLong()
        return minOf(exponentialDelay + jitter, com.example.iurankomplek.utils.Constants.Network.MAX_RETRY_DELAY_MS)
    }
}