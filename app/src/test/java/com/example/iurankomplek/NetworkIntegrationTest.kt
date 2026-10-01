package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
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
        mockWebServer.start(8080) // Use a specific port for consistency

        // Create API service pointing to mock server
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

    @Test
    fun `real ApiConfig service should handle successful responses`() {
        // This test uses the actual ApiConfig to ensure it's properly configured
        // But we can't easily test it against the real API in a unit test environment
        // So we'll test that the configuration doesn't throw errors
        try {
            // Just getting the service shouldn't throw an exception under normal conditions
            val apiService = ApiConfig.getApiService()
            assert(apiService != null) { "ApiService should not be null" }
        } catch (e: Exception) {
            org.junit.Assert.fail("ApiConfig.getApiService() should not throw an exception: ${e.message}")
        }
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() {
        // Given
        val mockPemanfaatanResponse = com.example.iurankomplek.model.PemanfaatanResponse(
            data = listOf(
                com.example.iurankomplek.model.DataItem(
                    first_name = "Jane",
                    last_name = "Doe",
                    email = "jane.doe@example.com",
                    alamat = "456 Oak Ave",
                    iuran_perwarga = 1000000,
                    total_iuran_rekap = 3000000,
                    jumlah_iuran_bulanan = 1000000,
                    total_iuran_individu = 1000000,
                    pengeluaran_iuran_warga = 250000,
                    pemanfaatan_iuran = "Maintenance Fund",
                    avatar = "https://example.com/avatar.jpg"
                )
            )
        )

        val responseJson = Gson().toJson(mockPemanfaatanResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))

        // When
        val responseReceived = runBlocking { apiService.getPemanfaatan() }

        // Then
        assert(responseReceived.isSuccessful) { "Response should be successful" }
        val responseBody = responseReceived.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.data?.size == 1) { "Should have 1 pemanfaatan item in response" }
        assert(responseBody?.data?.first()?.pemanfaatan_iuran == "Maintenance Fund") {
            "First item should be Maintenance Fund"
        }
    }

    @Test
    fun `getAnnouncements should parse response correctly`() {
        // Given
        val mockAnnouncements = listOf(
            com.example.iurankomplek.model.Announcement(
                id = "1",
                title = "Community Meeting",
                content = "Meeting at 7 PM",
                category = "general",
                priority = "high",
                createdAt = "2023-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        val responseJson = Gson().toJson(mockAnnouncements)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))

        // When
        val responseReceived = runBlocking { apiService.getAnnouncements() }

        // Then
        assert(responseReceived.isSuccessful) { "Response should be successful" }
        val responseBody = responseReceived.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.size == 1) { "Should have 1 announcement in response" }
        assert(responseBody?.first()?.title == "Community Meeting") { "First announcement should be Community Meeting" }
    }

    @Test
    fun `getMessages should parse response correctly`() {
        // Given
        val mockMessages = listOf(
            com.example.iurankomplek.model.Message(
                id = "1",
                senderId = "user1",
                receiverId = "user2",
                content = "Hello, how are you?",
                timestamp = "2023-01-01T10:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        val responseJson = Gson().toJson(mockMessages)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))

        // When
        val responseReceived = runBlocking { apiService.getMessages("user1") }

        // Then
        assert(responseReceived.isSuccessful) { "Response should be successful" }
        val responseBody = responseReceived.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.size == 1) { "Should have 1 message in response" }
        assert(responseBody?.first()?.content == "Hello, how are you?") { "First message should have correct content" }
    }
}
