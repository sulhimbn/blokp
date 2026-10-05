package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
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

class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
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

    private fun enqueueJson(code: Int, payload: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(payload))
        )
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
                    readBy = listOf("user1")
                )
            )
        )

        val result = apiService.getAnnouncements()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertEquals(1, body?.size)
        assertEquals("Community Meeting", body?.first()?.title)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getMessages should send userId query parameter`() = runTest {
        enqueueJson(
            200,
            listOf(
                Message(
                    id = "m1",
                    senderId = "user1",
                    receiverId = "vendor1",
                    content = "Hello",
                    timestamp = "2023-01-01T00:00:00Z",
                    readStatus = false,
                    attachments = emptyList()
                )
            )
        )

        val result = apiService.getMessages("user1")

        assertTrue(result.isSuccessful)
        assertEquals("Hello", result.body()?.first()?.content)
        val request = mockWebServer.takeRequest()
        assertEquals("/messages", request.requestUrl?.encodedPath)
        assertEquals("user1", request.requestUrl?.queryParameter("userId"))
    }

    @Test
    fun `sendMessage should post content as query parameters`() = runTest {
        enqueueJson(
            200,
            Message(
                id = "m2",
                senderId = "user1",
                receiverId = "vendor1",
                content = "On my way",
                timestamp = "2023-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        val result = apiService.sendMessage("user1", "vendor1", "On my way")

        assertTrue(result.isSuccessful)
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("On my way", request.requestUrl?.queryParameter("content"))
    }

    @Test
    fun `getCommunityPosts should parse response correctly`() = runTest {
        enqueueJson(
            200,
            listOf(
                CommunityPost(
                    id = "p1",
                    authorId = "user1",
                    title = "Broken streetlight",
                    content = "The light is out",
                    category = "maintenance",
                    likes = 3,
                    comments = emptyList(),
                    createdAt = "2023-01-01T00:00:00Z"
                )
            )
        )

        val result = apiService.getCommunityPosts()

        assertTrue(result.isSuccessful)
        assertEquals("Broken streetlight", result.body()?.first()?.title)
    }

    @Test
    fun `getVendors should surface server error code`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"unavailable\"}")
        )

        val result = apiService.getVendors()

        assertFalse(result.isSuccessful)
        assertEquals(503, result.code())
    }
}