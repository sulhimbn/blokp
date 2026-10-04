package com.example.iurankomplek.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebhookSecurityUtilTest {

    private fun nowSeconds() = System.currentTimeMillis() / 1000

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val timestamp = nowSeconds().toString()
        val signature = WebhookSecurityUtil.generateTestSignature(payload, timestamp, TRUSTED_SECRET)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = timestamp,
            secret = TRUSTED_SECRET
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890",
            secret = TRUSTED_SECRET
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
            timestamp = "1234567890",
            secret = TRUSTED_SECRET
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
            timestamp = null,
            secret = TRUSTED_SECRET
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
            timestamp = "",
            secret = TRUSTED_SECRET
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
            timestamp = "not_a_number",
            secret = TRUSTED_SECRET
        )

        assertEquals(
            "Invalid timestamp format",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withExpiredTimestamp_returnsError() {
        val expiredTimestamp = (nowSeconds() - 600).toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=abc123",
            timestamp = expiredTimestamp,
            secret = TRUSTED_SECRET
        )

        assertEquals(
            "Timestamp outside allowed window (replay attack suspected)",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withInvalidSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = "sha256=invalid_signature",
            timestamp = nowSeconds().toString(),
            secret = TRUSTED_SECRET
        )

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withTamperedPayload_returnsError() {
        val original = "{\"event\":\"payment.success\",\"amount\":100}"
        val timestamp = nowSeconds().toString()
        val signature =
            WebhookSecurityUtil.generateTestSignature(original, timestamp, TRUSTED_SECRET)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\",\"amount\":999999}",
            signature = signature,
            timestamp = timestamp,
            secret = TRUSTED_SECRET
        )

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withoutSha256Prefix_stillWorks() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = nowSeconds().toString()
        val signature = WebhookSecurityUtil
            .generateTestSignature(payload, timestamp, TRUSTED_SECRET)
            .removePrefix("sha256=")

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = timestamp,
            secret = TRUSTED_SECRET
        )

        assertTrue(
            "a bare hex digest must still verify",
            result is WebhookSecurityUtil.VerificationResult.Success
        )
    }

    @Test
    fun verifyWebhook_withDifferentSecret_returnsError() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = nowSeconds().toString()
        val signature =
            WebhookSecurityUtil.generateTestSignature(payload, timestamp, "another_secret")

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = timestamp,
            secret = TRUSTED_SECRET
        )

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason
        )
    }

    @Test
    fun verifyWebhook_withFutureTimestamp_withinTolerance_passes() {
        val nearFutureTimestamp = (nowSeconds() + 60).toString()
        val payload = "{\"event\":\"payment.success\"}"
        val signature = WebhookSecurityUtil
            .generateTestSignature(payload, nearFutureTimestamp, TRUSTED_SECRET)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = signature,
            timestamp = nearFutureTimestamp,
            secret = TRUSTED_SECRET
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun generateTestSignature_changesWithTimestamp() {
        val payload = "{\"event\":\"payment.success\"}"

        val signature1 =
            WebhookSecurityUtil.generateTestSignature(payload, "1234567890", TRUSTED_SECRET)
        val signature2 =
            WebhookSecurityUtil.generateTestSignature(payload, "1234567891", TRUSTED_SECRET)

        assertNotEquals(signature1, signature2)
    }

    private companion object {
        const val TRUSTED_SECRET = "whsec_unit_test_secret"
    }
}