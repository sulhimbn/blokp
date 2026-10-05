package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit integration coverage for the endpoints the app consumes directly.
 * Every call goes through a real OkHttp stack against MockWebServer.
 */
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    private fun dataItem() = DataItem(
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
        assertTrue(body != null)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data.first().first_name)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        enqueueJson(200, UserResponse(emptyList()))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertTrue(result.body()!!.data.isEmpty())
    }

    @Test
    fun `getUsers should handle server error`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error": "Internal Server Error"}""")
        )

        val result = apiService.getUsers()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        enqueueJson(200, PemanfaatanResponse(listOf(dataItem())))

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertTrue(body != null)
        assertEquals(1, body!!.data.size)
        assertEquals("Maintenance", body.data.first().pemanfaatan_iuran)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        enqueueJson(
            200,
            listOf(
                Announcement(
                    id = "a1",
                    title = "Community Meeting",
                    content = "Meeting at 7 PM",
                    category = "general",
                    priority = "high",
                    createdAt = "2023-01-01T00:00:00Z",
                    readBy = emptyList()
                )
            )
        )

        val result = apiService.getAnnouncements()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertTrue(body != null)
        assertEquals(1, body!!.size)
        assertEquals("Community Meeting", body.first().title)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
    }
}