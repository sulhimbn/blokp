package com.example.iurankomplek.network

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Wire-format checks for the users and pemanfaatan endpoints.
 *
 * The previous version of this class enqueued fixtures into a MockWebServer but
 * issued its calls through ApiConfig.getApiService(), which points at the
 * production host: the fixtures were never served and the test depended on the
 * live API. The service under test is now built against the mock server.
 */
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enqueueJson(code: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private fun sampleUser() = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john.doe@example.com",
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    @Test
    fun `getUsers should parse response correctly`() = runTest {
        enqueueJson(200, gson.toJson(UserResponse(data = listOf(sampleUser()))))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(1, result.body()?.data?.size)
        assertEquals("John", result.body()?.data?.get(0)?.first_name)
        assertEquals("Doe", result.body()?.data?.get(0)?.last_name)
        assertEquals("john.doe@example.com", result.body()?.data?.get(0)?.email)
    }

    @Test
    fun `getUsers should handle server error response`() = runTest {
        enqueueJson(500, """{"error": "Internal server error"}""")

        val result = apiService.getUsers()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
        assertNull(result.body())
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        enqueueJson(200, gson.toJson(UserResponse(data = emptyList())))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertTrue(result.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getPemanfaatan should parse financial response correctly`() = runTest {
        val financialData = listOf(
            sampleUser().copy(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane.smith@example.com",
                alamat = "456 Oak Ave",
                iuran_perwarga = 200,
                total_iuran_rekap = 600,
                jumlah_iuran_bulanan = 300,
                total_iuran_individu = 200,
                pengeluaran_iuran_warga = 75,
                pemanfaatan_iuran = "Repairs",
                avatar = "https://example.com/avatar2.jpg"
            )
        )
        enqueueJson(200, gson.toJson(PemanfaatanResponse(data = financialData)))

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(1, result.body()?.data?.size)
        assertEquals("Jane", result.body()?.data?.get(0)?.first_name)
        assertEquals("Repairs", result.body()?.data?.get(0)?.pemanfaatan_iuran)
    }

    @Test
    fun `getPemanfaatan should handle server error response`() = runTest {
        enqueueJson(404, """{"error": "Not found"}""")

        val result = apiService.getPemanfaatan()

        assertFalse(result.isSuccessful)
        assertEquals(404, result.code())
    }
}