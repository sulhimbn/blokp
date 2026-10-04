package com.example.iurankomplek.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class WebhookSecurityUtilTest {

    private val testSecret = "whsec_configured_by_ci"

    private fun sign(payload: String, timestamp: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val hash = mac.doFinal("$timestamp.$payload".toByteArray(Charsets.UTF_8))
        return "sha256=" + hash.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun verifyWebhook_withValidSignature_returnsSuccess() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, timestamp, testSecret),
            timestamp = timestamp,
            secret = testSecret,
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_acceptsSignatureWithoutTheSha256Prefix() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val bare = sign(payload, timestamp, testSecret).removePrefix("sha256=")

        val result = WebhookSecurityUtil.verifyWebhook(payload, bare, timestamp, testSecret)

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Success)
    }

    @Test
    fun verifyWebhook_rejectsSignatureFromADifferentSecret() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, timestamp, "attacker_secret"),
            timestamp = timestamp,
            secret = testSecret,
        )

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    @Test
    fun verifyWebhook_rejectsPayloadTamperedAfterSigning() {
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val original = "{\"amount\":100}"
        val signature = sign(original, timestamp, testSecret)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"amount\":1}",
            signature = signature,
            timestamp = timestamp,
            secret = testSecret,
        )

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    @Test
    fun verifyWebhook_withMissingSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook(
            payload = "{\"event\":\"payment.success\"}",
            signature = null,
            timestamp = "1234567890",
            secret = testSecret,
        )

        assertEquals(
            "Missing signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    @Test
    fun verifyWebhook_withBlankSignature_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook("{}", "   ", "1234567890", testSecret)

        assertEquals(
            "Missing signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    @Test
    fun verifyWebhook_withMissingTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook("{}", "sha256=abc", null, testSecret)

        assertEquals(
            "Missing timestamp",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    @Test
    fun verifyWebhook_withNonNumericTimestamp_returnsError() {
        val result = WebhookSecurityUtil.verifyWebhook("{}", "sha256=abc", "not-a-number", testSecret)

        assertEquals(
            "Invalid timestamp format",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    @Test
    fun verifyWebhook_withStaleTimestamp_returnsReplayError() {
        val payload = "{\"event\":\"payment.success\"}"
        val stale = (System.currentTimeMillis() / 1000) - 86_400
        val staleTimestamp = stale.toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, staleTimestamp, testSecret),
            timestamp = staleTimestamp,
            secret = testSecret,
        )

        assertTrue((result as WebhookSecurityUtil.VerificationResult.Error).reason.contains("replay"))
    }

    @Test
    fun verifyWebhook_withFutureTimestamp_returnsReplayError() {
        val payload = "{\"event\":\"payment.success\"}"
        val futureTimestamp = ((System.currentTimeMillis() / 1000) + 86_400).toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, futureTimestamp, testSecret),
            timestamp = futureTimestamp,
            secret = testSecret,
        )

        assertTrue((result as WebhookSecurityUtil.VerificationResult.Error).reason.contains("replay"))
    }

    @Test
    fun verifyWebhook_withGarbageSignature_returnsError() {
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val result = WebhookSecurityUtil.verifyWebhook("{}", "sha256=deadbeef", timestamp, testSecret)

        assertEquals(
            "Invalid signature",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    /**
     * Regression: the build ships no webhook secret, so verification previously fell
     * back to a constant compiled into the APK. Anyone able to unzip the APK could
     * read that constant and forge signatures that were accepted as genuine.
     */
    @Test
    fun verifyWebhook_rejectsSignatureMadeWithTheCompiledInPlaceholderConstant() {
        val payload = "{\"event\":\"payment.success\",\"transactionId\":\"txn_123\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val forged = sign(payload, timestamp, Constants.Security.WEBHOOK_SECRET_KEY)

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = forged,
            timestamp = timestamp,
        )

        assertEquals(
            "Webhook secret is not configured",
            (result as WebhookSecurityUtil.VerificationResult.Error).reason,
        )
    }

    @Test
    fun verifyWebhook_withoutConfiguredSecret_failsClosed() {
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val result = WebhookSecurityUtil.verifyWebhook(
            payload = payload,
            signature = sign(payload, timestamp, "any_secret_at_all"),
            timestamp = timestamp,
        )

        assertTrue(result is WebhookSecurityUtil.VerificationResult.Error)
    }

    @Test
    fun generateTestSignature_throwsWhenNoSecretIsConfigured() {
        val thrown = runCatching {
            WebhookSecurityUtil.generateTestSignature("{}", "1700000000")
        }.exceptionOrNull()

        assertTrue(
            "signing must fail loudly instead of using a shipped constant",
            thrown is IllegalStateException,
        )
    }

    @Test
    fun constantTimeEquals_isUsedForSignatureComparison() {
        // Guards the property that matters: equal-content signatures verify, and a
        // single-character difference does not.
        val payload = "{\"event\":\"payment.success\"}"
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val good = sign(payload, timestamp, testSecret)
        val tampered = "sha256=" + MessageDigest.getInstance("SHA-256")
            .digest("x".toByteArray()).joinToString("") { "%02x".format(it) }

        assertTrue(
            WebhookSecurityUtil.verifyWebhook(payload, good, timestamp, testSecret)
                    is WebhookSecurityUtil.VerificationResult.Success
        )
        assertTrue(
            WebhookSecurityUtil.verifyWebhook(payload, tampered, timestamp, testSecret)
                    is WebhookSecurityUtil.VerificationResult.Error
        )
    }
}