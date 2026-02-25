package com.example.iurankomplek.utils

import android.util.Log
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

/**
 * WebhookSecurityUtil provides security utilities for webhook verification.
 * 
 * This includes:
 * - HMAC-SHA256 signature verification
 * - Timestamp validation to prevent replay attacks
 * 
 * IMPORTANT: In production, the webhook secret should be loaded from secure storage
 * (e.g., BuildConfig, encrypted preferences, or a secrets management service).
 */
object WebhookSecurityUtil {
    private val TAG = Constants.Tags.WEBHOOK_RECEIVER
    
    /**
     * Result of webhook verification
     */
    sealed class VerificationResult {
        object Success : VerificationResult()
        data class Error(val reason: String) : VerificationResult()
    }

    /**
     * Verifies a webhook request by validating:
     * 1. HMAC-SHA256 signature
     * 2. Timestamp for replay attack prevention
     * 
     * @param payload The raw webhook payload body
     * @param signature The signature from X-Webhook-Signature header
     * @param timestamp The timestamp from X-Webhook-Timestamp header (Unix timestamp in seconds)
     * @return VerificationResult indicating success or the specific error
     */
    fun verifyWebhook(
        payload: String,
        signature: String?,
        timestamp: String?
    ): VerificationResult {
        // Step 1: Validate signature presence
        if (signature.isNullOrBlank()) {
            Log.w(TAG, "Webhook verification failed: Missing signature header")
            return VerificationResult.Error("Missing signature")
        }

        // Step 2: Validate timestamp presence
        if (timestamp.isNullOrBlank()) {
            Log.w(TAG, "Webhook verification failed: Missing timestamp header")
            return VerificationResult.Error("Missing timestamp")
        }

        // Step 3: Validate timestamp format and replay attack check
        val timestampResult = validateTimestamp(timestamp)
        if (timestampResult is VerificationResult.Error) {
            Log.w(TAG, "Webhook verification failed: ${timestampResult.reason}")
            return timestampResult
        }

        // Step 4: Verify HMAC signature
        val signatureResult = verifySignature(payload, timestamp, signature)
        if (signatureResult is VerificationResult.Error) {
            Log.w(TAG, "Webhook verification failed: ${signatureResult.reason}")
            return signatureResult
        }

        Log.d(TAG, "Webhook signature verified successfully")
        return VerificationResult.Success
    }

    /**
     * Validates the timestamp to prevent replay attacks.
     * Checks that the timestamp is within the allowed tolerance window.
     */
    private fun validateTimestamp(timestamp: String): VerificationResult {
        return try {
            val webhookTimestamp = timestamp.toLong()
            val currentTimeMillis = System.currentTimeMillis()
            val currentTimestampSeconds = currentTimeMillis / 1000
            val timeDiffSeconds = abs(currentTimestampSeconds - webhookTimestamp)
            val toleranceSeconds = Constants.Security.WEBHOOK_TIMESTAMP_TOLERANCE_MS / 1000

            if (timeDiffSeconds > toleranceSeconds) {
                Log.w(TAG, "Webhook timestamp validation failed: Timestamp outside tolerance window. " +
                        "Difference: $timeDiffSeconds seconds, Tolerance: $toleranceSeconds seconds")
                VerificationResult.Error("Timestamp outside allowed window (replay attack suspected)")
            } else {
                VerificationResult.Success
            }
        } catch (e: NumberFormatException) {
            Log.w(TAG, "Webhook timestamp validation failed: Invalid timestamp format: $timestamp")
            VerificationResult.Error("Invalid timestamp format")
        }
    }

    /**
     * Verifies the HMAC-SHA256 signature of the payload.
     * 
     * Signature format expected: sha256=<hex-encoded-hmac>
     * The signature is computed over: timestamp.payload
     */
    private fun verifySignature(
        payload: String,
        timestamp: String,
        providedSignature: String
    ): VerificationResult {
        return try {
            // Expected signature format: sha256=<signature>
            val signaturePrefix = "sha256="
            val expectedPrefix = providedSignature.lowercase().startsWith(signaturePrefix)
            
            val providedHash = if (expectedPrefix) {
                providedSignature.substring(signaturePrefix.length)
            } else {
                providedSignature
            }

            // Compute expected signature: HMAC-SHA256(timestamp.payload, secret)
            val signedContent = "$timestamp.$payload"
            val computedHash = computeHmacSha256(signedContent, Constants.Security.WEBHOOK_SECRET_KEY)

            // Constant-time comparison to prevent timing attacks
            if (constantTimeEquals(providedHash, computedHash)) {
                VerificationResult.Success
            } else {
                Log.w(TAG, "Webhook signature verification failed: Signature mismatch")
                VerificationResult.Error("Invalid signature")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Webhook signature verification error: ${e.message}", e)
            VerificationResult.Error("Signature verification error")
        }
    }

    /**
     * Computes HMAC-SHA256 signature for the given data and secret.
     * 
     * @param data The data to sign
     * @param secret The secret key
     * @return Lowercase hex-encoded HMAC-SHA256 hash
     */
    private fun computeHmacSha256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(
            secret.toByteArray(StandardCharsets.UTF_8),
            "HmacSHA256"
        )
        mac.init(secretKeySpec)
        val hmacBytes = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     * 
     * @param a First string
     * @param b Second string
     * @return true if strings are equal, false otherwise
     */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) {
            return false
        }
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    /**
     * Generates a test signature for a given payload (for testing purposes only).
     * 
     * @param payload The payload to sign
     * @param timestamp The timestamp to use
     * @return The generated signature
     */
    fun generateTestSignature(payload: String, timestamp: String): String {
        val signedContent = "$timestamp.$payload"
        val hash = computeHmacSha256(signedContent, Constants.Security.WEBHOOK_SECRET_KEY)
        return "sha256=$hash"
    }
}
