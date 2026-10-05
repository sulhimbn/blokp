package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
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

    private fun enqueueJson(code: Int, body: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(body))
        )
    }

    @Test
    fun `getUsers should parse response correctly`() = runBlocking {
        enqueueJson(200, UserResponse(data = listOf(sampleUser())))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data.first().first_name)
        assertEquals("users", mockWebServer.takeRequest().path!!.substringBefore("?").removePrefix("/"))
    }

    @Test
    fun `getUsers should handle empty response`() = runBlocking {
        enqueueJson(200, UserResponse(data = emptyList()))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        assertTrue(response.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers should handle server error`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"internal"}""")
        )

        val response = apiService.getUsers()

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runBlocking {
        enqueueJson(200, PemanfaatanResponse(data = listOf(sampleUser())))

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Maintenance", body.data.first().pemanfaatan_iuran)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runBlocking {
        val announcements = listOf(
            Announcement(
                id = "a1",
                title = "Water Shutoff",
                content = "Water will be off tomorrow",
                category = "maintenance",
                priority = "high",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = listOf("user1")
            )
        )
        enqueueJson(200, announcements)

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Water Shutoff", body.first().title)
        assertEquals(listOf("user1"), body.first().readBy)
    }
}