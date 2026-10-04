package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
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

/**
 * Integration tests for [ApiService] against a real HTTP server.
 *
 * The previous version of this file started a MockWebServer and then built the
 * service from [ApiConfig.getApiService], which points at the spreadsheet host,
 * so the queued responses were never consumed and the assertions proved nothing.
 * The service is now built directly against the MockWebServer URL, so these tests
 * exercise Retrofit, Gson and OkHttp end to end.
 */
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    private fun dataItem(
        email: String = "john.doe@example.com",
        pemanfaatan: String = "Maintenance"
    ) = DataItem(
        first_name = "John",
        last_name = "Doe",
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

    private fun enqueueJson(code: Int = 200, body: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(body))
        )
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
    fun `getUsers parses the response body and hits the users path`() = runTest {
        enqueueJson(body = UserResponse(data = listOf(dataItem())))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        val users = requireNotNull(response.body()).data
        assertEquals(1, users.size)
        assertEquals("john.doe@example.com", users.first().email)
        assertEquals(100, users.first().iuran_perwarga)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/users", request.path)
    }

    @Test
    fun `getPemanfaatan parses the response body`() = runTest {
        enqueueJson(body = PemanfaatanResponse(data = listOf(dataItem(pemanfaatan = "Perbaikan jalan"))))

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val items = requireNotNull(response.body()).data
        assertEquals("Perbaikan jalan", items.first().pemanfaatan_iuran)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getUsers surfaces a server error without throwing`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"boom"}""")
        )

        val response = apiService.getUsers()

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
        assertNotNull(response.errorBody())
    }

    @Test
    fun `getUsers does not parse a malformed payload silently`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":"not-a-list"}""")
        )

        val failure = runCatching { apiService.getUsers() }.exceptionOrNull()

        assertNotNull("malformed payload must raise instead of returning empty data", failure)
    }

    @Test
    fun `getUsers handles an empty list`() = runTest {
        enqueueJson(body = UserResponse(data = emptyList()))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        assertTrue(requireNotNull(response.body()).data.isEmpty())
    }

    @Test
    fun `getAnnouncements parses a bare JSON array`() = runTest {
        val announcements = listOf(
            mapOf(
                "id" to "a1",
                "title" to "Jadwal lift",
                "content" to "Lift maintenance",
                "category" to "maintenance",
                "priority" to "high",
                "createdAt" to "2024-01-01T00:00:00Z",
                "readBy" to listOf("user1")
            )
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(announcements))
        )

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val parsed = requireNotNull(response.body())
        assertEquals(1, parsed.size)
        assertEquals("a1", parsed.first().id)
        assertEquals(listOf("user1"), parsed.first().readBy)
    }

    @Test
    fun `initiatePayment sends the amount as a query parameter`() = runTest {
        enqueueJson(
            body = PaymentResponse(
                transactionId = "txn-1",
                status = "COMPLETED",
                paymentMethod = "BANK_TRANSFER",
                amount = "150000",
                currency = "IDR",
                transactionTime = 1700000000L,
                referenceNumber = "REF-1"
            )
        )

        val response = apiService.initiatePayment(
            amount = "150000",
            description = "Iuran Januari",
            customerId = "user1",
            paymentMethod = "BANK_TRANSFER"
        )

        assertTrue(response.isSuccessful)
        assertEquals("txn-1", requireNotNull(response.body()).transactionId)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/payments/initiate", request.requestUrl?.encodedPath)
        assertEquals("150000", request.requestUrl?.queryParameter("amount"))
    }

    @Test
    fun `getPaymentStatus maps the status endpoint`() = runTest {
        enqueueJson(
            body = PaymentStatusResponse(
                transactionId = "txn-1",
                status = "PENDING",
                amount = "150000",
                currency = "IDR",
                updatedAt = 1700000001L
            )
        )

        val response = apiService.getPaymentStatus("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("PENDING", requireNotNull(response.body()).status)
        assertEquals("/payments/txn-1/status", mockWebServer.takeRequest().path)
    }

    @Test
    fun `confirmPayment maps the confirmation endpoint`() = runTest {
        enqueueJson(
            body = PaymentConfirmationResponse(
                transactionId = "txn-1",
                status = "CONFIRMED",
                confirmationTime = 1700000002L
            )
        )

        val response = apiService.confirmPayment("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("CONFIRMED", requireNotNull(response.body()).status)
        assertEquals("/payments/txn-1/confirm", mockWebServer.takeRequest().path)
    }
}