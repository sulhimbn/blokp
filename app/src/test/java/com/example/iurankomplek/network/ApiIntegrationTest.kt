package com.example.iurankomplek.network

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    private fun dataItem(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        pemanfaatan: String = "Maintenance"
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = pemanfaatan,
        avatar = "https://example.com/avatar.jpg"
    )

    private fun enqueueJson(code: Int, body: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(body))
        )
    }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/").toString())
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getUsers should parse response correctly`() = runTest {
        enqueueJson(200, UserResponse(listOf(dataItem())))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data[0].first_name)
        assertEquals("Doe", body.data[0].last_name)
        assertEquals("john.doe@example.com", body.data[0].email)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getUsers should handle server error response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error": "Internal server error"}""")
        )

        val result = apiService.getUsers()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        enqueueJson(200, UserResponse(emptyList()))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertTrue(result.body()!!.data.isEmpty())
    }

    @Test
    fun `getPemanfaatan should parse financial response correctly`() = runTest {
        enqueueJson(
            200,
            PemanfaatanResponse(
                listOf(
                    dataItem(
                        firstName = "Jane",
                        lastName = "Smith",
                        email = "jane.smith@example.com",
                        pemanfaatan = "Repairs"
                    )
                )
            )
        )

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Jane", body.data[0].first_name)
        assertEquals("Repairs", body.data[0].pemanfaatan_iuran)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getPemanfaatan should handle server error response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error": "Not found"}""")
        )

        val result = apiService.getPemanfaatan()

        assertFalse(result.isSuccessful)
        assertEquals(404, result.code())
    }
}