package com.example.iurankomplek.network

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

class NetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    private fun json(code: Int, body: String) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

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

    @Test
    fun `getAnnouncements parses the list and hits the announcements path`() = runTest {
        mockWebServer.enqueue(
            json(
                200,
                """[{"id":"a1","title":"Water shutoff","content":"Maintenance on Friday",
                    "category":"utility","priority":"high",
                    "createdAt":"2024-01-01T00:00:00Z","readBy":["u1","u2"]}]"""
            )
        )

        val result = apiService.getAnnouncements()

        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.size)
        assertEquals("Water shutoff", result.body()?.get(0)?.title)
        assertEquals("high", result.body()?.get(0)?.priority)
        assertEquals(2, result.body()?.get(0)?.readBy?.size)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/announcements", request.path)
    }

    @Test
    fun `getAnnouncements surfaces a server error`() = runTest {
        mockWebServer.enqueue(json(500, """{"error":"boom"}"""))

        val result = apiService.getAnnouncements()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
    }

    @Test
    fun `getMessages passes userId as a query parameter`() = runTest {
        mockWebServer.enqueue(
            json(
                200,
                """[{"id":"m1","senderId":"u1","receiverId":"u2","content":"Hello",
                    "timestamp":"2024-01-01T00:00:00Z","readStatus":false,"attachments":[]}]"""
            )
        )

        val result = apiService.getMessages("u1")

        assertTrue(result.isSuccessful)
        assertEquals("Hello", result.body()?.get(0)?.content)
        assertFalse(result.body()?.get(0)?.readStatus ?: true)

        val request = mockWebServer.takeRequest()
        assertEquals("/messages", request.path?.substringBefore("?"))
        assertTrue(request.path!!.contains("userId=u1"))
    }

    @Test
    fun `getMessagesWithUser uses the receiver path and senderId query`() = runTest {
        mockWebServer.enqueue(json(200, "[]"))

        val result = apiService.getMessagesWithUser("u2", "u1")

        assertTrue(result.isSuccessful)
        assertTrue(result.body()?.isEmpty() == true)

        val request = mockWebServer.takeRequest()
        assertEquals("/messages/u2", request.path?.substringBefore("?"))
        assertTrue(request.path!!.contains("senderId=u1"))
    }

    @Test
    fun `sendMessage posts to messages with sender receiver and content`() = runTest {
        mockWebServer.enqueue(
            json(
                200,
                """{"id":"m2","senderId":"u1","receiverId":"u2","content":"Balik",
                    "timestamp":"2024-01-02T00:00:00Z","readStatus":true,"attachments":["a.png"]}"""
            )
        )

        val result = apiService.sendMessage("u1", "u2", "Balik")

        assertTrue(result.isSuccessful)
        assertEquals("m2", result.body()?.id)
        assertEquals(1, result.body()?.attachments?.size)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/messages", request.path?.substringBefore("?"))
        assertTrue(request.path!!.contains("content=Balik"))
    }

    @Test
    fun `getCommunityPosts parses nested comments`() = runTest {
        mockWebServer.enqueue(
            json(
                200,
                """[{"id":"p1","authorId":"u1","title":"Broken lift","content":"Stops at 3F",
                    "category":"maintenance","likes":4,
                    "comments":[{"id":"c1","authorId":"u2","content":"Seen too","timestamp":"2024-01-03T00:00:00Z"}],
                    "createdAt":"2024-01-02T00:00:00Z"}]"""
            )
        )

        val result = apiService.getCommunityPosts()

        assertTrue(result.isSuccessful)
        assertEquals("Broken lift", result.body()?.get(0)?.title)
        assertEquals(4, result.body()?.get(0)?.likes)
        assertEquals("Seen too", result.body()?.get(0)?.comments?.get(0)?.content)

        assertEquals("/community-posts", mockWebServer.takeRequest().path)
    }

    @Test
    fun `createCommunityPost posts the payload as query parameters`() = runTest {
        mockWebServer.enqueue(
            json(
                200,
                """{"id":"p2","authorId":"u1","title":"Gate broken","content":"Latch jammed",
                    "category":"security","likes":0,"comments":[],"createdAt":"2024-01-04T00:00:00Z"}"""
            )
        )

        val result = apiService.createCommunityPost("u1", "Gate broken", "Latch jammed", "security")

        assertTrue(result.isSuccessful)
        assertEquals("p2", result.body()?.id)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/community-posts", request.path?.substringBefore("?"))
        assertTrue(request.path!!.contains("category=security"))
    }

    @Test
    fun `getVendors parses the vendor envelope`() = runTest {
        mockWebServer.enqueue(
            json(
                200,
                """{"data":[{"id":"v1","name":"Acme Plumbing","contactPerson":"John",
                    "phoneNumber":"123","email":"a@b.c","specialty":"plumbing","address":"1 St",
                    "licenseNumber":"L1","insuranceInfo":"INS","certifications":["ISO"],
                    "rating":4.5,"totalReviews":10,"contractStart":"2024-01-01",
                    "contractEnd":"2024-12-31","isActive":true}]}"""
            )
        )

        val result = apiService.getVendors()

        assertTrue(result.isSuccessful)
        assertEquals("Acme Plumbing", result.body()?.data?.get(0)?.name)
        assertEquals(4.5, result.body()?.data?.get(0)?.rating!!, 0.001)
        assertEquals("/vendors", mockWebServer.takeRequest().path)
    }
}