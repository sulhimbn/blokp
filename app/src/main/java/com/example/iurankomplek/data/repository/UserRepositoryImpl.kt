package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.ErrorHandler
import kotlinx.coroutines.delay
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {
    private val maxRetries = 3
    private val errorHandler = ErrorHandler()
    
    override suspend fun getUsers(): Result<UserResponse> {
        var currentRetry = 0
        var lastException: Exception? = null
        
        while (currentRetry <= maxRetries) {
            try {
                val response: Response<UserResponse> = apiService.getUsers()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        return Result.success(responseBody)
                    } else {
                        return Result.failure(Exception("Response body is null"))
                    }
                } else {
                    // Check if the error is retryable
                    val isRetryable = isRetryableError(response.code())
                    if (currentRetry < maxRetries && isRetryable) {
                        val delayMillis = calculateDelay(currentRetry + 1, 1000, 30000)
                        delay(delayMillis)
                        currentRetry++
                        continue
                    } else {
                        throw Exception("API request failed with code: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                lastException = e
                
                // Check if the exception is retryable
                val isRetryable = isRetryableException(e)
                if (currentRetry < maxRetries && isRetryable) {
                    val delayMillis = calculateDelay(currentRetry + 1, 1000, 30000)
                    delay(delayMillis)
                    currentRetry++
                } else {
                    break // Exit the loop after max retries or non-retryable error
                }
            }
        }
        
        val errorMessage = if (lastException != null) {
            errorHandler.handleError(lastException)
        } else {
            "Unknown error occurred"
        }
        
        return Result.failure(Exception(errorMessage))
    }
    
    /**
     * Determines if an HTTP error code is retryable
     */
    private fun isRetryableError(httpCode: Int): Boolean {
        // Retry on server errors (5xx) and some client errors (4xx)
        // 408: Request Timeout
        // 429: Too Many Requests
        return httpCode in 408..429 || httpCode / 100 == 5
    }
    
    /**
     * Determines if an exception is retryable
     */
    private fun isRetryableException(t: Throwable): Boolean {
        return when (t) {
            is SocketTimeoutException,
            is UnknownHostException,
            is SSLException -> true
            else -> false
        }
    }
    
    private fun calculateDelay(currentRetry: Int, initialDelayMs: Long, maxDelayMs: Long): Long {
        // Implement exponential backoff with jitter and max delay
        val exponentialDelay = (initialDelayMs * Math.pow(2.0, (currentRetry - 1).toDouble())).toLong()
        // Add jitter to prevent thundering herd problem
        val jitter = (Math.random() * initialDelayMs).toLong()
        return minOf(exponentialDelay + jitter, maxDelayMs)
    }
}
    }
    
    private suspend fun <T> executeWithRetry(
        maxRetries: Int,
        initialDelayMs: Long,
        maxDelayMs: Long,
        operation: (Int) -> Call<T>
    ): Result<T> = suspendCancellableCoroutine { continuation ->
        var currentRetry = 0
        
        fun makeRequest() {
            val call = operation(currentRetry)
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            continuation.resume(Result.success(responseBody))
                        } else {
                            continuation.resume(Result.failure(Exception("Response body is null")))
                        }
                    } else {
                        val isRetryable = isRetryableError(response.code())
                        if (currentRetry < maxRetries && isRetryable) {
                            currentRetry++
                            scheduleRetry(makeRequest, calculateDelay(currentRetry, initialDelayMs, maxDelayMs))
                        } else {
                            continuation.resume(Result.failure(Exception("API request failed with code: ${response.code()}")))
                        }
                    }
                }
                
                override fun onFailure(call: Call<T>, t: Throwable) {
                    val errorMessage = errorHandler.handleError(t)
                    val isRetryable = isRetryableException(t)
                    
                    if (currentRetry < maxRetries && isRetryable) {
                        currentRetry++
                        scheduleRetry(makeRequest, calculateDelay(currentRetry, initialDelayMs, maxDelayMs))
                    } else {
                        continuation.resume(Result.failure(Exception(errorMessage)))
                    }
                }
            })
        }
        
        makeRequest()
    }
    
    private fun isRetryableError(httpCode: Int): Boolean {
        // Retry on server errors (5xx) and some client errors (4xx)
        // 408: Request Timeout
        // 429: Too Many Requests
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
    
    private fun calculateDelay(currentRetry: Int, initialDelayMs: Long, maxDelayMs: Long): Long {
        // Implement exponential backoff with jitter and max delay
        val exponentialDelay = (initialDelayMs * Math.pow(2.0, (currentRetry - 1).toDouble())).toLong()
        // Add jitter to prevent thundering herd problem
        val jitter = (Math.random() * initialDelayMs).toLong()
        return minOf(exponentialDelay + jitter, maxDelayMs)
    }
    
    private fun scheduleRetry(operation: () -> Unit, delay: Long) {
        // Using a fixed delay mechanism as there's no context available in repository
        // In a real implementation, we'd use a proper scheduler
        Thread.sleep(delay)
        operation()
    }
}