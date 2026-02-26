package com.example.iurankomplek

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.iurankomplek.utils.ErrorHandler
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Base fragment providing common retry logic for network operations.
 * Follows the same pattern as BaseActivity for consistency.
 */
abstract class BaseFragment : Fragment() {
    private val errorHandler = ErrorHandler()
    private val retryHandler = Handler(Looper.getMainLooper())
    
    protected fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 30000,
        operation: (Int) -> Call<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        currentRetry: Int = 0
    ) {
        // Check if fragment is still attached
        if (!isAdded) {
            return
        }
        
        // Check network availability on each retry attempt
        context?.let { ctx ->
            if (!NetworkUtils.isNetworkAvailable(ctx)) {
                if (currentRetry == 0) {
                    onError(getString(R.string.no_internet_connection))
                }
                return
            }
        } ?: run {
            onError("Fragment context not available")
            return
        }
        
        operation(currentRetry).enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                // Check if fragment is still attached before proceeding
                if (!isAdded) return
                
                if (response.isSuccessful) {
                    response.body()?.let { 
                        onSuccess(it) 
                    } ?: onError(getString(R.string.invalid_response_format))
                } else {
                    // Check if the error is retryable
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
            }
            
            override fun onFailure(call: Call<T>, t: Throwable) {
                // Check if fragment is still attached before proceeding
                if (!isAdded) return
                
                val errorMessage = errorHandler.handleError(t)
                
                // Check if the error is retryable
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
                    Log.e("BaseFragment", "Operation failed after $maxRetries retries", t)
                }
            }
        })
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
            else -> {
                // Log non-retryable exceptions for debugging
                Log.w("BaseFragment", "Non-retryable exception: ${t.javaClass.simpleName}")
                false
            }
        }
    }
    
    private fun <T> scheduleRetry(
        maxRetries: Int,
        initialDelayMs: Long,
        maxDelayMs: Long,
        operation: (Int) -> Call<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        retryCount: Int
    ) {
        // Implement exponential backoff with jitter and max delay
        val exponentialDelay = (initialDelayMs * Math.pow(2.0, (retryCount - 1).toDouble())).toLong()
        // Add jitter to prevent thundering herd problem
        val jitter = (Math.random() * initialDelayMs).toLong()
        val delay = minOf(exponentialDelay + jitter, maxDelayMs)
        
        Log.d("BaseFragment", "Scheduling retry $retryCount in ${delay}ms")
        
        retryHandler.postDelayed({
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

    override fun onDestroyView() {
        super.onDestroyView()
        retryHandler.removeCallbacksAndMessages(null)
    }
}
