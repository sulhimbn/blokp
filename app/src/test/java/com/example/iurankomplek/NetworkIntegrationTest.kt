package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
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

class NetworkIntegrationTest {

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
    fun `ApiConfig should cache a single api service instance`() {
        val first = ApiConfig.getApiService()
        val second = ApiConfig.getApiService()

        assertNotNull(first)
        assertSame(first, second)
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runBlocking {
        enqueueJson(200, PemanfaatanResponse(data = listOf(sampleUser())))

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.data?.size)
        assertEquals("pemanfaatan", mockWebServer.takeRequest().path!!.substringBefore("?").removePrefix("/"))
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runBlocking {
        enqueueJson(
            200,
            listOf(
                Announcement(
                    id = "a1",
                    title = "Board Meeting",
                    content = "Monthly board meeting",
                    category = "general",
                    priority = "medium",
                    createdAt = "2024-01-01T00:00:00Z",
                    readBy = emptyList()
                )
            )
        )

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
        assertEquals("Board Meeting", response.body()?.first()?.title)
    }

    @Test
    fun `getMessages should parse response correctly`() = runBlocking {
        enqueueJson(
            200,
            listOf(
                Message(
                    id = "m1",
                    senderId = "user1",
                    receiverId = "user2",
                    content = "Hello",
                    timestamp = "2024-01-01T00:00:00Z",
                    readStatus = false,
                    attachments = listOf("https://example.com/photo.jpg")
                )
            )
        )

        val response = apiService.getMessages("user2")

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Hello", body.first().content)
        assertFalse(body.first().readStatus)
        assertTrue(body.first().attachments.isNotEmpty())
        assertTrue(mockWebServer.takeRequest().path!!.contains("userId=user2"))
    }

    @Test
    fun `getUsers should parse response correctly`() = runBlocking {
        enqueueJson(200, UserResponse(data = listOf(sampleUser())))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        assertEquals("John", response.body()?.data?.first()?.first_name)
    }
}