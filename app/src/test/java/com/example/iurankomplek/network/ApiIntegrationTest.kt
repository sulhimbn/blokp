package com.example.iurankomplek.network

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Serialization gate: every response wrapper the Android client declares is round-tripped
 * through Gson exactly as it would be on the wire, so a model/serializer mismatch fails here.
 */
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

        mockWebServer.enqueue(json(gson.toJson(UserResponse(data = mockUsers))))

        val response = apiService.getUsers()

        assertTrue("expected HTTP success, got ${response.code()}", response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data[0].first_name)
        assertEquals("Doe", body.data[0].last_name)
        assertEquals("john.doe@example.com", body.data[0].email)
        assertEquals(100, body.data[0].iuran_perwarga)
    }

    @Test
    fun `getUsers should handle empty response`() = runBlocking {
        mockWebServer.enqueue(json(gson.toJson(UserResponse(data = emptyList()))))

        val body = apiService.getUsers().body()!!

        assertNotNull(body.data)
        assertTrue(body.data.isEmpty())
    }

    @Test
    fun `getUsers should surface a server error status without throwing`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"Internal server error"}""")
        )

        val response = apiService.getUsers()

        assertEquals(500, response.code())
        assertTrue(!response.isSuccessful)
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runBlocking {
        val mockData = listOf(
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

        mockWebServer.enqueue(json(gson.toJson(PemanfaatanResponse(data = mockData))))

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Repairs", body.data[0].pemanfaatan_iuran)
        assertEquals(75, body.data[0].pengeluaran_iuran_warga)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runBlocking {
        val announcements = listOf(
            Announcement(
                id = "1",
                title = "Maintenance Notice",
                content = "Water will be off on Monday",
                category = "maintenance",
                priority = "normal",
                createdAt = "2023-06-15T10:30:00Z",
                readBy = listOf("USER-001")
            )
        )

        mockWebServer.enqueue(json(gson.toJson(announcements)))

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Maintenance Notice", body[0].title)
        assertEquals("maintenance", body[0].category)
        assertEquals(listOf("USER-001"), body[0].readBy)
    }

    @Test
    fun `getAnnouncements should surface missing fields as null rather than crashing`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""[{"id":"1","author":"Someone","timestamp":123}]""")
        )

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val announcement = response.body()!!.single()
        assertEquals("1", announcement.id)
        assertEquals(null, announcement.title)
        assertEquals(null, announcement.readBy)
    }

    @Test
    fun `getMessages should parse response and send the userId query parameter`() = runBlocking {
        val messages = listOf(
            Message(
                id = "1",
                senderId = "USER-001",
                receiverId = "USER-002",
                content = "Hello",
                timestamp = "2023-06-15T10:30:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        mockWebServer.enqueue(json(gson.toJson(messages)))

        val response = apiService.getMessages("USER-002")

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Hello", body[0].content)
        assertEquals(false, body[0].readStatus)

        val recorded = mockWebServer.takeRequest()
        assertEquals("/messages?userId=USER-002", recorded.path)
    }

    private fun json(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}
