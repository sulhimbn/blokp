package com.example.iurankomplek.payment

import android.util.Base64
import android.util.Log
import com.example.iurankomplek.utils.Constants
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class WebhookSignatureVerifier(
    private val securityConfig: WebhookSecurityConfig = WebhookSecurityConfig
) {
    
    companion object {
        private const val SIGNATURE_HEADER = "X-Webhook-Signature"
        private const val SIGNATURE_PREFIX = "sha256="
        private const val HMAC_ALGORITHM = "HmacSHA256"
        
        fun extractSignature(headers: Map<String, String>): String? {
            val signatureHeader = headers[SIGNATURE_HEADER] ?: headers[SIGNATURE_HEADER.lowercase()]
            return if (signatureHeader?.startsWith(SIGNATURE_PREFIX) == true) {
                signatureHeader.substring(SIGNATURE_PREFIX.length)
            } else {
                null
            }
        }
    }
    
    private var macInstance: Mac? = null
    private var lastSecretKey: String? = null
    
    fun verifyWebhookSignature(
        payload: String,
        signature: String?
    ): WebhookVerificationResult {
        if (payload.isBlank()) {
            return WebhookVerificationResult.Invalid("Empty payload")
        }
        
        if (signature.isNullOrBlank()) {
            return WebhookVerificationResult.Invalid("Missing signature")
        }
        
        val secretKey = securityConfig.getWebhookSecret()
        if (secretKey.isNullOrBlank()) {
            Log.e(Constants.Tags.WEBHOOK_RECEIVER, "Webhook signature verification disabled")
            return WebhookVerificationResult.Skipped("Secret key not configured")
        }
        
        return try {
            val expectedSignature = computeSignature(payload, secretKey)
            
            if (constantTimeCompare(signature, expectedSignature)) {
                WebhookVerificationResult.Valid
            } else {
                WebhookVerificationResult.Invalid("Signature mismatch")
            }
        } catch (e: Exception) {
            Log.e(Constants.Tags.WEBHOOK_RECEIVER, "Error verifying webhook signature")
            WebhookVerificationResult.Invalid("Verification error")
        }
    }
    
    private fun computeSignature(payload: String, secretKey: String): String {
        val mac = getMacInstance(secretKey)
        val payloadBytes = payload.toByteArray(StandardCharsets.UTF_8)
        val signatureBytes = mac.doFinal(payloadBytes)
        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }
    
    private fun getMacInstance(secretKey: String): Mac {
        if (macInstance == null || lastSecretKey != secretKey) {
            try {
                val keySpec = SecretKeySpec(
                    secretKey.toByteArray(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
                )
                macInstance = Mac.getInstance(HMAC_ALGORITHM).apply {
                    init(keySpec)
                }
                lastSecretKey = secretKey
            } catch (e: NoSuchAlgorithmException) {
                throw SecurityException("HMAC-SHA256 algorithm not available", e)
            } catch (e: InvalidKeyException) {
                throw SecurityException("Invalid webhook secret key", e)
            }
        }
        return requireNotNull(macInstance) { "Mac instance not initialized" }
    }
    
    private fun constantTimeCompare(a: String, b: String): Boolean {
        if (a.length != b.length) {
            return false
        }
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}

sealed class WebhookVerificationResult {
    data object Valid : WebhookVerificationResult()
    data class Invalid(val reason: String) : WebhookVerificationResult()
    data class Skipped(val reason: String) : WebhookVerificationResult()
}
