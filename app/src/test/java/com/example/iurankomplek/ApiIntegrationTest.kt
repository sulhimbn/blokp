package com.example.iurankomplek

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

class ApiIntegrationTest {

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

    private fun enqueue(body: String, code: Int = 200) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private val usersJson = """
        {"data":[{
          "first_name":"John","last_name":"Doe","email":"john.doe@example.com",
          "alamat":"123 Main St","iuran_perwarga":100,"total_iuran_rekap":500,
          "jumlah_iuran_bulanan":200,"total_iuran_individu":150,
          "pengeluaran_iuran_warga":50,"pemanfaatan_iuran":"Maintenance",
          "avatar":"https://example.com/avatar.jpg"
        }]}
    """.trimIndent()

    @Test
    fun `getUsers should parse response correctly`() = runTest {
        enqueue(usersJson)

        val response = apiService.getUsers()

        assertTrue("Response should be successful", response.isSuccessful)
        val body = response.body()
        assertNotNull("Response body should not be null", body)
        assertEquals("Should have 1 user in response", 1, body?.data?.size)
        assertEquals("First user should be John", "John", body?.data?.first()?.first_name)
        assertEquals("Doe", body?.data?.first()?.last_name)
        assertEquals(150, body?.data?.first()?.total_iuran_individu)
        assertEquals("https://example.com/avatar.jpg", body?.data?.first()?.avatar)
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        enqueue(Gson().toJson(mapOf("data" to emptyList<Any>())))

        val response = apiService.getUsers()

        assertTrue("Response should be successful", response.isSuccessful)
        assertTrue("Response should have empty data list", response.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers should handle server error`() = runTest {
        enqueue("""{"error": "Internal Server Error"}""", code = 500)

        val response = apiService.getUsers()

        assertTrue("Response should not be successful for 500 error", !response.isSuccessful)
        assertEquals("Response code should be 500", 500, response.code())
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        enqueue(
            """{"data":[{
              "first_name":"Maintenance","last_name":"Fund",
              "email":"maintenance@example.com","alamat":"Monthly maintenance",
              "iuran_perwarga":1000000,"total_iuran_rekap":1000000,
              "jumlah_iuran_bulanan":1000000,"total_iuran_individu":1000000,
              "pengeluaran_iuran_warga":0,"pemanfaatan_iuran":"Monthly maintenance",
              "avatar":""
            }]}"""
        )

        val response = apiService.getPemanfaatan()

        assertTrue("Response should be successful", response.isSuccessful)
        val body = response.body()
        assertNotNull("Response body should not be null", body)
        assertEquals("Should have 1 pemanfaatan item in response", 1, body?.data?.size)
        assertEquals("Maintenance Fund", "${body?.data?.first()?.first_name} ${body?.data?.first()?.last_name}")
        assertEquals("Monthly maintenance", body?.data?.first()?.pemanfaatan_iuran)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        enqueue(
            """[{
              "id":"1","title":"Community Meeting","content":"Meeting at 7 PM",
              "category":"general","priority":"high",
              "createdAt":"2023-01-01T00:00:00Z","readBy":[]
            }]"""
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
}