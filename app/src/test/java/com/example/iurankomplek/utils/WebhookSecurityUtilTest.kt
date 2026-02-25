package com.example.iurankomplek.utils

import org.junit.Assert.*
import org.junit.Test

class WebhookSecurityUtilTest {

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val currentTimestamp = System.currentTimeMillis() / 1000
        val signature = WebhookSecurityUtil.generateTestSignature(payload, currentTimestamp.toString())
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = currentTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890"
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "   ",
            timestamp = "1234567890"
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withMissingTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = null
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = ""
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withInvalidTimestampFormat_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
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
        
        val result = WebhookSecurityUtil.verifyWebhook(
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
        
        val result = WebhookSecurityUtil.verifyWebhook(
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
        val signature = WebhookSecurityUtil.generateTestSignature(originalPayload, currentTimestamp.toString())
        
        val tamperedPayload = "{\"event\":\"payment.success\",\"amount\":999999}"
        
        val result = WebhookSecurityUtil.verifyWebhook(
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
        val hash = WebhookSecurityUtil.generateTestSignature(payload, currentTimestamp.toString())
            .removePrefix("sha256=")
        
        val result = WebhookSecurityUtil.verifyWebhook(
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
        
        val signature1 = WebhookSecurityUtil.generateTestSignature(payload, timestamp)
        val signature2 = WebhookSecurityUtil.generateTestSignature(payload, timestamp)
        
        assertEquals(signature1, signature2)
    }

    @Test
    fun generateTestSignature_differentTimestamp_differentOutput() {
        val payload = "test_payload"
        
        val signature1 = WebhookSecurityUtil.generateTestSignature(payload, "1234567890")
        val signature2 = WebhookSecurityUtil.generateTestSignature(payload, "1234567891")
        
        assertNotEquals(signature1, signature2)
    }

    @Test
    fun verifyWebhook_withFutureTimestamp_withinTolerance_passes() {
        val nearFutureTimestamp = (System.currentTimeMillis() / 1000) + 60 // 1 minute in future
        
        val payload = "{\"event\":\"payment.success\"}"
        val signature = WebhookSecurityUtil.generateTestSignature(payload, nearFutureTimestamp.toString())
        
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = nearFutureTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }
}
