package com.example.iurankomplek

import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.data.api.models.ApiResponse
import com.example.iurankomplek.data.api.models.ApiListResponse
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.model.Announcement
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*

class ApiIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiServiceV1
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)
        
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ApiServiceV1::class.java)
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `getUsers should parse response correctly`() = runBlocking {
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
        
        val mockResponse = ApiResponse(
            data = UserResponse(data = mockUsers)
        )
        
        val responseJson = Gson().toJson(mockResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        val response = apiService.getUsers()
        
        assertTrue(response.isSuccessful)
        val responseBody = response.body()
        assertNotNull(responseBody)
        assertNotNull(responseBody?.data)
        assertEquals(1, responseBody?.data?.data?.size)
        assertEquals("John", responseBody?.data?.data?.first()?.first_name)
    }
    
    @Test
    fun `getUsers should handle empty response`() = runBlocking {
        val mockResponse = ApiResponse(
            data = UserResponse(data = emptyList())
        )
        
        val responseJson = Gson().toJson(mockResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        val response = apiService.getUsers()
        
        assertTrue(response.isSuccessful)
        val responseBody = response.body()
        assertNotNull(responseBody)
        assertTrue(responseBody?.data?.data?.isEmpty() == true)
    }
    
    @Test
    fun `getUsers should handle server error`() = runBlocking {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"error\": \"Internal Server Error\"}"))
        
        val response = apiService.getUsers()
        
        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }
    
    @Test
    fun `getPemanfaatan should parse response correctly`() = runBlocking {
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
        
        val mockPemanfaatanResponse = PemanfaatanResponse(data = mockPemanfaatanData)
        
        val mockResponse = ApiResponse(data = mockPemanfaatanResponse)
        
        val responseJson = Gson().toJson(mockResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        val response = apiService.getPemanfaatan()
        
        assertTrue(response.isSuccessful)
        val responseBody = response.body()
        assertNotNull(responseBody)
        assertNotNull(responseBody?.data)
        assertEquals(1, responseBody?.data?.data?.size)
        assertEquals("Maintenance Fund", responseBody?.data?.data?.first()?.pemanfaatan_iuran)
    }
    
    @Test
    fun `getAnnouncements should parse response correctly`() = runBlocking {
        val mockAnnouncements = listOf(
            Announcement(
                id = "1",
                title = "Community Meeting",
                content = "Meeting at 7 PM",
                category = "meeting",
                priority = "high",
                createdAt = "2023-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )
        
        val mockResponse = ApiListResponse(data = mockAnnouncements)
        
        val responseJson = Gson().toJson(mockResponse)
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseJson))
        
        val response = apiService.getAnnouncements()
        
        assertTrue(response.isSuccessful)
        val responseBody = response.body()
        assertNotNull(responseBody)
        assertNotNull(responseBody?.data)
        assertEquals(1, responseBody?.data?.size)
        assertEquals("Community Meeting", responseBody?.data?.first()?.title)
    }
}
