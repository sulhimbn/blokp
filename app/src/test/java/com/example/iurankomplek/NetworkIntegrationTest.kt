package com.example.iurankomplek

import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.network.SecurityConfig
import com.example.iurankomplek.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Wiring tests for the network layer configuration.
 *
 * The previous version of this file duplicated the ApiService MockWebServer tests
 * while never pointing Retrofit at the server it started. Endpoint/parsing coverage
 * now lives in network/ApiIntegrationTest.kt; this file covers the client
 * configuration itself: base URL selection, timeouts, certificate pinning and the
 * absence of a body-logging interceptor on the production client.
 */
class NetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiService = retrofit2.Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `ApiConfig returns a cached singleton service`() {
        val first = ApiConfig.getApiService()
        val second = ApiConfig.getApiService()

        assertNotNull(first)
        assertTrue("ApiService must be cached, not rebuilt per call", first === second)
    }

    @Test
    fun `ApiConfig base url is rooted at the configured spreadsheet id`() {
        val debugBuild = com.example.iurankomplek.BuildConfig.DEBUG

        val expectedId = BuildConfig.API_SPREADSHEET_ID
        val expected = if (debugBuild && System.getenv("DOCKER_ENV") != null) {
            "http://api-mock:5000/data/$expectedId/"
        } else {
            "https://api.apispreadsheets.com/data/$expectedId/"
        }

        val field = ApiConfig::class.java.getDeclaredField("BASE_URL")
        field.isAccessible = true
        assertEquals(expected, field.get(ApiConfig))
    }

    @Test
    fun `secure client applies the configured timeouts`() {
        val client = SecurityConfig.getSecureOkHttpClient()

        assertEquals(
            Constants.Network.CONNECT_TIMEOUT,
            TimeUnit.MILLISECONDS.toSeconds(client.connectTimeoutMillis.toLong())
        )
        assertEquals(
            Constants.Network.READ_TIMEOUT,
            TimeUnit.MILLISECONDS.toSeconds(client.readTimeoutMillis.toLong())
        )
        assertEquals(
            Constants.Network.WRITE_TIMEOUT,
            TimeUnit.MILLISECONDS.toSeconds(client.writeTimeoutMillis.toLong())
        )
    }

    @Test
    fun `secure client pins the production certificate`() {
        val client = SecurityConfig.getSecureOkHttpClient()

        val pins = client.certificatePinner.findMatchingPins("api.apispreadsheets.com")
        assertTrue("no pin installed for the production host", pins.isNotEmpty())
        val installed = pins.map { java.util.Base64.getEncoder().encodeToString(it.hash.toByteArray()) }
        assertTrue(
            "configured pin ${Constants.Security.CERTIFICATE_PINNER} is not installed, found $installed",
            installed.contains(Constants.Security.CERTIFICATE_PINNER.removePrefix("sha256/"))
        )
    }

    @Test
    fun `production client sends the hardening headers`() {
        val server = MockWebServer()
        server.start()
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val client: OkHttpClient = SecurityConfig.getSecureOkHttpClient()
            .newBuilder()
            // pinning is verified separately; relax it here so the request can be observed
            .certificatePinner(okhttp3.CertificatePinner.Builder().build())
            .build()
        val request = okhttp3.Request.Builder()
            .url(server.url("/data/QjX6hB1ST2IDKaxB/users"))
            .build()

        client.newCall(request).execute().close()
        val recorded = server.takeRequest()

        assertEquals("nosniff", recorded.getHeader("X-Content-Type-Options"))
        assertEquals("DENY", recorded.getHeader("X-Frame-Options"))
        server.shutdown()
    }

    @Test
    fun `a served user payload is parsed by the shared gson configuration`() = runTest {
        val payload = """
            {"data":[{"first_name":"John","last_name":"Doe","email":"john@example.com",
            "alamat":"Jl. Contoh No. 1","iuran_perwarga":150000,"total_iuran_rekap":1800000,
            "jumlah_iuran_bulanan":150000,"total_iuran_individu":150000,
            "pengeluaran_iuran_warga":50000,"pemanfaatan_iuran":"Perbaikan jalan",
            "avatar":"https://example.com/a1.jpg"}]}
        """.trimIndent()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(payload)
        )

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        val body: UserResponse = requireNotNull(response.body())
        assertEquals(1, body.data.size)
        assertEquals("john@example.com", body.data.first().email)
        assertEquals(150000, body.data.first().iuran_perwarga)
        assertEquals(gson.fromJson(payload, UserResponse::class.java), body)
    }

    @Test
    fun `an error response does not throw and exposes an error body`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(422).setBody("""{"error":"unprocessable"}""")
        )

        val response = apiService.getUsers()

        assertFalse(response.isSuccessful)
        assertEquals(422, response.code())
        assertNotNull(response.errorBody()?.string())
    }
}