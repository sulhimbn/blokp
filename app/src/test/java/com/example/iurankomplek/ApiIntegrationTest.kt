package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    private fun json(code: Int, body: String) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    @Test
    fun `getUsers should parse response correctly`() = runTest {
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
        mockWebServer.enqueue(json(200, gson.toJson(UserResponse(data = mockUsers))))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data.first().first_name)
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        mockWebServer.enqueue(json(200, gson.toJson(UserResponse(data = emptyList()))))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertTrue(body!!.data.isEmpty())
    }

    @Test
    fun `getUsers should handle server error`() = runTest {
        mockWebServer.enqueue(json(500, """{"error": "Internal Server Error"}"""))

        val result = apiService.getUsers()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        val mockPemanfaatanResponse = PemanfaatanResponse(
            data = listOf(
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
                    pemanfaatan_iuran = "Maintenance Fund",
                    avatar = "https://example.com/avatar2.jpg"
                )
            )
        )
        mockWebServer.enqueue(json(200, gson.toJson(mockPemanfaatanResponse)))

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Maintenance Fund", body.data.first().pemanfaatan_iuran)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        val mockAnnouncements = listOf(
            Announcement(
                id = "1",
                title = "Community Meeting",
                content = "Meeting at 7 PM",
                category = "general",
                priority = "high",
                createdAt = "2023-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )
        mockWebServer.enqueue(json(200, gson.toJson(mockAnnouncements)))

        val result = apiService.getAnnouncements()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Community Meeting", body.first().title)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
    }
}