package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import retrofit2.HttpException

class UserRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : UserRepository {
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    private val maxRetries = 3
    
    override suspend fun getUsers(): Result<UserResponse> = withCircuitBreaker {
        apiService.getUsers()
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
        val exponentialDelay = (com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS * Math.pow(2.0, (currentRetry - 1).toDouble())).toLong()
        val jitter = (Math.random() * com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS).toLong()
        return minOf(exponentialDelay + jitter, com.example.iurankomplek.utils.Constants.Network.MAX_RETRY_DELAY_MS)
    }
}