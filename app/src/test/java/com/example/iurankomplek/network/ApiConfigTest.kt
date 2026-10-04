package com.example.iurankomplek.network

import com.example.iurankomplek.BuildConfig
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Verifies that the production [ApiConfig] wiring yields a usable [ApiService]
 * and that the configured base URL targets the expected host.
 */
@RunWith(RobolectricTestRunner::class)
class ApiConfigTest {

    @Test
    fun `getApiService should return a non-null singleton`() {
        val service = ApiConfig.getApiService()

        assertNotNull(service)
        assertTrue(service === ApiConfig.getApiService())
    }

    @Test
    fun `debug builds target the mock api host`() {
        val expectedPrefix = if (BuildConfig.DEBUG) {
            "http://api-mock:5000/data/"
        } else {
            "https://api.apispreadsheets.com/data/"
        }

        assertTrue(
            "Base URL should start with $expectedPrefix",
            rawBaseUrl().startsWith(expectedPrefix)
        )
    }

    @Test
    fun `spreadsheet id is injected into the base url`() {
        assertTrue(
            "Base URL must carry the configured spreadsheet id",
            rawBaseUrl().contains(BuildConfig.API_SPREADSHEET_ID)
        )
    }

    @Test
    fun `service built with a debug client reaches the network`() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"data":[]}""")
            )

            val service = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

            val response = service.getUsers()

            assertTrue(response.isSuccessful)
            assertTrue(response.body()!!.data.isEmpty())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `error responses keep their status code`() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse()
                    .setResponseCode(500)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"error":"Internal server error"}""")
            )

            val service = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

            val response = service.getUsers()

            assertFalse(response.isSuccessful)
            assertEquals(500, response.code())
        } finally {
            server.shutdown()
        }
    }

    /** Reads the private BASE_URL chosen for this build variant. */
    private fun rawBaseUrl(): String {
        val field = ApiConfig::class.java.getDeclaredField("BASE_URL")
        field.isAccessible = true
        return field.get(null) as String
    }
}