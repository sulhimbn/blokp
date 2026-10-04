package com.example.iurankomplek.security

import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.WebhookSecurityUtil
import com.example.iurankomplek.utils.WebhookSecurityUtil.VerificationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the webhook signature gate.
 *
 * The placeholder secret is compiled into the APK, so a release build that falls back
 * to it would accept signatures anybody can forge offline. The secret is passed in
 * explicitly so both build variants can exercise the policy.
 */
class WebhookSecretPolicyTest {

    private fun nowSeconds() = System.currentTimeMillis() / 1000

    @Test
    fun `a real injected secret is usable in release builds`() {
        assertTrue(WebhookSecurityUtil.isSecretUsable(TRUSTED_SECRET, isDebugBuild = false))
    }

    @Test
    fun `the compiled-in placeholder secret is rejected in release builds`() {
        assertTrue(
            !WebhookSecurityUtil.isSecretUsable(
                Constants.Security.WEBHOOK_SECRET_KEY,
                isDebugBuild = false
            )
        )
    }

    @Test
    fun `a blank secret is rejected in release builds`() {
        assertTrue(!WebhookSecurityUtil.isSecretUsable("", isDebugBuild = false))
        assertTrue(!WebhookSecurityUtil.isSecretUsable("   ", isDebugBuild = false))
    }

    @Test
    fun `debug builds may still use the placeholder so local flows work`() {
        assertTrue(
            WebhookSecurityUtil.isSecretUsable(
                Constants.Security.WEBHOOK_SECRET_KEY,
                isDebugBuild = true
            )
        )
    }

    @Test
    fun `a correctly signed webhook is accepted`() {
        val timestamp = nowSeconds().toString()
        val signature = WebhookSecurityUtil.generateTestSignature(PAYLOAD, timestamp, TRUSTED_SECRET)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = PAYLOAD,
            signature = signature,
            timestamp = timestamp,
            secret = TRUSTED_SECRET
        )

        assertTrue("expected success but got $result", result is VerificationResult.Success)
    }

    @Test
    fun `a tampered payload is rejected even with a valid-looking signature`() {
        val timestamp = nowSeconds().toString()
        val signature = WebhookSecurityUtil.generateTestSignature(PAYLOAD, timestamp, TRUSTED_SECRET)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = """{"event":"payment.refunded","id":"txn-1"}""",
            signature = signature,
            timestamp = timestamp,
            secret = TRUSTED_SECRET
        )

        assertTrue(result is VerificationResult.Error)
    }

    @Test
    fun `a replayed webhook outside the tolerance window is rejected`() {
        val staleTimestamp = (nowSeconds() - 3600).toString()
        val signature =
            WebhookSecurityUtil.generateTestSignature(PAYLOAD, staleTimestamp, TRUSTED_SECRET)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = PAYLOAD,
            signature = signature,
            timestamp = staleTimestamp,
            secret = TRUSTED_SECRET
        )

        assertEquals(
            "Timestamp outside allowed window (replay attack suspected)",
            (result as VerificationResult.Error).reason
        )
    }

    @Test
    fun `a missing signature header is rejected`() {
        val timestamp = nowSeconds().toString()

        val missingSignature = WebhookSecurityUtil.verifyWebhook(
            payload = PAYLOAD,
            signature = null,
            timestamp = timestamp,
            secret = TRUSTED_SECRET
        )
        val missingTimestamp = WebhookSecurityUtil.verifyWebhook(
            payload = PAYLOAD,
            signature = "sha256=abc",
            timestamp = null,
            secret = TRUSTED_SECRET
        )

        assertEquals("Missing signature", (missingSignature as VerificationResult.Error).reason)
        assertEquals("Missing timestamp", (missingTimestamp as VerificationResult.Error).reason)
    }

    @Test
    fun `a malformed timestamp is rejected`() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = PAYLOAD,
            signature = "sha256=abc",
            timestamp = "not-a-number",
            secret = TRUSTED_SECRET
        )

        assertEquals(
            "Invalid timestamp format",
            (result as VerificationResult.Error).reason
        )
    }

    private companion object {
        const val TRUSTED_SECRET = "whsec_unit_test_secret"
        const val PAYLOAD = """{"event":"payment.success","id":"txn-1"}"""
    }
}