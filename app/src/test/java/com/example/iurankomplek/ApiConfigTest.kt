package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.network.SecurityConfig
import com.example.iurankomplek.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ApiConfigTest {

    private val expectedHeaders = mapOf(
        "X-Content-Type-Options" to "nosniff",
        "X-Frame-Options" to "DENY",
        "X-XSS-Protection" to "1; mode=block"
    )

    @Test
    fun `getApiService returns a usable service instance`() {
        assertTrue(ApiConfig.getApiService() is ApiService)
    }

    @Test
    fun `getApiService hands back the same instance on every call`() {
        assertEquals(
            ApiConfig.getApiService().hashCode(),
            ApiConfig.getApiService().hashCode()
        )
        assertTrue(ApiConfig.getApiService() === ApiConfig.getApiService())
    }

    @Test
    fun `the secure client uses the configured timeouts`() {
        val client: OkHttpClient = SecurityConfig.getSecureOkHttpClient()

        assertEquals(Constants.Network.CONNECT_TIMEOUT * 1000, client.connectTimeoutMillis.toLong())
        assertEquals(Constants.Network.READ_TIMEOUT * 1000, client.readTimeoutMillis.toLong())
        assertEquals(Constants.Network.WRITE_TIMEOUT * 1000, client.writeTimeoutMillis.toLong())
    }

    @Test
    fun `the secure client installs application interceptors`() {
        assertTrue(SecurityConfig.getSecureOkHttpClient().interceptors.isNotEmpty())
    }

    @Test
    fun `the security interceptor attaches the declared headers and nothing else`() {
        val request = Request.Builder()
            .url("https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/users")
            .build()

        val recorded = runInterceptors(SecurityConfig.getSecureOkHttpClient(), request)

        assertEquals(expectedHeaders.size, recorded.headers.size)
        for ((name, value) in expectedHeaders) {
            assertEquals("header $name", value, recorded.headers.get(name))
        }
    }

    @Test
    fun `the declared protections cover content sniffing framing and script injection`() {
        val names = expectedHeaders.keys.map { it.uppercase() }

        assertTrue("expected X-Content-Type-Options among $names", names.contains("X-CONTENT-TYPE-OPTIONS"))
        assertTrue("expected X-Frame-Options among $names", names.contains("X-FRAME-OPTIONS"))
        assertTrue("expected X-XSS-Protection among $names", names.contains("X-XSS-PROTECTION"))
    }

    private fun runInterceptors(client: OkHttpClient, request: Request): Request {
        var current = request
        for (interceptor in client.interceptors) {
            if (interceptor is okhttp3.logging.HttpLoggingInterceptor) continue
            current = interceptor.intercept(RecordingChain(current)).request
        }
        return current
    }

    private fun RecordingChain(request: Request) = object : Interceptor.Chain {
        private val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        override fun request(): Request = request
        override fun proceed(request: Request): Response =
            response.newBuilder().request(request).build()
        override fun connection() = null
        override fun call() = throw UnsupportedOperationException("not used")
        override fun connectTimeoutMillis(): Int = 0
        override fun withConnectTimeout(timeout: Int, unit: TimeUnit) = this
        override fun readTimeoutMillis(): Int = 0
        override fun withReadTimeout(timeout: Int, unit: TimeUnit) = this
        override fun writeTimeoutMillis(): Int = 0
        override fun withWriteTimeout(timeout: Int, unit: TimeUnit) = this
    }
}
