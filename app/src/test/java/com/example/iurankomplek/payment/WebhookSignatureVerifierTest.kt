package com.example.iurankomplek.payment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebhookSignatureVerifierTest {
    
    private lateinit var verifier: WebhookSignatureVerifier
    private lateinit var mockSecurityConfig: MockWebhookSecurityConfig
    private val testSecret = "test_webhook_secret_key_for_testing"
    private val testPayload = """{"eventType":"payment.success","transactionId":"txn_123","metadata":{"key":"value"}}"""
    
    @Before
    fun setup() {
        mockSecurityConfig = MockWebhookSecurityConfig(testSecret)
        verifier = WebhookSignatureVerifier(mockSecurityConfig)
    }
    
    @Test
    fun `verifyWebhookSignature returns Valid for correct signature`() {
        val correctSignature = computeTestSignature(testPayload, testSecret)
        val result = verifier.verifyWebhookSignature(testPayload, correctSignature)
        
        assertTrue("Result should be Valid", result is WebhookVerificationResult.Valid)
    }
    
    @Test
    fun `verifyWebhookSignature returns Invalid for incorrect signature`() {
        val incorrectSignature = "invalid_signature_base64=="
        val result = verifier.verifyWebhookSignature(testPayload, incorrectSignature)
        
        assertTrue("Result should be Invalid", result is WebhookVerificationResult.Invalid)
        assertEquals("Should have reason", "Signature mismatch", (result as WebhookVerificationResult.Invalid).reason)
    }
    
    @Test
    fun `verifyWebhookSignature returns Invalid for missing signature`() {
        val result = verifier.verifyWebhookSignature(testPayload, null)
        
        assertTrue("Result should be Invalid", result is WebhookVerificationResult.Invalid)
        assertEquals("Should have reason", "Missing signature", (result as WebhookVerificationResult.Invalid).reason)
    }
    
    @Test
    fun `verifyWebhookSignature returns Invalid for empty payload`() {
        val result = verifier.verifyWebhookSignature("", "some_signature")
        
        assertTrue("Result should be Invalid", result is WebhookVerificationResult.Invalid)
        assertEquals("Should have reason", "Empty payload", (result as WebhookVerificationResult.Invalid).reason)
    }
    
    @Test
    fun `verifyWebhookSignature returns Invalid for blank signature`() {
        val result = verifier.verifyWebhookSignature(testPayload, "")
        
        assertTrue("Result should be Invalid", result is WebhookVerificationResult.Invalid)
        assertEquals("Should have reason", "Missing signature", (result as WebhookVerificationResult.Invalid).reason)
    }
    
    @Test
    fun `verifyWebhookSignature returns Skipped when secret not configured`() {
        mockSecurityConfig.setSecret(null)
        val result = verifier.verifyWebhookSignature(testPayload, "some_signature")
        
        assertTrue("Result should be Skipped", result is WebhookVerificationResult.Skipped)
        assertEquals("Should have reason", "Secret key not configured", (result as WebhookVerificationResult.Skipped).reason)
    }
    
    @Test
    fun `verifyWebhookSignature produces consistent signatures for same payload`() {
        val signature1 = computeTestSignature(testPayload, testSecret)
        val signature2 = computeTestSignature(testPayload, testSecret)
        
        assertEquals("Signatures should be consistent", signature1, signature2)
    }
    
    @Test
    fun `verifyWebhookSignature produces different signatures for different payloads`() {
        val payload1 = """{"eventType":"payment.success"}"""
        val payload2 = """{"eventType":"payment.failed"}"""
        
        val signature1 = computeTestSignature(payload1, testSecret)
        val signature2 = computeTestSignature(payload2, testSecret)
        
        assertFalse("Signatures should be different", signature1 == signature2)
    }
    
    @Test
    fun `verifyWebhookSignature produces different signatures for different secrets`() {
        val secret1 = "secret_one"
        val secret2 = "secret_two"
        
        val signature1 = computeTestSignature(testPayload, secret1)
        val signature2 = computeTestSignature(testPayload, secret2)
        
        assertFalse("Signatures should be different", signature1 == signature2)
    }
    
    @Test
    fun `extractSignature extracts signature from header with prefix`() {
        val headers = mapOf(
            "X-Webhook-Signature" to "sha256=abc123def456"
        )
        
        val signature = WebhookSignatureVerifier.extractSignature(headers)
        
        assertEquals("Should extract signature without prefix", "abc123def456", signature)
    }
    
    @Test
    fun `extractSignature extracts signature from lowercase header name`() {
        val headers = mapOf(
            "x-webhook-signature" to "sha256=abc123def456"
        )
        
        val signature = WebhookSignatureVerifier.extractSignature(headers)
        
        assertEquals("Should extract signature from lowercase header", "abc123def456", signature)
    }
    
    @Test
    fun `extractSignature returns null when header not present`() {
        val headers = mapOf(
            "Content-Type" to "application/json"
        )
        
        val signature = WebhookSignatureVerifier.extractSignature(headers)
        
        assertEquals("Should return null when header not found", null, signature)
    }
    
    @Test
    fun `extractSignature returns null when header missing prefix`() {
        val headers = mapOf(
            "X-Webhook-Signature" to "abc123def456"
        )
        
        val signature = WebhookSignatureVerifier.extractSignature(headers)
        
        assertEquals("Should return null when prefix missing", null, signature)
    }
    
    @Test
    fun `constant time comparison is secure against timing attacks`() {
        val verifier = WebhookSignatureVerifier(mockSecurityConfig)
        val correctSignature = computeTestSignature(testPayload, testSecret)
        
        val validStart = System.nanoTime()
        val validResult = verifier.verifyWebhookSignature(testPayload, correctSignature)
        val validEnd = System.nanoTime()
        
        val invalidStart = System.nanoTime()
        val invalidResult = verifier.verifyWebhookSignature(testPayload, "wrong_signature")
        val invalidEnd = System.nanoTime()
        
        val validDuration = validEnd - validStart
        val invalidDuration = invalidEnd - invalidStart
        
        assertTrue("Valid result should be Valid", validResult is WebhookVerificationResult.Valid)
        assertTrue("Invalid result should be Invalid", invalidResult is WebhookVerificationResult.Invalid)
        
        val ratio = invalidDuration.toDouble() / validDuration.toDouble()
        assertTrue("Timing should be similar (ratio < 2)", ratio < 2.0)
    }
    
    private fun computeTestSignature(payload: String, secret: String): String {
        return verifier.javaClass.getDeclaredMethod("computeSignature", String::class.java, String::class.java)
            .apply { isAccessible = true }
            .invoke(verifier, payload, secret) as String
    }
    
    class MockWebhookSecurityConfig(private var secret: String?) : WebhookSecurityConfig {
        fun setSecret(newSecret: String?) {
            secret = newSecret
        }
        
        override fun getWebhookSecret(): String? = secret
    }
}
