package com.example.iurankomplek.utils

import android.content.Context
import com.example.iurankomplek.R
import com.example.iurankomplek.network.model.NetworkError
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

class ErrorHandler(private val context: Context) {
    fun handleError(
        throwable: Throwable,
        errorContext: ErrorContext? = null
    ): String {
        val userMessage = getUserMessage(throwable)

        logError(throwable, errorContext, userMessage)

        return userMessage
    }

    private fun getUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> context.getString(R.string.no_internet_connection)
            is SocketTimeoutException -> context.getString(R.string.error_connection_timeout)
            is CircuitBreakerException -> context.getString(R.string.error_service_temporarily_unavailable)
            is HttpException -> {
                when (throwable.code()) {
                    400 -> context.getString(R.string.error_invalid_request)
                    401 -> context.getString(R.string.error_unauthorized_access)
                    403 -> context.getString(R.string.error_forbidden)
                    404 -> context.getString(R.string.error_resource_not_found)
                    408 -> context.getString(R.string.error_request_timeout)
                    429 -> context.getString(R.string.error_too_many_requests)
                    500 -> context.getString(R.string.error_server_error)
                    502 -> context.getString(R.string.error_bad_gateway)
                    503 -> context.getString(R.string.error_service_unavailable)
                    504 -> context.getString(R.string.error_gateway_timeout)
                    else -> context.getString(R.string.request_failed_with_status, throwable.code())
                }
            }
            is IOException -> context.getString(R.string.error_network_occurred)
            else -> context.getString(R.string.error_an_error_occurred, throwable.message)
        }
    }
    
    private fun logError(
        throwable: Throwable,
        errorContext: ErrorContext?,
        userMessage: String
    ) {
        val requestId = errorContext?.requestId ?: generateRequestId()
        val endpoint = errorContext?.endpoint ?: "unknown"

        val logMessage = buildString {
            append("Error [ID: $requestId] ")
            append("at $endpoint: ")
            append("$userMessage")

            if (errorContext?.httpCode != null) {
                append(" (HTTP ${errorContext.httpCode})")
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
            is UnknownHostException -> NetworkError.ConnectionError(userMessage = "No internet connection")
            is SocketTimeoutException -> NetworkError.TimeoutError(userMessage = "Connection timeout")
            is CircuitBreakerException -> NetworkError.CircuitBreakerError(
                code = com.example.iurankomplek.network.model.ApiErrorCode.SERVICE_UNAVAILABLE,
                userMessage = throwable.message ?: "Service unavailable"
            )
            is HttpException -> {
                val httpCode = throwable.code()
                val apiErrorCode = com.example.iurankomplek.network.model.ApiErrorCode.fromHttpCode(httpCode)
                NetworkError.HttpError(
                    code = apiErrorCode,
                    userMessage = throwable.message ?: "HTTP error",
                    httpCode = httpCode
                )
            }
            is IOException -> NetworkError.ConnectionError(userMessage = "Network error occurred")
            else -> NetworkError.UnknownNetworkError(
                code = com.example.iurankomplek.network.model.ApiErrorCode.UNKNOWN_ERROR,
                userMessage = throwable.message ?: "Unknown error"
            )
        }
    }
}