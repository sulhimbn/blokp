package com.example.iurankomplek.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers webhook verification: HMAC signature checking, replay protection and
 * fail-closed behaviour. Signature mechanics are exercised with an explicit
 * secret so the assertions do not depend on build-time configuration; the
 * build-configured path is covered by the fail-closed tests below.
 */
class WebhookSecurityUtilTest {

    private val testSecret = "unit-test-secret-not-used-in-production"

    private val secretConfigured =
        com.example.iurankomplek.BuildConfig.WEBHOOK_SECRET.isNotBlank()

    private fun sign(payload: String, timestamp: String): String =
        WebhookSecurityUtil.generateSignature(payload, timestamp, testSecret)!!

    private fun currentTimestamp(): String = (System.currentTimeMillis() / 1000).toString()

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val timestamp = currentTimestamp()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload, sign(payload, timestamp), timestamp, testSecret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withoutConfiguredSecret_failsClosed() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=" + "0".repeat(64),
            timestamp = currentTimestamp(),
            secret = null
        )

        assertTrue(
            "Verification must fail when no webhook secret is configured",
            result is WebhookSecurityUtil.VerificationResult.Error
        )
        assertEquals(
            "Webhook verification unavailable",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_buildConfiguredSecretPath_isRejectedWhenUnconfigured() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=deadbeef",
            timestamp = currentTimestamp()
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        if (!secretConfigured) {
            assertEquals(
                "Webhook verification unavailable",
                (result as WebhookSecurityUtil.VerificationResult.Error).reason
            )
        }
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890"
        )

        assertEquals(
            "Missing signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withBlankSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "   ",
            timestamp = "1234567890"
        )

        assertEquals(
            "Missing signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withMissingTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = null
        )

        assertEquals(
            "Missing timestamp",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withBlankTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = ""
        )

        assertEquals(
            "Missing timestamp",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withInvalidTimestampFormat_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = "not_a_number"
        )

        assertEquals(
            "Invalid timestamp format",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withExpiredTimestamp_rejectsReplay() {
        val expiredTimestamp = (System.currentTimeMillis() / 1000) - 600

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = expiredTimestamp.toString()
        )

        assertEquals(
            "Timestamp outside allowed window (replay attack suspected)",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withFutureTimestampWithinTolerance_isAccepted() {
        val nearFuture = (System.currentTimeMillis() / 1000) + 60
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = nearFuture.toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload, sign(payload, timestamp), timestamp, testSecret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withInvalidSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=invalid_signature_12345",
            timestamp = currentTimestamp(),
            secret = testSecret
        )

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withSignatureFromDifferentSecret_returnsError() {
        val timestamp = currentTimestamp()
        val payload = "{\"event\":\"payment.success\"}"
        val forged = WebhookSecurityUtil.generateSignature(payload, timestamp, "attacker-secret")!!

        val result = WebhookSecurityUtil.verifyWebhook(payload, forged, timestamp, testSecret)

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withTamperedPayload_returnsError() {
        val timestamp = currentTimestamp()
        val originalPayload = "{\"event\":\"payment.success\"}"
        val signature = sign(originalPayload, timestamp)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\",\"amount\":999999}",
            signature = signature,
            timestamp = timestamp,
            secret = testSecret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
    }

    @Test
    fun verifyWebhook_withoutSha256Prefix_stillWorks() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = currentTimestamp()
        val hash = sign(payload, timestamp).removePrefix("sha256=")

        val result = WebhookSecurityUtil.verifyWebhook(payload, hash, timestamp, testSecret)

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun generateSignature_isDeterministicAndFormatStable() {
        val signature = sign("test_payload", "1234567890")

        assertEquals(
            signature,
            WebhookSecurityUtil.generateSignature("test_payload", "1234567890", testSecret)
        )
        assertTrue(signature.startsWith("sha256="))
    }

    @Test
    fun generateSignature_differentTimestamp_differentOutput() {
        assertNotEquals(
            WebhookSecurityUtil.generateSignature("test_payload", "1234567890", testSecret),
            WebhookSecurityUtil.generateSignature("test_payload", "1234567891", testSecret)
        )
    }

    @Test
    fun generateSignature_withoutSecret_returnsNull() {
        assertNull(WebhookSecurityUtil.generateSignature("test_payload", "1234567890", null))
    }
}