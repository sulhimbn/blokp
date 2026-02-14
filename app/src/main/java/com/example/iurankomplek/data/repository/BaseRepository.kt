package com.example.iurankomplek.data.repository

import com.example.iurankomplek.utils.ErrorHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlin.math.min
import kotlin.math.pow

/**
 * Base repository interface defining common operations for all repositories
 */
interface BaseRepository<T> {
    
    suspend fun getAll(): Result<List<T>>
    
    suspend fun getById(id: String): Result<T>
    
    suspend fun create(item: T): Result<T>
    
    suspend fun update(item: T): Result<T>
    
    suspend fun delete(id: String): Result<Boolean>
    
    fun observeAll(): Flow<Result<List<T>>>
}

/**
 * Abstract base class for network repositories providing common retry logic
 * and error handling for API operations.
 */
abstract class BaseNetworkRepository {
    
    protected val maxRetries = 3
    protected abstract val errorHandler: ErrorHandler
    
    /**
     * Executes a network operation with automatic retry logic using exponential backoff.
     * 
     * @param operation The suspend function to execute
     * @param transform Transform function to convert Response body to desired type
     * @return Result containing either the success data or failure exception
     */
    protected suspend fun <T, R> executeWithRetry(
        operation: suspend () -> Response<T>,
        transform: (T) -> R
    ): Result<R> {
        var currentRetry = 0
        var lastException: Exception? = null
        
        while (currentRetry <= maxRetries) {
            try {
                val response = operation()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        return Result.success(transform(responseBody))
                    } else {
                        return Result.failure(Exception("Response body is null"))
                    }
                } else {
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
                
                val isRetryable = isRetryableException(e)
                if (currentRetry < maxRetries && isRetryable) {
                    val delayMillis = calculateDelay(currentRetry + 1, 1000, 30000)
                    delay(delayMillis)
                    currentRetry++
                } else {
                    break
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
    protected fun isRetryableError(httpCode: Int): Boolean {
        // Retry on server errors (5xx) and some client errors (4xx)
        // 408: Request Timeout
        // 429: Too Many Requests
        return httpCode in 408..429 || httpCode / 100 == 5
    }
    
    /**
     * Determines if an exception is retryable
     */
    protected fun isRetryableException(t: Throwable): Boolean {
        return when (t) {
            is SocketTimeoutException,
            is UnknownHostException,
            is SSLException -> true
            else -> false
        }
    }
    
    /**
     * Calculates delay for retry using exponential backoff with jitter
     */
    protected fun calculateDelay(currentRetry: Int, initialDelayMs: Long, maxDelayMs: Long): Long {
        // Implement exponential backoff with jitter and max delay
        val exponentialDelay = (initialDelayMs * 2.0.pow(currentRetry - 1)).toLong()
        // Add jitter to prevent thundering herd problem
        val jitter = (Math.random() * initialDelayMs).toLong()
        return min(exponentialDelay + jitter, maxDelayMs)
    }
}
