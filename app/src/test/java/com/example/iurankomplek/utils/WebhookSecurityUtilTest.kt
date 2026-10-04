package com.example.iurankomplek.utils

import org.junit.Assert.*
import org.junit.Test

class WebhookSecurityUtilTest {

    private val secret = "unit-test-webhook-secret"

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val currentTimestamp = System.currentTimeMillis() / 1000
        val signature = WebhookSecurityUtil.generateTestSignature(payload, currentTimestamp.toString(), secret)
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = currentTimestamp.toString(),
            secret = secret
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890"
        ,
            secret = secret)
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "   ",
            timestamp = "1234567890"
        ,
            secret = secret)
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withMissingTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = null
        ,
            secret = secret)
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = ""
        ,
            secret = secret)
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withInvalidTimestampFormat_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = "not_a_number"
        ,
            secret = secret)
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Invalid timestamp format", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withExpiredTimestamp_returnsError() {
        val expiredTimestamp = (System.currentTimeMillis() / 1000) - 600 // 10 minutes ago
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = expiredTimestamp.toString(),
            secret = secret
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Timestamp outside allowed window (replay attack suspected)", 
            (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withInvalidSignature_returnsError() {
        val currentTimestamp = System.currentTimeMillis() / 1000
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=invalid_signature_12345",
            timestamp = currentTimestamp.toString(),
            secret = secret
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Invalid signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withTamperedPayload_returnsError() {
        val currentTimestamp = System.currentTimeMillis() / 1000
        val originalPayload = "{\"event\":\"payment.success\"}"
        val signature = WebhookSecurityUtil.generateTestSignature(originalPayload, currentTimestamp.toString(), secret)
        
        val tamperedPayload = "{\"event\":\"payment.success\",\"amount\":999999}"
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = tamperedPayload,
            signature = signature,
            timestamp = currentTimestamp.toString(),
            secret = secret
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
    }

    @Test
    fun verifyWebhook_withoutSha256Prefix_stillWorks() {
        val payload = "{\"event\":\"payment.success\"}"
        val currentTimestamp = System.currentTimeMillis() / 1000
        val hash = WebhookSecurityUtil.generateTestSignature(payload, currentTimestamp.toString(), secret)
            .removePrefix("sha256=")
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = hash,
            timestamp = currentTimestamp.toString(),
            secret = secret
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun generateTestSignature_producesConsistentOutput() {
        val payload = "test_payload"
        val timestamp = "1234567890"
        
        val signature1 = WebhookSecurityUtil.generateTestSignature(payload, timestamp, secret)
        val signature2 = WebhookSecurityUtil.generateTestSignature(payload, timestamp, secret)
        
        assertEquals(signature1, signature2)
    }

    @Test
    fun generateTestSignature_differentTimestamp_differentOutput() {
        val payload = "test_payload"
        
        val signature1 = WebhookSecurityUtil.generateTestSignature(payload, "1234567890", secret)
        val signature2 = WebhookSecurityUtil.generateTestSignature(payload, "1234567891", secret)
        
        assertNotEquals(signature1, signature2)
    }

    @Test
    fun verifyWebhook_withFutureTimestamp_withinTolerance_passes() {
        val nearFutureTimestamp = (System.currentTimeMillis() / 1000) + 60 // 1 minute in future
        
        val payload = "{\"event\":\"payment.success\"}"
        val signature = WebhookSecurityUtil.generateTestSignature(payload, nearFutureTimestamp.toString(), secret)
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = nearFutureTimestamp.toString(),
            secret = secret
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_rejectsSignatureMadeWithTheShippedPlaceholderSecret() {
        // Anyone can read Constants.Security.WEBHOOK_SECRET_KEY from a decompiled APK,
        // so a webhook signed with it must never be trusted.
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_attacker\"}"
        val currentTimestamp = System.currentTimeMillis() / 1000
        val forgedSignature = WebhookSecurityUtil.generateTestSignature(
            payload = payload,
            timestamp = currentTimestamp.toString(),
            secret = com.example.iurankomplek.utils.Constants.Security.WEBHOOK_SECRET_KEY
        )

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = forgedSignature,
            timestamp = currentTimestamp.toString()
        )

        assertTrue(
            "A webhook signed with the publicly known placeholder secret must be rejected",
            result is WebhookSecurityUtil.VerificationResult.Error
        )
    }

    @Test
    fun verifyWebhook_rejectsEverythingWhenNoSecretIsConfigured() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=deadbeef",
            timestamp = (System.currentTimeMillis() / 1000).toString(),
            secret = null
        )

        assertEquals(
            "Webhook verification is not configured",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }
}
