package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
            status = "success",
            data = listOf(
                com.example.iurankomplek.model.PemanfaatanItem(
                    id = 1,
                    name = "Maintenance Fund",
                    amount = 1000000,
                    date = "2023-01-01",
                    description = "Monthly maintenance"
                )
            )
        )
        
        val responseJson = Gson().toJson(mockPemanfaatanResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        // When
        val call = apiService.getPemanfaatan()
        val latch = CountDownLatch(1)
        var responseReceived: retrofit2.Response<com.example.iurankomplek.model.PemanfaatanResponse>? = null
        var errorReceived: Throwable? = null
        
        call.enqueue(object : retrofit2.Callback<com.example.iurankomplek.model.PemanfaatanResponse> {
            override fun onResponse(call: retrofit2.Call<com.example.iurankomplek.model.PemanfaatanResponse>, response: retrofit2.Response<com.example.iurankomplek.model.PemanfaatanResponse>) {
                responseReceived = response
                latch.countDown()
            }
            
            override fun onFailure(call: retrofit2.Call<com.example.iurankomplek.model.PemanfaatanResponse>, t: Throwable) {
                errorReceived = t
                latch.countDown()
            }
        })
        
        // Wait for response
        latch.await(5, TimeUnit.SECONDS)
        
        // Then
        assert(errorReceived == null) { "Request should not fail: ${errorReceived?.message}" }
        assert(responseReceived?.isSuccessful == true) { "Response should be successful" }
        val responseBody = responseReceived?.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.status == "success") { "Status should be success" }
        assert(responseBody?.data?.size == 1) { "Should have 1 pemanfaatan item in response" }
        assert(responseBody?.data?.first()?.name == "Maintenance Fund") { "First item should be Maintenance Fund" }
    }
    
    @Test
    fun `getAnnouncements should parse response correctly`() {
        // Given
        val mockAnnouncements = listOf(
            com.example.iurankomplek.model.Announcement(
                id = 1,
                title = "Community Meeting",
                content = "Meeting at 7 PM",
                author = "Admin",
                timestamp = "2023-01-01T00:00:00Z",
                priority = "high"
            )
        )
        
        val responseJson = Gson().toJson(mockAnnouncements)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        // When
        val call = apiService.getAnnouncements()
        val latch = CountDownLatch(1)
        var responseReceived: retrofit2.Response<List<com.example.iurankomplek.model.Announcement>>? = null
        var errorReceived: Throwable? = null
        
        call.enqueue(object : retrofit2.Callback<List<com.example.iurankomplek.model.Announcement>> {
            override fun onResponse(call: retrofit2.Call<List<com.example.iurankomplek.model.Announcement>>, response: retrofit2.Response<List<com.example.iurankomplek.model.Announcement>>) {
                responseReceived = response
                latch.countDown()
            }
            
            override fun onFailure(call: retrofit2.Call<List<com.example.iurankomplek.model.Announcement>>, t: Throwable) {
                errorReceived = t
                latch.countDown()
            }
        })
        
        // Wait for response
        latch.await(5, TimeUnit.SECONDS)
        
        // Then
        assert(errorReceived == null) { "Request should not fail: ${errorReceived?.message}" }
        assert(responseReceived?.isSuccessful == true) { "Response should be successful" }
        val responseBody = responseReceived?.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.size == 1) { "Should have 1 announcement in response" }
        assert(responseBody?.first()?.title == "Community Meeting") { "First announcement should be Community Meeting" }
    }
    
    @Test
    fun `getMessages should parse response correctly`() {
        // Given
        val mockMessages = listOf(
            com.example.iurankomplek.model.Message(
                id = 1,
                senderId = "user1",
                receiverId = "user2", 
                content = "Hello, how are you?",
                timestamp = "2023-01-01T10:00:00Z",
                status = "sent"
            )
        )
        
        val responseJson = Gson().toJson(mockMessages)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        // When
        val call = apiService.getMessages("user1")
        val latch = CountDownLatch(1)
        var responseReceived: retrofit2.Response<List<com.example.iurankomplek.model.Message>>? = null
        var errorReceived: Throwable? = null
        
        call.enqueue(object : retrofit2.Callback<List<com.example.iurankomplek.model.Message>> {
            override fun onResponse(call: retrofit2.Call<List<com.example.iurankomplek.model.Message>>, response: retrofit2.Response<List<com.example.iurankomplek.model.Message>>) {
                responseReceived = response
                latch.countDown()
            }
            
            override fun onFailure(call: retrofit2.Call<List<com.example.iurankomplek.model.Message>>, t: Throwable) {
                errorReceived = t
                latch.countDown()
            }
        })
        
        // Wait for response
        latch.await(5, TimeUnit.SECONDS)
        
        // Then
        assert(errorReceived == null) { "Request should not fail: ${errorReceived?.message}" }
        assert(responseReceived?.isSuccessful == true) { "Response should be successful" }
        val responseBody = responseReceived?.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.size == 1) { "Should have 1 message in response" }
        assert(responseBody?.first()?.content == "Hello, how are you?") { "First message should have correct content" }
    }
}