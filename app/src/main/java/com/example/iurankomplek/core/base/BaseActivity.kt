package com.example.iurankomplek.core.base

import com.example.iurankomplek.R
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.utils.ErrorHandler
import com.example.iurankomplek.utils.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import retrofit2.Response

abstract class BaseActivity : AppCompatActivity() {
    private val errorHandler = ErrorHandler()

    protected fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 30000,
        operation: suspend () -> Response<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        currentRetry: Int = 0
    ) {
        lifecycleScope.launch {
            if (!NetworkUtils.isNetworkAvailable(this@BaseActivity)) {
                if (currentRetry == 0) {
                    onError(getString(R.string.no_internet_connection))
                }
                return@launch
            }

            try {
                val response = operation()
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError(getString(R.string.invalid_response_format))
                } else {
                    val isRetryable = isRetryableError(response.code())
                    if (currentRetry < maxRetries && isRetryable) {
                        scheduleRetry(
                            maxRetries = maxRetries,
                            initialDelayMs = initialDelayMs,
                            maxDelayMs = maxDelayMs,
                            operation = operation,
                            onSuccess = onSuccess,
                            onError = onError,
                            retryCount = currentRetry + 1
                        )
                    } else {
                        onError(getString(R.string.request_failed_with_status, response.code()))
                    }
                }
            } catch (t: Throwable) {
                val errorMessage = errorHandler.handleError(t)
                val isRetryable = isRetryableException(t)

                if (currentRetry < maxRetries && isRetryable) {
                    scheduleRetry(
                        maxRetries = maxRetries,
                        initialDelayMs = initialDelayMs,
                        maxDelayMs = maxDelayMs,
                        operation = operation,
                        onSuccess = onSuccess,
                        onError = onError,
                        retryCount = currentRetry + 1
                    )
                } else {
                    onError(errorMessage)
                    Log.e("BaseActivity", "Non-retryable error after $currentRetry retries: ${t.javaClass.simpleName}", t)
                }
            }
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
            else -> {
                Log.w("BaseActivity", "Non-retryable exception: ${t.javaClass.simpleName}")
                false
            }
        }
    }

    private fun <T> scheduleRetry(
        maxRetries: Int,
        initialDelayMs: Long,
        maxDelayMs: Long,
        operation: suspend () -> Response<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        retryCount: Int
    ) {
        val exponentialDelay = (initialDelayMs * 2.0.pow((retryCount - 1).toDouble())).toLong()
        val jitter = (kotlin.random.Random.nextDouble() * initialDelayMs).toLong()
        val delay = minOf(exponentialDelay + jitter, maxDelayMs)

        Log.d("BaseActivity", "Scheduling retry $retryCount in ${delay}ms")

        Handler(Looper.getMainLooper()).postDelayed({
            executeWithRetry(
                maxRetries = maxRetries,
                initialDelayMs = initialDelayMs,
                maxDelayMs = maxDelayMs,
                operation = operation,
                onSuccess = onSuccess,
                onError = onError,
                currentRetry = retryCount
            )
        }, delay)
    }
}
