package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.ErrorHandler
import kotlinx.coroutines.delay
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class PemanfaatanRepositoryImpl(
    private val apiService: ApiService
) : PemanfaatanRepository {
    private val maxRetries = 3
    private val errorHandler = ErrorHandler()
    
    override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
        var currentRetry = 0
        var lastException: Exception? = null
        
        while (currentRetry <= maxRetries) {
            try {
                val response: Response<PemanfaatanResponse> = apiService.getPemanfaatan()
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