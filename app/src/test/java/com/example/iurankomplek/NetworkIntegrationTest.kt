package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
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
 * Integration tests for the communication and financial endpoints of [ApiService].
 * Uses a real HTTP loopback through MockWebServer so Retrofit/Gson parsing is exercised.
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

    private fun enqueueJson(code: Int = 200, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    @Test
    fun `getMessages should parse response and forward userId query`() = runTest {
        val messageJson = """
            [{
              "id": "m1",
              "senderId": "user1",
              "receiverId": "user2",
              "content": "Hello, how are you?",
              "timestamp": "2023-01-01T10:00:00Z",
              "readStatus": true,
              "attachments": []
            }]
        """.trimIndent()
        enqueueJson(body = messageJson)

        val response = apiService.getMessages("user1")

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals(1, body.size)
        assertEquals("Hello, how are you?", body.first().content)
        assertTrue(body.first().readStatus)
        assertEquals("/messages?userId=user1", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getMessagesWithUser should use receiverId path segment`() = runTest {
        enqueueJson(body = "[]")

        val response = apiService.getMessagesWithUser("user2", "user1")

        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        val path = mockWebServer.takeRequest().path!!
        assertTrue(path.startsWith("/messages/user2"))
        assertTrue(path.contains("senderId=user1"))
    }

    @Test
    fun `getAnnouncements should tolerate missing optional fields`() = runTest {
        enqueueJson(body = """[{"id":"1","title":"Water off","content":"Monday","category":"maintenance"}]""")

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val announcement = response.body()!!.single()
        assertEquals("Water off", announcement.title)
        assertEquals("", announcement.priority)
        assertEquals("", announcement.createdAt)
        assertTrue(announcement.readBy.isEmpty())
    }

    @Test
    fun `getAnnouncements should return the raw list envelope`() = runTest {
        val announcements = listOf(
            Announcement(
                id = "1",
                title = "Community Meeting",
                content = "Meeting at 7 PM",
                category = "general",
                priority = "high",
                createdAt = "2023-01-01T00:00:00Z",
                readBy = listOf("user1")
            )
        )
        enqueueJson(body = Gson().toJson(announcements))

        val response = apiService.getAnnouncements()

        assertEquals(announcements, response.body())
    }

    @Test
    fun `getPemanfaatan should parse financial payload`() = runTest {
        val item = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 500_000,
            total_iuran_rekap = 12_000_000,
            jumlah_iuran_bulanan = 500_000,
            total_iuran_individu = 400_000,
            pengeluaran_iuran_warga = 1_000_000,
            pemanfaatan_iuran = "Maintenance Fund",
            avatar = null
        )
        enqueueJson(body = Gson().toJson(PemanfaatanResponse(data = listOf(item))))

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val parsed = response.body()!!.data.single()
        assertEquals("Maintenance Fund", parsed.pemanfaatan_iuran)
        assertEquals(1_000_000, parsed.pengeluaran_iuran_warga)
        assertEquals(null, parsed.avatar)
    }

    @Test
    fun `malformed json should surface a parse failure`() = runTest {
        enqueueJson(body = "{not json")

        val thrown = runCatching { apiService.getPemanfaatan() }.exceptionOrNull()

        assertNotNull("Malformed JSON must not silently return an empty result", thrown)
    }

    @Test
    fun `getMessages should surface 401 error code`() = runTest {
        enqueueJson(code = 401, body = """{"error":"Unauthorized"}""")

        val response = apiService.getMessages("user1")

        assertTrue(!response.isSuccessful)
        assertEquals(401, response.code())
    }
}