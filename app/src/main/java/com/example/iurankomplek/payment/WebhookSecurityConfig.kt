package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.BuildConfig
import com.example.iurankomplek.utils.Constants

object WebhookSecurityConfig {
    private const val TAG = "${Constants.Tags.WEBHOOK_RECEIVER}.SecurityConfig"
    
    private var webhookSecret: String? = null
    
    fun initializeSecret(secret: String?) {
        webhookSecret = secret
        if (secret.isNullOrBlank()) {
            Log.w(TAG, "Webhook secret is null or blank, signature verification will be skipped")
        } else {
            Log.d(TAG, "Webhook secret configured successfully (length: ${secret.length})")
        }
    }
    
    fun getWebhookSecret(): String? {
        return webhookSecret ?: loadSecretFromEnvironment()
    }
    
    private fun loadSecretFromEnvironment(): String? {
        return System.getenv(Constants.Webhook.SECRET_ENV_VAR)
    }
    
    fun isSecretConfigured(): Boolean {
        return !getWebhookSecret().isNullOrBlank()
    }
    
    fun clearSecret() {
        webhookSecret = null
        Log.d(TAG, "Webhook secret cleared")
    }
}
