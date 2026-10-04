package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
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

class NetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

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

    private fun enqueueJson(payload: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(payload))
        )
    }

    @Test
    fun `real ApiConfig service should handle successful responses`() {
        val service = ApiConfig.getApiService()
        assertNotNull("ApiService should not be null", service)
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        enqueueJson(
            mapOf(
                "data" to listOf(
                    mapOf(
                        "first_name" to "Maintenance",
                        "last_name" to "Fund",
                        "email" to "maintenance@example.com",
                        "alamat" to "Monthly maintenance",
                        "iuran_perwarga" to 1000000,
                        "total_iuran_rekap" to 1000000,
                        "jumlah_iuran_bulanan" to 1000000,
                        "total_iuran_individu" to 1000000,
                        "pengeluaran_iuran_warga" to 0,
                        "pemanfaatan_iuran" to "Monthly maintenance",
                        "avatar" to ""
                    )
                )
            )
        )

        val response = apiService.getPemanfaatan()

        assertTrue("Response should be successful", response.isSuccessful)
        val body = response.body()
        assertNotNull("Response body should not be null", body)
        assertEquals("Should have 1 pemanfaatan item in response", 1, body?.data?.size)
        assertEquals("Maintenance Fund", "${body?.data?.first()?.first_name} ${body?.data?.first()?.last_name}")
        assertEquals(1000000, body?.data?.first()?.total_iuran_individu)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        enqueueJson(
            listOf(
                mapOf(
                    "id" to "1",
                    "title" to "Community Meeting",
                    "content" to "Meeting at 7 PM",
                    "category" to "general",
                    "priority" to "high",
                    "createdAt" to "2023-01-01T00:00:00Z",
                    "readBy" to emptyList<String>()
                )
            )
        )

        val response = apiService.getAnnouncements()

        assertTrue("Response should be successful", response.isSuccessful)
        val body = response.body()
        assertNotNull("Response body should not be null", body)
        assertEquals("Should have 1 announcement in response", 1, body?.size)
        assertEquals("Community Meeting", body?.first()?.title)
        assertEquals("high", body?.first()?.priority)
        assertEquals("2023-01-01T00:00:00Z", body?.first()?.createdAt)
    }

    @Test
    fun `getMessages should parse response correctly`() = runTest {
        enqueueJson(
            listOf(
                mapOf(
                    "id" to "1",
                    "senderId" to "user1",
                    "receiverId" to "user2",
                    "content" to "Hello, how are you?",
                    "timestamp" to "2023-01-01T10:00:00Z",
                    "readStatus" to false,
                    "attachments" to emptyList<String>()
                )
            )
        )

        val response = apiService.getMessages("user1")

        assertTrue("Response should be successful", response.isSuccessful)
        val body = response.body()
        assertNotNull("Response body should not be null", body)
        assertEquals("Should have 1 message in response", 1, body?.size)
        assertEquals("Hello, how are you?", body?.first()?.content)
        assertEquals("user2", body?.first()?.receiverId)
    }
}