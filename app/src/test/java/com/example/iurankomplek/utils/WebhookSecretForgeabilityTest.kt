package com.example.iurankomplek.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The webhook secret is a compile-time constant, so anything shipped in the APK can be
 * extracted. This test pins the current state so the forgery path stays visible until a
 * real secret is injected at build time.
 */
class WebhookSecretForgeabilityTest {

    @Test
    fun `the fallback secret is a committed placeholder, not a real secret`() {
        assertEquals(
            "whsec_placeholder_replace_in_production",
            Constants.Security.WEBHOOK_SECRET_KEY
        )
    }

    @Test
    fun `a signature minted from the public placeholder is accepted as authentic`() {
        val payload = """{"eventType":"payment_completed","transactionId":"txn_1"}"""
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val signature = WebhookSecurityUtil.generateTestSignature(payload, timestamp)
        val result = WebhookSecurityUtil.verifyWebhook(payload, signature, timestamp)

        assertTrue(
            "an attacker holding only the committed placeholder can forge this signature",
            result is WebhookSecurityUtil.VerificationResult.Success
        )
    }

    @Test
    fun `forgery is still rejected once a signature is tampered with`() {
        val payload = """{"eventType":"payment_completed","transactionId":"txn_1"}"""
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val signature = WebhookSecurityUtil.generateTestSignature(payload, timestamp)
            .replace("sha256=", "sha256=00")

        val result = WebhookSecurityUtil.verifyWebhook(payload, signature, timestamp)

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
    }

    @Test
    fun `replay outside the tolerance window is rejected`() {
        val payload = """{"eventType":"payment_completed","transactionId":"txn_1"}"""
        val staleTimestamp =
            ((System.currentTimeMillis() / 1000) - Constants.Security.WEBHOOK_TIMESTAMP_TOLERANCE_MS / 1000 - 60).toString()

        val signature = WebhookSecurityUtil.generateTestSignature(payload, staleTimestamp)
        val result = WebhookSecurityUtil.verifyWebhook(payload, signature, staleTimestamp)

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
        assertTrue((result as WebhookSecurityUtil.VerificationResult.Error).reason.contains("replay"))
    }
}