package com.example.iurankomplek.network

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.data.api.models.UserResponse
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)
        apiService = ApiConfig.getApiService()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getUsers should parse response correctly`() {
        // Given
        val mockUsers = listOf(
            DataItem(
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
            success = true,
            message = "Users fetched successfully",
            data = mockUsers
        )
        
        val responseBody = gson.toJson(mockResponse)
        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody)
        
        mockWebServer.enqueue(response)

        // When
        val call = apiService.getUsers()
        val result = call.execute()

        // Then
        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(true, result.body()?.success)
        assertEquals("Users fetched successfully", result.body()?.message)
        assertNotNull(result.body()?.data)
        assertEquals(1, result.body()?.data?.size)
        assertEquals("John", result.body()?.data?.get(0)?.first_name)
        assertEquals("Doe", result.body()?.data?.get(0)?.last_name)
        assertEquals("john.doe@example.com", result.body()?.data?.get(0)?.email)
    }

    @Test
    fun `getUsers should handle server error response`() {
        // Given
        val response = MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"error\": \"Internal server error\"}")
        
        mockWebServer.enqueue(response)

        // When
        val call = apiService.getUsers()
        val result = call.execute()

        // Then
        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
    }

    @Test
    fun `getUsers should handle empty response`() {
        // Given
        val mockResponse = UserResponse(
            success = true,
            message = "No users found",
            data = emptyList()
        )
        
        val responseBody = gson.toJson(mockResponse)
        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody)
        
        mockWebServer.enqueue(response)

        // When
        val call = apiService.getUsers()
        val result = call.execute()

        // Then
        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(true, result.body()?.success)
        assertEquals("No users found", result.body()?.message)
        assertNotNull(result.body()?.data)
        assertTrue(result.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getPemanfaatan should parse financial response correctly`() {
        // Given
        val mockFinancialData = listOf(
            DataItem(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane.smith@example.com",
                alamat = "456 Oak Ave",
                iuran_perwarga = 200,
                total_iuran_rekap = 600,
                jumlah_iuran_bulanan = 300,
                total_iuran_individu = 200,
                pengeluaran_iuran_warga = 75,
                pemanfaatan_iuran = "Repairs",
                avatar = "https://example.com/avatar2.jpg"
            )
        )
        val mockResponse = PemanfaatanResponse(
            success = true,
            message = "Financial data fetched successfully",
            data = mockFinancialData
        )
        
        val responseBody = gson.toJson(mockResponse)
        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody)
        
        mockWebServer.enqueue(response)

        // When
        val call = apiService.getPemanfaatan()
        val result = call.execute()

        // Then
        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(true, result.body()?.success)
        assertEquals("Financial data fetched successfully", result.body()?.message)
        assertNotNull(result.body()?.data)
        assertEquals(1, result.body()?.data?.size)
        assertEquals("Jane", result.body()?.data?.get(0)?.first_name)
        assertEquals("Repairs", result.body()?.data?.get(0)?.pemanfaatan_iuran)
    }

    @Test
    fun `getPemanfaatan should handle server error response`() {
        // Given
        val response = MockResponse()
            .setResponseCode(404)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"error\": \"Not found\"}")
        
        mockWebServer.enqueue(response)

        // When
        val call = apiService.getPemanfaatan()
        val result = call.execute()

        // Then
        assertFalse(result.isSuccessful)
        assertEquals(404, result.code())
    }
}