package com.example.iurankomplek.utils

import android.util.Log
import com.example.iurankomplek.BuildConfig
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
 * IMPORTANT: the webhook secret MUST be supplied at build time via
 * BuildConfig.WEBHOOK_SECRET (set from a CI/CD secret). When it is absent this
 * utility fails closed: verification returns an error instead of falling back to
 * a value that is compiled into the APK and therefore public to an attacker.
 */
object WebhookSecurityUtil {
    private val TAG = Constants.Tags.WEBHOOK_RECEIVER

    private val configuredSecret: String?
        get() = BuildConfig.WEBHOOK_SECRET.takeIf { it.isNotBlank() }

    /**
     * Result of webhook verification
     */
    sealed class VerificationResult {
        object Success : VerificationResult()
        data class Error(val reason: String) : VerificationResult()
    }

    /**
     * Verifies a webhook request using the build-time secret.
     *
     * Fails closed when no secret was configured at build time.
     */
    fun verifyWebhook(
        payload: String,
        signature: String?,
        timestamp: String?
    ): VerificationResult {
        val secret = configuredSecret
        if (secret == null) {
            Log.e(TAG, "Webhook verification refused: no secret configured in BuildConfig.WEBHOOK_SECRET")
            return VerificationResult.Error("Webhook secret not configured")
        }
        return verifyWebhook(payload, signature, timestamp, secret)
    }

    /**
     * Verifies a webhook request against an explicit secret.
     *
     * The signature is verified against the raw payload before any caller is
     * allowed to process it, and the timestamp is bounded to reject replays.
     */
    fun verifyWebhook(
        payload: String,
        signature: String?,
        timestamp: String?,
        secret: String
    ): VerificationResult {
        if (signature.isNullOrBlank()) {
            Log.w(TAG, "Webhook verification failed: Missing signature header")
            return VerificationResult.Error("Missing signature")
        }

        if (timestamp.isNullOrBlank()) {
            Log.w(TAG, "Webhook verification failed: Missing timestamp header")
            return VerificationResult.Error("Missing timestamp")
        }

        val timestampResult = validateTimestamp(timestamp)
        if (timestampResult is VerificationResult.Error) {
            Log.w(TAG, "Webhook verification failed: ${timestampResult.reason}")
            return timestampResult
        }

        val signatureResult = verifySignature(payload, timestamp, signature, secret)
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
        providedSignature: String,
        secret: String
    ): VerificationResult {
        return try {
            val signaturePrefix = "sha256="
            val hasPrefix = providedSignature.lowercase().startsWith(signaturePrefix)

            val providedHash = if (hasPrefix) {
                providedSignature.substring(signaturePrefix.length)
            } else {
                providedSignature
            }

            val signedContent = "$timestamp.$payload"
            val computedHash = computeHmacSha256(signedContent, secret)

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
     * Generates a signature for the given payload and secret.
     *
     * Exposed so callers and tests can produce signatures without depending on
     * build-time configuration; verification always uses an explicit secret.
     */
    fun generateSignature(payload: String, timestamp: String, secret: String): String {
        val signedContent = "$timestamp.$payload"
        return "sha256=${computeHmacSha256(signedContent, secret)}"
    }
}
