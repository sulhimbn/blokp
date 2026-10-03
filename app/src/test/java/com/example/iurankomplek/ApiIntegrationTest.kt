package com.example.iurankomplek

import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ApiIntegrationTest {
    
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
    fun `getUsers should parse response correctly`() {
        // Given
        val mockUsers = listOf(
            com.example.iurankomplek.model.DataItem(
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
        )
        
        val mockResponse = UserResponse(
            data = mockUsers
        )
        
        val responseJson = Gson().toJson(mockResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        // When
        val responseReceived: retrofit2.Response<UserResponse> = runBlocking { apiService.getUsers() }
        
        // Then
        assert(responseReceived?.isSuccessful == true) { "Response should be successful" }
        val responseBody = responseReceived?.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.data?.size == 1) { "Should have 1 user in response" }
        assert(responseBody?.data?.first()?.first_name == "John") { "First user should be John" }
    }
    
    @Test
    fun `getUsers should handle empty response`() {
        // Given
        val mockResponse = UserResponse(
            data = emptyList()
        )
        
        val responseJson = Gson().toJson(mockResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        // When
        val responseReceived: retrofit2.Response<UserResponse> = runBlocking { apiService.getUsers() }
        
        // Then
        assert(responseReceived?.isSuccessful == true) { "Response should be successful" }
        assert(responseReceived?.body()?.data?.isEmpty() == true) { "Response should have empty data list" }
    }
    
    @Test
    fun `getUsers should handle server error`() {
        // Given
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"error\": \"Internal Server Error\"}"))
        
        // When
        val responseReceived: retrofit2.Response<UserResponse> = runBlocking { apiService.getUsers() }
        
        // Then
        assert(responseReceived.isSuccessful == false) { 
            "Response should not be successful for 500 error" 
        }
        assert(responseReceived?.code() == 500) { "Response code should be 500" }
    }
    
    @Test
    fun `getPemanfaatan should parse response correctly`() {
        // Given
        val mockPemanfaatanResponse = com.example.iurankomplek.model.PemanfaatanResponse(
            data = listOf(
                com.example.iurankomplek.model.DataItem(
                    pemanfaatan_iuran = "Maintenance Fund",
                    iuran_perwarga = 1000000,
                    jumlah_iuran_bulanan = 1000000,
                    total_iuran_individu = 1000000
                )
            )
        )
        
        val responseJson = Gson().toJson(mockPemanfaatanResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        // When
        val responseReceived: retrofit2.Response<com.example.iurankomplek.model.PemanfaatanResponse> = runBlocking { apiService.getPemanfaatan() }
        
        // Then
        assert(responseReceived?.isSuccessful == true) { "Response should be successful" }
        val responseBody = responseReceived?.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.data?.size == 1) { "Should have 1 pemanfaatan item in response" }
        assert(responseBody?.data?.first()?.pemanfaatan_iuran == "Maintenance Fund") { "First item should be Maintenance Fund" }
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
        val responseReceived: retrofit2.Response<List<com.example.iurankomplek.model.Announcement>> = runBlocking { apiService.getAnnouncements() }
        
        // Then
        assert(responseReceived?.isSuccessful == true) { "Response should be successful" }
        val responseBody = responseReceived?.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.size == 1) { "Should have 1 announcement in response" }
        assert(responseBody?.first()?.title == "Community Meeting") { "First announcement should be Community Meeting" }
    }
}