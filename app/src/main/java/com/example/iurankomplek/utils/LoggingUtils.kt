package com.example.iurankomplek.utils

import android.util.Log

/**
 * Centralized logging utilities for the application
 * Provides consistent logging format and log level control
 */
object LoggingUtils {
    
    private const val DEFAULT_TAG = "BlokP"
    private var isDebugEnabled = true
    
    /**
     * Enable or disable debug logging
     */
    fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }
    
    /**
     * Log debug message
     */
    fun d(message: String, tag: String = DEFAULT_TAG) {
        if (isDebugEnabled) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log info message
     */
    fun i(message: String, tag: String = DEFAULT_TAG) {
        Log.i(tag, message)
    }
    
    /**
     * Log warning message
     */
    fun w(message: String, tag: String = DEFAULT_TAG) {
        Log.w(tag, message)
    }
    
    /**
     * Log warning with throwable
     */
    fun w(message: String, throwable: Throwable?, tag: String = DEFAULT_TAG) {
        Log.w(tag, message, throwable)
    }
    
    /**
     * Log error message
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        Log.e(tag, message, throwable)
    }
    
    /**
     * Log verbose message
     */
    fun v(message: String, tag: String = DEFAULT_TAG) {
        if (isDebugEnabled) {
            Log.v(tag, message)
        }
    }
    
    /**
     * Log network security warning
     */
    fun logNetworkSecurityWarning(message: String) {
        Log.w(Constants.Tags.SECURITY_MANAGER, "[NETWORK_SECURITY] $message")
    }
    
    /**
     * Log API call for debugging
     */
    fun logApiCall(endpoint: String, method: String = "GET") {
        d("API Call: $method $endpoint", Constants.Tags.WEBHOOK_RECEIVER)
    }
    
    /**
     * Log API response for debugging
     */
    fun logApiResponse(endpoint: String, statusCode: Int, success: Boolean) {
        val status = if (success) "SUCCESS" else "FAILED"
        d("API Response [$endpoint]: HTTP $statusCode - $status", Constants.Tags.WEBHOOK_RECEIVER)
    }
}
