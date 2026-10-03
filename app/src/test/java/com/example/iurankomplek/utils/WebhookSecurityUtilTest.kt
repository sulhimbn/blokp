package com.example.iurankomplek.utils

import org.junit.Assert.*
import org.junit.Test

class WebhookSecurityUtilTest {

    private val testSecret = "whsec_unit_test_secret"

    private fun reason(result: WebhookSecurityUtil.VerificationResult): String =
        (result as WebhookSecurityUtil.VerificationResult.Error).reason

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val currentTimestamp = System.currentTimeMillis() / 1000
        val signature = WebhookSecurityUtil.generateSignature(testSecret, payload, currentTimestamp.toString())
        
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
            payload = payload,
            signature = signature,
            timestamp = currentTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890"
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
            payload = "{\"event\":\"payment.success\"}",
            signature = "   ",
            timestamp = "1234567890"
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withMissingTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = null
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = ""
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withInvalidTimestampFormat_returnsError() {
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
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
        
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
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
        
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
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
        val signature = WebhookSecurityUtil.generateSignature(testSecret, originalPayload, currentTimestamp.toString())
        
        val tamperedPayload = "{\"event\":\"payment.success\",\"amount\":999999}"
        
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
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
        val hash = WebhookSecurityUtil.generateSignature(testSecret, payload, currentTimestamp.toString())
            .removePrefix("sha256=")
        
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
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
        
        val signature1 = WebhookSecurityUtil.generateSignature(testSecret, payload, timestamp)
        val signature2 = WebhookSecurityUtil.generateSignature(testSecret, payload, timestamp)
        
        assertEquals(signature1, signature2)
    }

    @Test
    fun generateTestSignature_differentTimestamp_differentOutput() {
        val payload = "test_payload"
        
        val signature1 = WebhookSecurityUtil.generateSignature(testSecret, payload, "1234567890")
        val signature2 = WebhookSecurityUtil.generateSignature(testSecret, payload, "1234567891")
        
        assertNotEquals(signature1, signature2)
    }

    @Test
    fun verifyWebhook_withFutureTimestamp_withinTolerance_passes() {
        val nearFutureTimestamp = (System.currentTimeMillis() / 1000) + 60 // 1 minute in future
        
        val payload = "{\"event\":\"payment.success\"}"
        val signature = WebhookSecurityUtil.generateSignature(testSecret, payload, nearFutureTimestamp.toString())
        
        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
            payload = payload,
            signature = signature,
            timestamp = nearFutureTimestamp.toString()
        )
        
        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_failsClosedWhenSecretIsNotConfigured() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = WebhookSecurityUtil.generateSignature(
                "whsec_placeholder_replace_in_production",
                payload,
                timestamp
            ),
            timestamp = timestamp
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Webhook secret is not configured", reason(result))
    }

    @Test
    fun verifyWebhook_rejectsSignatureMadeWithADifferentSecret() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val forged = WebhookSecurityUtil.generateSignature("attacker-secret", payload, timestamp)

        val result = WebhookSecurityUtil.verifyWith(
            secret = testSecret,
            payload = payload,
            signature = forged,
            timestamp = timestamp
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertEquals("Invalid signature", reason(result))
    }
}
