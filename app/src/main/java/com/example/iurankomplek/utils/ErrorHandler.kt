package com.example.iurankomplek.utils

import com.example.iurankomplek.network.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreakerException
import retrofit2.HttpException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException
import java.util.UUID

data class ErrorContext(
    val requestId: String? = null,
    val endpoint: String? = null,
    val httpCode: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class ErrorHandler {
    fun handleError(
        throwable: Throwable,
        context: ErrorContext? = null
    ): String {
        val userMessage = getUserMessage(throwable)
        
        logError(throwable, context, userMessage)
        
        return userMessage
    }
    
    private fun getUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Connection timeout"
            is CircuitBreakerException -> "Service temporarily unavailable"
            is HttpException -> {
                when (throwable.code()) {
                    400 -> "Invalid request"
                    401 -> "Unauthorized access"
                    403 -> "Forbidden"
                    404 -> "Resource not found"
                    408 -> "Request timeout"
                    429 -> "Too many requests. Please slow down."
                    500 -> "Server error"
                    502 -> "Bad gateway"
                    503 -> "Service unavailable"
                    504 -> "Gateway timeout"
                    else -> "HTTP Error: ${throwable.code()}"
                }
            }
            is IOException -> "Network error occurred"
            else -> "An error occurred: ${throwable.message}"
        }
    }
    
    private fun logError(
        throwable: Throwable,
        context: ErrorContext?,
        userMessage: String
    ) {
        val requestId = context?.requestId ?: generateRequestId()
        val endpoint = context?.endpoint ?: "unknown"
        
        val logMessage = buildString {
            append("Error [ID: $requestId] ")
            append("at $endpoint: ")
            append("$userMessage")
            
            if (context?.httpCode != null) {
                append(" (HTTP ${context.httpCode})")
            }
            
            if (throwable is HttpException) {
                val errorBody = throwable.response()?.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    append(" | Details: $errorBody")
                }
            }
        }
        
        when (throwable) {
            is CircuitBreakerException -> {
                android.util.Log.w(Constants.Tags.BASE_ACTIVITY, logMessage, throwable)
            }
            is HttpException -> {
                if (throwable.code() >= 500) {
                    android.util.Log.e(Constants.Tags.BASE_ACTIVITY, logMessage, throwable)
                } else {
                    android.util.Log.w(Constants.Tags.BASE_ACTIVITY, logMessage, throwable)
                }
            }
            else -> {
                android.util.Log.e(Constants.Tags.BASE_ACTIVITY, logMessage, throwable)
            }
        }
    }
    
    private fun generateRequestId(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }
    
    fun toNetworkError(throwable: Throwable): NetworkError {
        return when (throwable) {
            is UnknownHostException -> NetworkError.ConnectionError("No internet connection")
            is SocketTimeoutException -> NetworkError.TimeoutError("Connection timeout")
            is CircuitBreakerException -> NetworkError.CircuitBreakerError(throwable.message ?: "Service unavailable")
            is HttpException -> NetworkError.HttpError(throwable.code(), throwable.message ?: "HTTP error")
            is IOException -> NetworkError.ConnectionError("Network error occurred")
            else -> NetworkError.UnknownNetworkError(throwable.message ?: "Unknown error")
        }
    }
}