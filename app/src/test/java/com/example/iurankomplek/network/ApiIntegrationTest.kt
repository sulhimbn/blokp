package com.example.iurankomplek.network

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.CacheManager
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(RobolectricTestRunner::class)
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

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
        CacheManager.getInstance().clearSync()
        mockWebServer.shutdown()
    }

    @Test
    fun `ApiConfig should hand back a cached ApiService instance`() {
        val first = ApiConfig.getApiService()
        val second = ApiConfig.getApiService()

        assertNotNull(first)
        assertTrue(first === second)
    }

    @Test
    fun `getUsers should parse response correctly`() {
        val mockUsers = listOf(TestFixtures.dataItem(first_name = "John", last_name = "Doe"))
        mockWebServer.enqueue(jsonResponse(Gson().toJson(UserResponse(data = mockUsers))))

        val response = runBlocking { apiService.getUsers() }

        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.data?.size)
        assertEquals("John", response.body()?.data?.first()?.first_name)
    }

    @Test
    fun `getMessages should forward the userId query parameter`() {
        val messages = listOf(
            TestFixtures.message(id = "m1", senderId = "vendor1", receiverId = "user1")
        )
        mockWebServer.enqueue(jsonResponse(Gson().toJson(messages)))

        val response = runBlocking { apiService.getMessages("user1") }

        assertTrue(response.isSuccessful)
        assertEquals("m1", response.body()?.single()?.id)
        assertEquals("/messages?userId=user1", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getCommunityPosts should parse a bare list response`() {
        val posts = listOf(
            TestFixtures.communityPost(id = "p1", title = "Lost cat", likes = 3)
        )
        mockWebServer.enqueue(jsonResponse(Gson().toJson(posts)))

        val response = runBlocking { apiService.getCommunityPosts() }

        assertTrue(response.isSuccessful)
        assertEquals("Lost cat", response.body()?.single()?.title)
        assertEquals(3, response.body()?.single()?.likes)
        assertEquals("/community-posts", mockWebServer.takeRequest().path)
    }

    @Test
    fun `malformed JSON should surface as a failure rather than a crash`() {
        mockWebServer.enqueue(
            MockResponse().apply {
            setResponseCode(200)
            setHeader("Content-Type", "application/json")
            setBody("{not json")
        }
        )

        val thrown = runCatching { runBlocking { apiService.getUsers() } }.exceptionOrNull()

        assertNotNull(thrown)
    }

    private fun jsonResponse(body: String) = MockResponse().apply {
            setResponseCode(200)
            setHeader("Content-Type", "application/json")
            setBody(body)
        }
}