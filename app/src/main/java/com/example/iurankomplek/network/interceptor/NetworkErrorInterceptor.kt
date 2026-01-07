package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.model.ApiErrorCode
import com.example.iurankomplek.network.model.NetworkError
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.net.ssl.SSLException
import android.util.Log

class NetworkErrorInterceptor(
    private val enableLogging: Boolean = false,
    private val tag: String = "NetworkErrorInterceptor"
) : Interceptor {
    
    private val gson = Gson()
    private val charset: Charset = StandardCharsets.UTF_8
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val requestTag = originalRequest.tag(String::class.java) ?: "Unknown Request"
        
        return try {
            val response = chain.proceed(originalRequest)
            
            if (!response.isSuccessful) {
                val error = parseErrorResponse(response, requestTag)
                if (enableLogging) {
                    logError(requestTag, error, response.code)
                }
                throw error
            }
            
            response
        } catch (e: SocketTimeoutException) {
            val error = NetworkError.TimeoutError(userMessage = "Request timed out", timeoutDuration = 30000L)
            if (enableLogging) {
                logError(requestTag, error, null)
            }
            throw error
        } catch (e: UnknownHostException) {
            val error = NetworkError.ConnectionError(userMessage = "No internet connection", cause = e)
            if (enableLogging) {
                logError(requestTag, error, null)
            }
            throw error
        } catch (e: SSLException) {
            val error = NetworkError.ConnectionError(
                userMessage = "Secure connection failed",
                cause = e
            )
            if (enableLogging) {
                logError(requestTag, error, null)
            }
            throw error
        } catch (e: IOException) {
            val error = NetworkError.ConnectionError(userMessage = "Network error", cause = e)
            if (enableLogging) {
                logError(requestTag, error, null)
            }
            throw error
        } catch (e: NetworkError) {
            if (enableLogging) {
                logError(requestTag, e, null)
            }
            throw e
        } catch (e: Exception) {
            val error = NetworkError.UnknownNetworkError(originalException = e)
            if (enableLogging) {
                logError(requestTag, error, null)
            }
            throw error
        }
    }
    
    private fun parseErrorResponse(response: Response, requestTag: String): NetworkError {
        val httpCode = response.code
        val errorCode = ApiErrorCode.fromHttpCode(httpCode)
        
        val responseBody = response.peekBody(Long.MAX_VALUE)
        val bodyString = responseBody.string()
        
        return try {
            val apiError = gson.fromJson(bodyString, com.example.iurankomplek.network.model.ApiError::class.java)
            NetworkError.HttpError(
                code = errorCode,
                userMessage = apiError.message ?: errorCode.defaultMessage,
                httpCode = httpCode,
                details = apiError.details
            )
        } catch (e: Exception) {
            when (httpCode) {
                400 -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "Bad request. Please check your parameters.",
                    httpCode = httpCode
                )
                401 -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "Unauthorized. Please log in again.",
                    httpCode = httpCode
                )
                403 -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "Access denied.",
                    httpCode = httpCode
                )
                404 -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "Resource not found.",
                    httpCode = httpCode
                )
                429 -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "Too many requests. Please slow down.",
                    httpCode = httpCode
                )
                500 -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "Server error. Please try again later.",
                    httpCode = httpCode
                )
                503 -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "Service unavailable. Please try again later.",
                    httpCode = httpCode
                )
                else -> NetworkError.HttpError(
                    code = errorCode,
                    userMessage = "HTTP Error $httpCode",
                    httpCode = httpCode
                )
            }
        }
    }
    
    private fun logError(requestTag: String, error: NetworkError, httpCode: Int?) {
        val message = buildString {
            append("Request: $requestTag\n")
            append("Error: ${error.javaClass.simpleName}\n")
            append("Code: ${error.code}\n")
            append("User Message: ${error.userMessage}\n")
            httpCode?.let { append("HTTP Code: $it\n") }
            append("Details: ${error.message}")
        }
        Log.e(tag, message)
    }
}

class RequestIdInterceptor : Interceptor {
    companion object {
        private const val REQUEST_ID_HEADER = "X-Request-ID"
    }
    
    private val idGenerator = java.util.UUID.randomUUID().toString()
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestId = generateRequestId()
        val originalRequest = chain.request()
        
        val requestWithId = originalRequest.newBuilder()
            .tag(Class.forName("java.lang.String"), requestId)
            .addHeader(REQUEST_ID_HEADER, requestId)
            .build()
        
        return chain.proceed(requestWithId)
    }
    
    private fun generateRequestId(): String {
        return "${System.currentTimeMillis()}-${(Math.random() * 10000).toInt()}"
    }
}

class RetryableRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestMethod = request.method
        
        val isRetryable = requestMethod == "GET" || 
                         requestMethod == "HEAD" || 
                         requestMethod == "OPTIONS" ||
                         request.header("X-Retryable") == "true"
        
        val requestWithFlag = request.newBuilder()
            .tag(Class.forName("java.lang.Boolean"), isRetryable)
            .build()
        
        return chain.proceed(requestWithFlag)
    }
}
