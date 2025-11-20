package com.example.iurankomplek.utils

import retrofit2.HttpException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException

class ErrorHandler {
    fun handleError(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Connection timeout"
            is HttpException -> {
                when (throwable.code()) {
                    401 -> "Unauthorized access"
                    403 -> "Forbidden"
                    404 -> "Resource not found"
                    500 -> "Server error"
                    else -> "HTTP Error: ${throwable.code()}"
                }
            }
            is IOException -> "Network error occurred"
            else -> "An error occurred: ${throwable.message}"
        }
    }
}