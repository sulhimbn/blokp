package com.example.iurankomplek

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseActivity : AppCompatActivity() {
    
    protected fun <T> executeWithRetry(
        maxRetries: Int = 3,
        operation: (Int) -> Call<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        currentRetry: Int = 0
    ) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            if (currentRetry == 0) {
                onError("No internet connection. Please check your network settings.")
            }
            return
        }
        
        operation(currentRetry).enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                        ?: onError("Invalid response format")
                } else if (currentRetry < maxRetries) {
                    scheduleRetry(operation, onSuccess, onError, currentRetry + 1)
                } else {
                    onError("Failed after ${maxRetries + 1} attempts")
                }
            }
            
            override fun onFailure(call: Call<T>, t: Throwable) {
                if (currentRetry < maxRetries) {
                    scheduleRetry(operation, onSuccess, onError, currentRetry + 1)
                } else {
                    onError("Network error: ${t.message}")
                    t.printStackTrace()
                }
            }
        })
    }
    
    private fun <T> scheduleRetry(
        operation: (Int) -> Call<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        retryCount: Int
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            executeWithRetry(operation, onSuccess, onError, retryCount)
        }, 1000L * retryCount)
    }
}