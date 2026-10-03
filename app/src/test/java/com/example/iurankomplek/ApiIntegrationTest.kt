package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * End-to-end wiring gate for the two dashboard endpoints, driven through the same Retrofit
 * + Gson stack the app uses at runtime.
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
        assertEquals("john.doe@example.com", body.data[0].email)
    }

    @Test
    fun `getUsers should handle empty response`() = runBlocking {
        mockWebServer.enqueue(json(gson.toJson(UserResponse(data = emptyList()))))

        val body = apiService.getUsers().body()!!

        assertNotNull(body.data)
        assertTrue(body.data.isEmpty())
    }

    @Test
    fun `getUsers should issue a GET to the users path`() = runBlocking {
        mockWebServer.enqueue(json("""{"data":[]}"""))

        apiService.getUsers()

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/users", recorded.path)
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

        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `ApiConfig should build a usable ApiService without throwing`() {
        val service = ApiConfig.getApiService()

        assertNotNull(service)
    }

    @Test
    fun `ApiConfig should return the same cached ApiService instance`() {
        assertSame(ApiConfig.getApiService(), ApiConfig.getApiService())
    }

    private fun json(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}
