package com.example.iurankomplek.network

import com.example.iurankomplek.model.DataItem
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

    private fun json(code: Int, body: String) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private fun dataItem(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        pemanfaatan: String = "Maintenance"
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = pemanfaatan,
        avatar = "https://example.com/avatar.jpg"
    )

    private fun usersBody(vararg items: DataItem): String {
        val data = items.joinToString(",") {
            """{"first_name":"${it.first_name}","last_name":"${it.last_name}",
               "email":"${it.email}","alamat":"${it.alamat}",
               "iuran_perwarga":${it.iuran_perwarga},"total_iuran_rekap":${it.total_iuran_rekap},
               "jumlah_iuran_bulanan":${it.jumlah_iuran_bulanan},
               "total_iuran_individu":${it.total_iuran_individu},
               "pengeluaran_iuran_warga":${it.pengeluaran_iuran_warga},
               "pemanfaatan_iuran":"${it.pemanfaatan_iuran}","avatar":"${it.avatar}"}"""
        }
        return """{"data":[$data]}"""
    }

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
    fun `getUsers parses the response and hits the users path`() = runTest {
        mockWebServer.enqueue(json(200, usersBody(dataItem())))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(1, result.body()?.data?.size)
        assertEquals("John", result.body()?.data?.get(0)?.first_name)
        assertEquals("Doe", result.body()?.data?.get(0)?.last_name)
        assertEquals("john.doe@example.com", result.body()?.data?.get(0)?.email)
        assertEquals(100, result.body()?.data?.get(0)?.iuran_perwarga)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/users", request.path)
    }

    @Test
    fun `getUsers surfaces a server error`() = runTest {
        mockWebServer.enqueue(json(500, """{"error":"Internal server error"}"""))

        val result = apiService.getUsers()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
    }

    @Test
    fun `getUsers parses an empty list`() = runTest {
        mockWebServer.enqueue(json(200, """{"data":[]}"""))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertTrue(result.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getPemanfaatan parses the financial response and hits the pemanfaatan path`() = runTest {
        mockWebServer.enqueue(
            json(200, usersBody(dataItem(firstName = "Jane", lastName = "Smith", email = "jane.smith@example.com", pemanfaatan = "Repairs")))
        )

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.data?.size)
        assertEquals("Jane", result.body()?.data?.get(0)?.first_name)
        assertEquals("Repairs", result.body()?.data?.get(0)?.pemanfaatan_iuran)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/pemanfaatan", request.path)
    }

    @Test
    fun `getPemanfaatan surfaces a not found error`() = runTest {
        mockWebServer.enqueue(json(404, """{"error":"Not found"}"""))

        val result = apiService.getPemanfaatan()

        assertFalse(result.isSuccessful)
        assertEquals(404, result.code())
    }

    @Test
    fun `users and pemanfaatan use distinct paths`() = runTest {
        mockWebServer.enqueue(json(200, usersBody(dataItem())))
        mockWebServer.enqueue(json(200, usersBody(dataItem())))

        apiService.getUsers()
        apiService.getPemanfaatan()

        assertEquals("/users", mockWebServer.takeRequest().path)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }
}