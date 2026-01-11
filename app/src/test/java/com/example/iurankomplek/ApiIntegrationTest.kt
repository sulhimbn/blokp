package com.example.iurankomplek

import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.test.runTest

class ApiIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)
        
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
    fun `getUsers should parse response correctly`() = runTest {
        val mockUsers = listOf(
            LegacyDataItemDto(
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
        
        val response = apiService.getUsers()
        
        assert(response.isSuccessful) { "Response should be successful" }
        val responseBody = response.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.data?.size == 1) { "Should have 1 user in response" }
        assert(responseBody?.data?.first()?.first_name == "John") { "First user should be John" }
    }
    
    @Test
    fun `getUsers should handle empty response`() = runTest {
        val mockResponse = UserResponse(
            data = emptyList()
        )
        
        val responseJson = Gson().toJson(mockResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        val response = apiService.getUsers()
        
        assert(response.isSuccessful) { "Response should be successful" }
        assert(response.body()?.data?.isEmpty() == true) { "Response should have empty data list" }
    }
    
    @Test
    fun `getUsers should handle server error`() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"error\": \"Internal Server Error\"}"))
        
        val response = apiService.getUsers()
        
        assert(!response.isSuccessful) { "Response should not be successful for 500 error" }
        assert(response.code() == 500) { "Response code should be 500" }
    }
    
    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        val mockPemanfaatanData = listOf(
            LegacyDataItemDto(
                first_name = "",
                last_name = "",
                email = "",
                alamat = "",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance Fund",
                avatar = ""
            )
        )
        
        val mockPemanfaatanResponse = PemanfaatanResponse(
            data = mockPemanfaatanData
        )
        
        val responseJson = Gson().toJson(mockPemanfaatanResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        val response = apiService.getPemanfaatan()
        
        assert(response.isSuccessful) { "Response should be successful" }
        val responseBody = response.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.data?.size == 1) { "Should have 1 pemanfaatan item in response" }
        assert(responseBody?.data?.first()?.pemanfaatan_iuran == "Maintenance Fund") { "First item should be Maintenance Fund" }
    }
    
    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        val mockAnnouncements = listOf(
            com.example.iurankomplek.model.Announcement(
                id = "ann_1",
                title = "Community Meeting",
                content = "Meeting at 7 PM",
                category = "General",
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
        
        val response = apiService.getAnnouncements()
        
        assert(response.isSuccessful) { "Response should be successful" }
        val responseBody = response.body()
        assert(responseBody != null) { "Response body should not be null" }
        assert(responseBody?.size == 1) { "Should have 1 announcement in response" }
        assert(responseBody?.first()?.title == "Community Meeting") { "First announcement should be Community Meeting" }
    }
}
