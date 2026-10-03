package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.CacheManager
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `getUsers should parse response correctly`() {
        val mockUsers = listOf(TestFixtures.dataItem(first_name = "John", last_name = "Doe"))
        mockWebServer.enqueue(jsonResponse(Gson().toJson(UserResponse(data = mockUsers))))

        val response = runBlocking { apiService.getUsers() }

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertEquals(1, body?.data?.size)
        assertEquals("John", body?.data?.first()?.first_name)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getUsers should handle empty response`() {
        mockWebServer.enqueue(jsonResponse(Gson().toJson(UserResponse(data = emptyList()))))

        val response = runBlocking { apiService.getUsers() }

        assertTrue(response.isSuccessful)
        assertTrue(response.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers should surface server error without throwing`() {
        mockWebServer.enqueue(
            MockResponse().apply {
            setResponseCode(500)
            setHeader("Content-Type", "application/json")
            setBody("{\"error\": \"Internal Server Error\"}")
        }
        )

        val response = runBlocking { apiService.getUsers() }

        assertEquals(500, response.code())
        assertEquals(false, response.isSuccessful)
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() {
        val mockData = listOf(
            TestFixtures.dataItem(
                pemanfaatan_iuran = "Perbaikan jalan komplek",
                pengeluaran_iuran_warga = 50000
            )
        )
        mockWebServer.enqueue(
            jsonResponse(Gson().toJson(PemanfaatanResponse(data = mockData)))
        )

        val response = runBlocking { apiService.getPemanfaatan() }

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertEquals(1, body?.data?.size)
        assertEquals("Perbaikan jalan komplek", body?.data?.first()?.pemanfaatan_iuran)
        assertEquals(50000, body?.data?.first()?.pengeluaran_iuran_warga)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getAnnouncements should parse a bare list response`() {
        val announcements = listOf(
            TestFixtures.announcement(id = "1", title = "Community Meeting", priority = "high")
        )
        mockWebServer.enqueue(jsonResponse(Gson().toJson(announcements)))

        val response = runBlocking { apiService.getAnnouncements() }

        assertTrue(response.isSuccessful)
        assertEquals(1, response.body()?.size)
        assertEquals("Community Meeting", response.body()?.first()?.title)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getAnnouncements should preserve announcement fields`() {
        val announcements = listOf(
            Announcement(
                id = "a1",
                title = "Water shutoff",
                content = "Maintenance 9am-12pm",
                category = "maintenance",
                priority = "urgent",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = listOf("user1")
            )
        )
        mockWebServer.enqueue(jsonResponse(Gson().toJson(announcements)))

        val parsed = runBlocking { apiService.getAnnouncements() }.body()?.single()

        assertEquals("a1", parsed?.id)
        assertEquals("urgent", parsed?.priority)
        assertEquals(listOf("user1"), parsed?.readBy)
    }

    private fun jsonResponse(body: String) = MockResponse().apply {
            setResponseCode(200)
            setHeader("Content-Type", "application/json")
            setBody(body)
        }
}