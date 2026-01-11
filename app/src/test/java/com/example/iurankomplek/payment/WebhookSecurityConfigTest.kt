package com.example.iurankomplek.payment

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebhookSecurityConfigTest {

    @Before
    fun setup() {
        WebhookSecurityConfig.clearSecret()
    }

    @After
    fun tearDown() {
        WebhookSecurityConfig.clearSecret()
        unmockkAll()
    }

    @Test
    fun `initializeSecret should set webhook secret`() {
        val secret = "test_secret_key_12345"

        WebhookSecurityConfig.initializeSecret(secret)

        assertEquals(secret, WebhookSecurityConfig.getWebhookSecret())
    }

    @Test
    fun `initializeSecret with null should clear secret`() {
        WebhookSecurityConfig.initializeSecret("initial_secret")
        assertTrue(WebhookSecurityConfig.isSecretConfigured())

        WebhookSecurityConfig.initializeSecret(null)

        assertFalse(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `initializeSecret with blank string should clear secret`() {
        WebhookSecurityConfig.initializeSecret("initial_secret")
        assertTrue(WebhookSecurityConfig.isSecretConfigured())

        WebhookSecurityConfig.initializeSecret("")

        assertFalse(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `getWebhookSecret should return initialized secret`() {
        val secret = "test_secret_key_12345"
        WebhookSecurityConfig.initializeSecret(secret)

        val result = WebhookSecurityConfig.getWebhookSecret()

        assertEquals(secret, result)
    }

    @Test
    fun `getWebhookSecret should load from environment when not initialized`() {
        mockkStatic("kotlin.system.System")
        every { System.getenv(any()) } returns "env_secret_key_67890"

        WebhookSecurityConfig.clearSecret()
        val result = WebhookSecurityConfig.getWebhookSecret()

        assertEquals("env_secret_key_67890", result)
    }

    @Test
    fun `getWebhookSecret should prefer initialized secret over environment`() {
        mockkStatic("kotlin.system.System")
        every { System.getenv(any()) } returns "env_secret_key"

        WebhookSecurityConfig.initializeSecret("initialized_secret")
        val result = WebhookSecurityConfig.getWebhookSecret()

        assertEquals("initialized_secret", result)
    }

    @Test
    fun `getWebhookSecret should return null when not initialized and no environment variable`() {
        mockkStatic("kotlin.system.System")
        every { System.getenv(any()) } returns null

        WebhookSecurityConfig.clearSecret()
        val result = WebhookSecurityConfig.getWebhookSecret()

        assertNull(result)
    }

    @Test
    fun `getWebhookSecret should return null when environment variable is blank`() {
        mockkStatic("kotlin.system.System")
        every { System.getenv(any()) } returns ""

        WebhookSecurityConfig.clearSecret()
        val result = WebhookSecurityConfig.getWebhookSecret()

        assertNull(result)
    }

    @Test
    fun `isSecretConfigured should return true when secret is initialized`() {
        WebhookSecurityConfig.initializeSecret("test_secret")

        assertTrue(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `isSecretConfigured should return true when secret is in environment`() {
        mockkStatic("kotlin.system.System")
        every { System.getenv(any()) } returns "env_secret"

        WebhookSecurityConfig.clearSecret()
        assertTrue(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `isSecretConfigured should return false when secret is null`() {
        mockkStatic("kotlin.system.System")
        every { System.getenv(any()) } returns null

        WebhookSecurityConfig.clearSecret()
        assertFalse(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `isSecretConfigured should return false when secret is blank`() {
        mockkStatic("kotlin.system.System")
        every { System.getenv(any()) } returns ""

        WebhookSecurityConfig.clearSecret()
        assertFalse(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `isSecretConfigured should return false after clearSecret`() {
        WebhookSecurityConfig.initializeSecret("test_secret")
        assertTrue(WebhookSecurityConfig.isSecretConfigured())

        WebhookSecurityConfig.clearSecret()

        assertFalse(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `clearSecret should remove initialized secret`() {
        WebhookSecurityConfig.initializeSecret("test_secret")
        assertEquals("test_secret", WebhookSecurityConfig.getWebhookSecret())

        WebhookSecurityConfig.clearSecret()

        assertNull(WebhookSecurityConfig.getWebhookSecret())
    }

    @Test
    fun `clearSecret should work when no secret is set`() {
        WebhookSecurityConfig.clearSecret()
        assertFalse(WebhookSecurityConfig.isSecretConfigured())

        WebhookSecurityConfig.clearSecret()

        assertFalse(WebhookSecurityConfig.isSecretConfigured())
    }

    @Test
    fun `should handle empty secret after clearSecret`() {
        WebhookSecurityConfig.initializeSecret("secret_1")
        WebhookSecurityConfig.clearSecret()

        val secret = WebhookSecurityConfig.getWebhookSecret()
        assertNull(secret)
    }
}
