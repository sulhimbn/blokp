package com.example.iurankomplek.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebhookSecurityUtilTest {

    private val secret = "unit-test-webhook-secret"

    private fun nowSeconds(): String =
        (System.currentTimeMillis() / 1000).toString()

    private fun sign(payload: String, timestamp: String) =
        WebhookSecurityUtil.generateSignature(payload, timestamp, secret)

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val ts = nowSeconds()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, ts),
            timestamp = ts,
            secret = secret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890",
            secret = secret
        )

        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "   ",
            timestamp = "1234567890",
            secret = secret
        )

        assertEquals("Missing signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withMissingTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = null,
            secret = secret
        )

        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withBlankTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = "",
            secret = secret
        )

        assertEquals("Missing timestamp", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withInvalidTimestampFormat_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = "not_a_number",
            secret = secret
        )

        assertEquals(
            "Invalid timestamp format",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withExpiredTimestamp_isRejectedAsReplay() {
        val expired = (System.currentTimeMillis() / 1000) - 600

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = sign("{\"event\":\"payment.success\"}", expired.toString()),
            timestamp = expired.toString(),
            secret = secret
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

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, nearFuture.toString()),
            timestamp = nearFuture.toString(),
            secret = secret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withInvalidSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=invalid_signature_12345",
            timestamp = nowSeconds(),
            secret = secret
        )

        assertEquals("Invalid signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun verifyWebhook_withTamperedPayload_isRejected() {
        val ts = nowSeconds()
        val original = "{\"event\":\"payment.success\"}"
        val signature = sign(original, ts)
        val tampered = "{\"event\":\"payment.success\",\"amount\":999999}"

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = tampered,
            signature = signature,
            timestamp = ts,
            secret = secret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
    }

    @Test
    fun verifyWebhook_withoutSha256Prefix_stillVerifies() {
        val payload = "{\"event\":\"payment.success\"}"
        val ts = nowSeconds()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, ts).removePrefix("sha256="),
            timestamp = ts,
            secret = secret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun generateSignature_isDeterministicAndTimestampBound() {
        val signature1 = WebhookSecurityUtil.generateSignature("test_payload", "1234567890", secret)
        val signature2 = WebhookSecurityUtil.generateSignature("test_payload", "1234567890", secret)

        assertEquals(signature1, signature2)
        assertTrue(signature1.startsWith("sha256="))

        assertNotEquals(
            signature1,
            WebhookSecurityUtil.generateSignature("test_payload", "1234567891", secret)
        )
    }

    @Test
    fun verifyWebhook_signedWithADifferentSecret_isRejected() {
        val payload = "{\"event\":\"payment.success\"}"
        val ts = nowSeconds()
        val forged = WebhookSecurityUtil.generateSignature(payload, ts, "attacker-guessed-secret")

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = forged,
            timestamp = ts,
            secret = secret
        )

        assertEquals("Invalid signature", (result as WebhookSecurityUtil.VerificationResult.Error).reason)
    }

    @Test
    fun theOldPublicPlaceholderSecretIsNoLongerAccepted() {
        val payload = "{\"event\":\"payment.success\"}"
        val ts = nowSeconds()
        val forged = WebhookSecurityUtil.generateSignature(
            payload, ts, "whsec_placeholder_replace_in_production"
        )

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = forged,
            timestamp = ts,
            secret = secret
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
    }

    @Test
    fun verifyWebhook_withoutAConfiguredSecret_failsClosed() {
        val payload = "{\"event\":\"payment.success\"}"
        val ts = nowSeconds()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = WebhookSecurityUtil.generateSignature(payload, ts, "anything"),
            timestamp = ts
        )

        assertEquals(
            "Webhook secret not configured",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }
}
