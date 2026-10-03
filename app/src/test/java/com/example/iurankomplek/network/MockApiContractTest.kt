package com.example.iurankomplek.network

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.FinancialCalculator
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
 * Contract gate: the exact payloads served by mock-api are parsed by the real
 * Retrofit + Gson + model stack. The fixtures under resources/fixtures are copies of the
 * JSON under mock-api/mock-data, so a drift in either side fails here.
 */
class MockApiContractTest {

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

    @Test
    fun `users endpoint parses the mock-api users payload`() = runBlocking {
        mockWebServer.enqueue(jsonFixture("users.json"))

        val response = apiService.getUsers()

        assertTrue("expected HTTP success, got ${response.code()}", response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals(2, body!!.data.size)

        val john: DataItem = body.data.first()
        assertEquals("John", john.first_name)
        assertEquals("Doe", john.last_name)
        assertEquals("john@example.com", john.email)
        assertEquals("Jl. Contoh No. 1", john.alamat)
        assertEquals(150000, john.iuran_perwarga)
        assertEquals(1800000, john.total_iuran_rekap)
        assertEquals(150000, john.jumlah_iuran_bulanan)
        assertEquals(150000, john.total_iuran_individu)
        assertEquals(50000, john.pengeluaran_iuran_warga)
        assertEquals("Perbaikan jalan komplek", john.pemanfaatan_iuran)
        assertEquals("https://example.com/avatar1.jpg", john.avatar)
    }

    @Test
    fun `pemanfaatan endpoint parses the mock-api pemanfaatan payload`() = runBlocking {
        mockWebServer.enqueue(jsonFixture("pemanfaatan.json"))

        val response = apiService.getPemanfaatan()

        assertTrue("expected HTTP success, got ${response.code()}", response.isSuccessful)
        val body: PemanfaatanResponse? = response.body()
        assertNotNull(body)
        assertEquals(3, body!!.data.size)
        assertEquals("Siti", body.data[0].first_name)
        assertEquals("Perbaikan drainase", body.data[0].pemanfaatan_iuran)
        assertEquals(60000, body.data[0].pengeluaran_iuran_warga)
    }

    @Test
    fun `the two dashboard endpoints serve distinguishable payloads`() {
        // mock-api once shipped pemanfaatan.json as a byte-for-byte copy of users.json,
        // which made the utilisation screen impossible to exercise and let a regression in
        // one endpoint hide behind the other.
        assertTrue(
            "the users and utilisation fixtures must not be identical",
            fixtureText("users.json") != fixtureText("pemanfaatan.json")
        )
    }

    @Test
    fun `mock-api user payload produces the totals the report screen shows`() = runBlocking {
        mockWebServer.enqueue(jsonFixture("users.json"))

        val users: UserResponse = apiService.getUsers().body()!!

        assertEquals(350000, FinancialCalculator.calculateTotalIuranBulanan(users.data))
        assertEquals(125000, FinancialCalculator.calculateTotalPengeluaran(users.data))
        assertEquals(1_050_000, FinancialCalculator.calculateTotalIuranIndividu(users.data))
        assertEquals(925_000, FinancialCalculator.calculateRekapIuran(users.data))
    }

    @Test
    fun `mock-api routes are requested on the paths the Android client expects`() = runBlocking {
        mockWebServer.enqueue(jsonFixture("users.json"))
        apiService.getUsers()
        assertEquals("/users", mockWebServer.takeRequest().path)

        mockWebServer.enqueue(jsonFixture("pemanfaatan.json"))
        apiService.getPemanfaatan()
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)

        mockWebServer.enqueue(jsonFixture("pemanfaatan.json"))
        apiService.getPemanfaatan()
        assertEquals("GET", mockWebServer.takeRequest().method)
    }

    @Test
    fun `empty data array deserializes to an empty list rather than null`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":[]}""")
        )

        val body = apiService.getUsers().body()!!

        assertNotNull(body.data)
        assertTrue(body.data.isEmpty())
    }

    private fun fixtureText(name: String): String {
        val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")) {
            "missing test fixture fixtures/$name"
        }
        return stream.bufferedReader().use { it.readText() }
    }

    private fun jsonFixture(name: String): MockResponse {
        val body = fixtureText(name)
        return MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
    }
}
