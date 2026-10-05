package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.network.SecurityConfig
import com.example.iurankomplek.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the Retrofit/OkHttp wiring in [ApiConfig] and [SecurityConfig].
 *
 * The service is created lazily against a base URL that is not reachable from a JVM
 * unit test, so these tests assert configuration properties rather than live traffic.
 */
class NetworkIntegrationTest {

    @Test
    fun `ApiConfig should build a usable ApiService`() {
        val apiService = ApiConfig.getApiService()

        assertNotNull(apiService)
        assertTrue(apiService is ApiService)
    }

    @Test
    fun `ApiConfig should return the same cached instance`() {
        assertTrue(ApiConfig.getApiService() === ApiConfig.getApiService())
    }

    @Test
    fun `secure client should configure the documented timeouts`() {
        val client = SecurityConfig.getSecureOkHttpClient()

        assertEquals(
            Constants.Network.CONNECT_TIMEOUT * 1000L,
            client.connectTimeoutMillis.toLong()
        )
        assertEquals(
            Constants.Network.READ_TIMEOUT * 1000L,
            client.readTimeoutMillis.toLong()
        )
        assertEquals(
            Constants.Network.WRITE_TIMEOUT * 1000L,
            client.writeTimeoutMillis.toLong()
        )
    }

    @Test
    fun `secure client should apply a certificate pinner`() {
        val client = SecurityConfig.getSecureOkHttpClient()

        assertNotNull(client.certificatePinner)
        assertTrue(
            client.certificatePinner.findMatchingPins("api.apispreadsheets.com").isNotEmpty()
        )
    }

    @Test
    fun `security interceptor should add hardening headers`() {
        val client = SecurityConfig.getSecureOkHttpClient()
        val interceptor = client.interceptors.first()

        val request = okhttp3.Request.Builder()
            .url("https://api.apispreadsheets.com/data")
            .build()

        val chain = object : okhttp3.Interceptor.Chain {
            override fun request(): okhttp3.Request = request
            override fun proceed(request: okhttp3.Request): okhttp3.Response {
                recordedHeaders = request.headers
                return okhttp3.Response.Builder()
                    .request(request)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .build()
            }
            override fun connection(): okhttp3.Connection? = null
            override fun call(): okhttp3.Call = throw UnsupportedOperationException()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }

        interceptor.intercept(chain)

        assertEquals("nosniff", recordedHeaders!!["X-Content-Type-Options"])
        assertEquals("DENY", recordedHeaders!!["X-Frame-Options"])
        assertEquals("1; mode=block", recordedHeaders!!["X-XSS-Protection"])
    }

    private var recordedHeaders: okhttp3.Headers? = null
}