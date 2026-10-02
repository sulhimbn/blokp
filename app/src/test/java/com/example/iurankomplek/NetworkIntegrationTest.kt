package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.network.SecurityConfig
import com.example.iurankomplek.utils.Constants
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

/**
 * Network wiring tests: Retrofit/OkHttp construction, base URL selection and the
 * production security configuration (certificate pinning + security headers).
 */
class NetworkIntegrationTest {

    @Test
    fun `ApiConfig should return a usable ApiService`() {
        val apiService = ApiConfig.getApiService()

        assertNotNull(apiService)
        assertTrue(ApiService::class.java.isAssignableFrom(apiService.javaClass))
    }

    @Test
    fun `ApiConfig should cache the ApiService instance`() {
        val first = ApiConfig.getApiService()
        val second = ApiConfig.getApiService()

        assertSame("ApiService should be a cached singleton", first, second)
    }

    @Test
    fun `ApiConfig debug base URL should target the spreadsheet id path`() {
        val retrofitField = ApiConfig::class.java.getDeclaredField("BASE_URL")
        retrofitField.isAccessible = true
        val baseUrl = retrofitField.get(null) as String

        assertTrue(
            "Base URL must end with a /data/<spreadsheet-id>/ path, was: $baseUrl",
            Regex("^http://[^/]+/data/[^/]+/$").matches(baseUrl)
        )
        assertTrue(
            "Debug builds must not target the production host over TLS pinning, was: $baseUrl",
            !baseUrl.startsWith("https://api.apispreadsheets.com")
        )
        assertTrue(
            "Debug base URL must carry a non-empty mock host, was: $baseUrl",
            baseUrl.removePrefix("http://").substringBefore("/").isNotBlank()
        )
    }

    @Test
    fun `ApiConfig should build Retrofit with a base URL ending in a slash`() {
        val retrofit = Retrofit.Builder().baseUrl("http://localhost:1/").build()

        assertEquals("/", retrofit.baseUrl().encodedPath)
    }

    @Test
    fun `SecurityConfig should pin the production API host`() {
        val client: OkHttpClient = SecurityConfig.getSecureOkHttpClient()

        val pinner = client.certificatePinner
        assertNotNull(pinner)
        val pins = pinner.findMatchingPins("api.apispreadsheets.com")
        assertTrue("Expected at least one pin for the production host", pins.isNotEmpty())
        assertTrue(
            "The configured pin must be the one enforced for the production host",
            pins.any { "sha256/${it.hash.base64()}" == Constants.Security.CERTIFICATE_PINNER }
        )
        assertTrue(
            "Pins must be scoped to the production API host only",
            pins.all { it.pattern == "api.apispreadsheets.com" }
        )
    }

    @Test
    fun `SecurityConfig should configure finite timeouts`() {
        val client = SecurityConfig.getSecureOkHttpClient()

        assertEquals(Constants.Network.CONNECT_TIMEOUT * 1000L, client.connectTimeoutMillis.toLong())
        assertEquals(Constants.Network.READ_TIMEOUT * 1000L, client.readTimeoutMillis.toLong())
        assertEquals(Constants.Network.WRITE_TIMEOUT * 1000L, client.writeTimeoutMillis.toLong())
    }

    @Test
    fun `SecurityConfig should register a security header interceptor`() {
        val client = SecurityConfig.getSecureOkHttpClient()

        assertTrue(
            "Expected at least one application interceptor adding security headers",
            client.interceptors.isNotEmpty()
        )
    }
}