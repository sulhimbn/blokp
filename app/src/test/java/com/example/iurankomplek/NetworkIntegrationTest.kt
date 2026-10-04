package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * End-to-end HTTP contract checks for the endpoints the app consumes, including
 * the real [ApiConfig] wiring.
 */
class NetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

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

    private fun sampleDataItem() = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john.doe@example.com",
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance Fund",
        avatar = "https://example.com/avatar.jpg"
    )

    @Test
    fun `real ApiConfig service should handle successful responses`() {
        val service = ApiConfig.getApiService()
        assertNotNull("ApiService should not be null", service)
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        enqueueJson(
            200,
            Gson().toJson(PemanfaatanResponse(data = listOf(sampleDataItem())))
        )

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Maintenance Fund", body.data.first().pemanfaatan_iuran)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        val announcements = listOf(
            Announcement(
                id = "a1",
                title = "Community Meeting",
                content = "Meeting at 7 PM",
                category = "general",
                priority = "high",
                createdAt = "2023-01-01T00:00:00Z",
                readBy = listOf("user1")
            )
        )
        enqueueJson(200, Gson().toJson(announcements))

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Community Meeting", body.first().title)
        assertEquals(listOf("user1"), body.first().readBy)
    }

    @Test
    fun `getMessages should parse response correctly`() = runTest {
        val messages = listOf(
            Message(
                id = "m1",
                senderId = "user1",
                receiverId = "user2",
                content = "Hello, how are you?",
                timestamp = "2023-01-01T10:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )
        enqueueJson(200, Gson().toJson(messages))

        val response = apiService.getMessages("user1")

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Hello, how are you?", body.first().content)
        assertEquals("/messages?userId=user1", mockWebServer.takeRequest().path)
    }
}