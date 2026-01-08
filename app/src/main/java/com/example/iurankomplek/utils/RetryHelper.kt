package com.example.iurankomplek.utils

import com.example.iurankomplek.network.model.NetworkError
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlin.math.pow

object RetryHelper {

    fun <T : Any> executeWithRetry(
        apiCall: suspend () -> retrofit2.Response<T>,
        maxRetries: Int = Constants.Network.MAX_RETRIES
    ): Result<T> {
        var currentRetry = 0
        var lastException: Exception? = null

        while (currentRetry <= maxRetries) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    response.body()?.let { return Result.success(it) }
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

        return Result.failure(lastException ?: Exception("Unknown error occurred"))
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
            is SocketTimeoutException,
            is UnknownHostException,
            is SSLException -> true
            else -> false
        }
    }

    private fun calculateDelay(currentRetry: Int): Long {
        val exponentialDelay = (Constants.Network.INITIAL_RETRY_DELAY_MS * 2.0.pow((currentRetry - 1).toDouble())).toLong()
        val jitter = (kotlin.random.Random.nextDouble() * Constants.Network.INITIAL_RETRY_DELAY_MS).toLong()
        return minOf(exponentialDelay + jitter, Constants.Network.MAX_RETRY_DELAY_MS)
    }
}