package com.example.iurankomplek.network

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * End-to-end checks of the Retrofit/OkHttp/Gson stack against MockWebServer.
 *
 * The service under test is built here against the mock server's own URL. ApiConfig.getApiService()
 * cannot be retargeted and, for a debug build, resolves to the Docker-internal host
 * `http://api-mock:5000/...`, so reusing it here would have these tests attempt real DNS
 * resolution instead of exercising the local fixture server.
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
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enqueueJson(code: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private fun dataItem(email: String, address: String = "123 Main St") = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = email,
        alamat = address,
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    @Test
    fun `getUsers should parse response correctly`() = runTest {
        val mockUsers = listOf(dataItem("john.doe@example.com"))
        enqueueJson(200, gson.toJson(UserResponse(data = mockUsers)))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("john.doe@example.com", body.data[0].email)
        assertEquals("John", body.data[0].first_name)
        assertEquals(150, body.data[0].total_iuran_individu)
    }

    @Test
    fun `getUsers should parse multiple users`() = runTest {
        val mockUsers = listOf(
            dataItem("john.doe@example.com"),
            dataItem("jane.smith@example.com")
        )
        enqueueJson(200, gson.toJson(UserResponse(data = mockUsers)))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertEquals(2, result.body()?.data?.size)
        assertEquals(
            listOf("john.doe@example.com", "jane.smith@example.com"),
            result.body()?.data?.map { it.email }
        )
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        enqueueJson(200, gson.toJson(UserResponse(data = emptyList())))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertTrue(result.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers should handle server error response`() = runTest {
        enqueueJson(500, "Internal Server Error")

        val result = apiService.getUsers()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
        assertNull(result.body())
    }

    @Test
    fun `getUsers should hit the users endpoint path`() = runTest {
        enqueueJson(200, gson.toJson(UserResponse(data = emptyList())))

        apiService.getUsers()

        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getPemanfaatan should parse financial response correctly`() = runTest {
        val items = listOf(
            dataItem("a@example.com").copy(pemanfaatan_iuran = "Maintenance", pengeluaran_iuran_warga = 25),
            dataItem("b@example.com").copy(pemanfaatan_iuran = "Utilities", pengeluaran_iuran_warga = 75)
        )
        enqueueJson(200, gson.toJson(PemanfaatanResponse(data = items)))

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(2, body!!.data.size)
        assertEquals(listOf("Maintenance", "Utilities"), body.data.map { it.pemanfaatan_iuran })
        assertEquals(100, body.data.sumOf { it.pengeluaran_iuran_warga })
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getPemanfaatan should handle server error response`() = runTest {
        enqueueJson(503, "Service Unavailable")

        val result = apiService.getPemanfaatan()

        assertFalse(result.isSuccessful)
        assertEquals(503, result.code())
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        val announcements = listOf(
            Announcement(
                id = "a1",
                title = "Water shutoff",
                content = "Maintenance window",
                category = "maintenance",
                priority = "high",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = listOf("user1")
            )
        )
        enqueueJson(200, gson.toJson(announcements))

        val result = apiService.getAnnouncements()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Water shutoff", body[0].title)
        assertEquals(listOf("user1"), body[0].readBy)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getMessages should parse response correctly`() = runTest {
        val messages = listOf(
            Message(
                id = "m1",
                senderId = "user1",
                receiverId = "user2",
                content = "Hello",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )
        enqueueJson(200, gson.toJson(messages))

        val result = apiService.getMessages("user2")

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals("Hello", body!![0].content)
        assertFalse(body[0].readStatus)
        assertEquals("/messages?userId=user2", mockWebServer.takeRequest().path)
    }
}