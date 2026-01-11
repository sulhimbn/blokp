package com.example.iurankomplek.core.base

import com.example.iurankomplek.R
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.ref.WeakReference
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.utils.Constants
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
    private val errorHandler = ErrorHandler(this)
    private val accessibilityManager by lazy {
        getSystemService(android.view.accessibility.AccessibilityManager::class.java)
    }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingRetryRunnables = mutableMapOf<String, Runnable>()

    protected fun announceForAccessibility(text: String) {
        if (accessibilityManager.isEnabled) {
            window.decorView.announceForAccessibility(text)
        }
    }

    protected fun isTouchExplorationEnabled(): Boolean {
        return accessibilityManager.isTouchExplorationEnabled
    }

    protected fun <T> executeWithRetry(
        maxRetries: Int = Constants.Network.MAX_RETRIES,
        initialDelayMs: Long = Constants.Network.INITIAL_RETRY_DELAY_MS,
        maxDelayMs: Long = Constants.Network.MAX_RETRY_DELAY_MS,
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
                    Log.e("BaseActivity", "Non-retryable error")
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
                Log.w("BaseActivity", "Non-retryable exception")
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

        val activityRef = WeakReference(this@BaseActivity)
        val runnable = Runnable {
            val activity = activityRef.get()
            if (activity != null && !activity.isFinishing) {
                activity.executeWithRetry(
                    maxRetries = maxRetries,
                    initialDelayMs = initialDelayMs,
                    maxDelayMs = delay,
                    operation = operation,
                    onSuccess = onSuccess,
                    onError = onError,
                    currentRetry = retryCount
                )
            }
        }

        val retryId = "${System.currentTimeMillis()}_${retryCount}"
        pendingRetryRunnables[retryId] = runnable
        mainHandler.postDelayed(runnable, delay)

        mainHandler.postDelayed({
            pendingRetryRunnables.remove(retryId)
        }, delay + 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelPendingRetries()
    }

    private fun cancelPendingRetries() {
        pendingRetryRunnables.values.forEach { runnable ->
            mainHandler.removeCallbacks(runnable)
        }
        pendingRetryRunnables.clear()
    }
}
