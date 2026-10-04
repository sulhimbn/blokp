package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Integration tests for [ApiService] against a local MockWebServer.
 * Exercises the real Retrofit/Gson parsing pipeline over HTTP.
 */
class ApiIntegrationTest {

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

    private val sampleUser = DataItem(
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
        enqueueJson(body = Gson().toJson(UserResponse(data = listOf(sampleUser))))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals(1, body.data.size)
        assertEquals("John", body.data.first().first_name)
        assertEquals(150, body.data.first().total_iuran_individu)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        enqueueJson(body = Gson().toJson(UserResponse(data = emptyList())))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        assertTrue(response.body()!!.data.isEmpty())
    }

    @Test
    fun `getUsers should surface server error code`() = runTest {
        enqueueJson(code = 500, body = """{"error":"Internal Server Error"}""")

        val response = apiService.getUsers()

        assertTrue(!response.isSuccessful)
        assertEquals(500, response.code())
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        enqueueJson(
            body = Gson().toJson(
                PemanfaatanResponse(
                    data = listOf(
                        sampleUser.copy(
                            pemanfaatan_iuran = "Maintenance Fund",
                            pengeluaran_iuran_warga = 1_000_000
                        )
                    )
                )
            )
        )

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals(1, body.data.size)
        assertEquals("Maintenance Fund", body.data.first().pemanfaatan_iuran)
        assertEquals(1_000_000, body.data.first().pengeluaran_iuran_warga)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        val announcement = Announcement(
            id = "1",
            title = "Community Meeting",
            content = "Meeting at 7 PM",
            category = "general",
            priority = "high",
            createdAt = "2023-01-01T00:00:00Z",
            readBy = emptyList()
        )
        enqueueJson(body = Gson().toJson(listOf(announcement)))

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals(1, body.size)
        assertEquals("Community Meeting", body.first().title)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getCommunityPosts should parse response correctly`() = runTest {
        val postJson = """
            [{
              "id": "p1",
              "authorId": "user1",
              "title": "Water supply maintenance",
              "content": "Water will be off on Monday",
              "category": "maintenance",
              "likes": 3,
              "comments": [],
              "createdAt": "2023-01-01T00:00:00Z"
            }]
        """.trimIndent()
        enqueueJson(body = postJson)

        val response = apiService.getCommunityPosts()

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals(1, body.size)
        assertEquals("Water supply maintenance", body.first().title)
        assertEquals(3, body.first().likes)
    }

    @Test
    fun `sendMessage should post query parameters`() = runTest {
        val messageJson = """
            {
              "id": "m1",
              "senderId": "user1",
              "receiverId": "user2",
              "content": "Halo",
              "timestamp": "2023-01-01T00:00:00Z",
              "readStatus": false,
              "attachments": []
            }
        """.trimIndent()
        enqueueJson(body = messageJson)

        val response = apiService.sendMessage("user1", "user2", "Halo")

        assertTrue(response.isSuccessful)
        assertEquals("Halo", response.body()!!.content)
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path!!.startsWith("/messages?"))
        assertTrue(request.path!!.contains("senderId=user1"))
        assertTrue(request.path!!.contains("receiverId=user2"))
    }
}