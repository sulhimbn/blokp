package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    private fun enqueueJson(body: String, code: Int = 200) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    @Test
    fun `getUsers should parse response correctly`() = runBlocking {
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

        enqueueJson(Gson().toJson(UserResponse(data = mockUsers)))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data.first().first_name)
        assertEquals(150, body.data.first().total_iuran_individu)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getUsers should handle empty response`() = runBlocking {
        enqueueJson(Gson().toJson(UserResponse(data = emptyList())))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        assertTrue(response.body()!!.data.isEmpty())
    }

    @Test
    fun `getUsers should handle server error`() = runBlocking {
        enqueueJson("{\"error\": \"Internal Server Error\"}", code = 500)

        val response = apiService.getUsers()

        assertEquals(500, response.code())
        assertNull(response.body())
        assertTrue(!response.isSuccessful)
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runBlocking {
        val mockPemanfaatan = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
               email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 1000000,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Monthly maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        enqueueJson(Gson().toJson(PemanfaatanResponse(data = mockPemanfaatan)))

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Monthly maintenance", body.data.first().pemanfaatan_iuran)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runBlocking {
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

        enqueueJson(Gson().toJson(mockAnnouncements))

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Community Meeting", body.first().title)
        assertEquals("general", body.first().category)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
    }
}