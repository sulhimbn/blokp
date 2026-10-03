package com.example.iurankomplek.utils

import org.junit.Assert.*
import org.junit.Test

class WebhookSecurityUtilTest {

    private val secret = "unit-test-webhook-secret"

    private fun sign(payload: String, timestamp: String): String =
        WebhookSecurityUtil.generateSignatureWithSecret(payload, timestamp, secret)

    private fun verify(
        payload: String,
        signature: String?,
        timestamp: String?
    ): WebhookSecurityUtil.VerificationResult =
        WebhookSecurityUtil.verifyWebhookWithSecret(payload, signature, timestamp, secret)

    @Test
    fun verifyWebhook_failsClosedWhenNoSecretIsConfigured() {
        // A build without WEBHOOK_SECRET must reject everything rather than fall back to a
        // value that is readable from the APK.
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val result = WebhookSecurityUtil.verifyWebhookWithSecret(payload, "sha256=anything", timestamp, "")

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals(
            "Webhook secret is not configured",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun committedConstantsMustNotCarryAUsableSecret() {
        assertEquals(
            "a secret committed to the repo would be forgeable from any decompiled APK",
            "", Constants.Security.WEBHOOK_SECRET_KEY
        )
    }

    @Test
    fun signatureFromADifferentSecretIsRejected() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val forged = WebhookSecurityUtil.generateSignatureWithSecret(payload, timestamp, "attacker-guess")

        val result = verify(payload, forged, timestamp)

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Invalid signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val currentTimestamp = System.currentTimeMillis() / 1000
        val signature = sign(payload, currentTimestamp.toString())
        
        val result = verify(
            payload = payload,
            signature = signature,
            timestamp = currentTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = verify(
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890"
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankSignature_returnsError() {
        val result = verify(
            payload = "{\"event\":\"payment.success\"}",
            signature = "   ",
            timestamp = "1234567890"
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withMissingTimestamp_returnsError() {
        val result = verify(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = null
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankTimestamp_returnsError() {
        val result = verify(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = ""
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withInvalidTimestampFormat_returnsError() {
        val result = verify(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = "not_a_number"
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Invalid timestamp format", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withExpiredTimestamp_returnsError() {
        val expiredTimestamp = (System.currentTimeMillis() / 1000) - 600 // 10 minutes ago
        
        val result = verify(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = expiredTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Timestamp outside allowed window (replay attack suspected)", 
            (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withInvalidSignature_returnsError() {
        val currentTimestamp = System.currentTimeMillis() / 1000
        
        val result = verify(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=invalid_signature_12345",
            timestamp = currentTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Invalid signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withTamperedPayload_returnsError() {
        val currentTimestamp = System.currentTimeMillis() / 1000
        val originalPayload = "{\"event\":\"payment.success\"}"
        val signature = sign(originalPayload, currentTimestamp.toString())
        
        val tamperedPayload = "{\"event\":\"payment.success\",\"amount\":999999}"
        
        val result = verify(
            payload = tamperedPayload,
            signature = signature,
            timestamp = currentTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
    }

    @Test
    fun verifyWebhook_withoutSha256Prefix_stillWorks() {
        val payload = "{\"event\":\"payment.success\"}"
        val currentTimestamp = System.currentTimeMillis() / 1000
        val hash = sign(payload, currentTimestamp.toString())
            .removePrefix("sha256=")
        
        val result = verify(
            payload = payload,
            signature = hash,
            timestamp = currentTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun generateTestSignature_producesConsistentOutput() {
        val payload = "test_payload"
        val timestamp = "1234567890"
        
        val signature1 = sign(payload, timestamp)
        val signature2 = sign(payload, timestamp)
        
        assertEquals(signature1, signature2)
    }

    @Test
    fun generateTestSignature_differentTimestamp_differentOutput() {
        val payload = "test_payload"
        
        val signature1 = sign(payload, "1234567890")
        val signature2 = sign(payload, "1234567891")
        
        assertNotEquals(signature1, signature2)
    }

    @Test
    fun verifyWebhook_withFutureTimestamp_withinTolerance_passes() {
        val nearFutureTimestamp = (System.currentTimeMillis() / 1000) + 60 // 1 minute in future
        
        val payload = "{\"event\":\"payment.success\"}"
        val signature = sign(payload, nearFutureTimestamp.toString())
        
        val result = verify(
            payload = payload,
            signature = signature,
            timestamp = nearFutureTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }
}
