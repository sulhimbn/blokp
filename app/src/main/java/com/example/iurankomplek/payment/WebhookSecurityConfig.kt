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
            Log.e(TAG, "Webhook signature verification disabled")
        } else {
            Log.d(TAG, "Webhook secret configured")
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
    }
}
