package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PaymentApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start(0)

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

    private fun enqueueJson(code: Int, body: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(body))
        )
    }

    @Test
    fun `initiatePayment should post amount and return payment response`() = runBlocking {
        enqueueJson(
            200,
            PaymentResponse(
                transactionId = "txn-1",
                status = "PENDING",
                paymentMethod = "QRIS",
                amount = "150000",
                currency = "IDR",
                transactionTime = 1_700_000_000_000L,
                referenceNumber = "REF-1"
            )
        )

        val response = apiService.initiatePayment(
            amount = "150000",
            description = "Iuran Januari",
            customerId = "user1",
            paymentMethod = "QRIS"
        )

        assertTrue(response.isSuccessful)
        assertEquals("txn-1", response.body()?.transactionId)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        val path = requireNotNull(recorded.path)
        assertTrue(path.removePrefix("/").startsWith("payments/initiate"))
        assertTrue(path.contains("amount=150000"))
        assertTrue(path.contains("paymentMethod=QRIS"))
    }

    @Test
    fun `getPaymentStatus should return status for transaction`() = runBlocking {
        enqueueJson(
            200,
            PaymentStatusResponse(
                transactionId = "txn-1",
                status = "SUCCESS",
                amount = "150000",
                currency = "IDR",
                updatedAt = 1_700_000_000_000L
            )
        )

        val response = apiService.getPaymentStatus("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("SUCCESS", response.body()?.status)
        assertEquals("payments/txn-1/status", mockWebServer.takeRequest().path!!.removePrefix("/"))
    }

    @Test
    fun `confirmPayment should confirm transaction`() = runBlocking {
        enqueueJson(
            200,
            PaymentConfirmationResponse(
                transactionId = "txn-1",
                status = "COMPLETED",
                confirmationTime = 1_700_000_000_000L
            )
        )

        val response = apiService.confirmPayment("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("COMPLETED", response.body()?.status)
        assertEquals("payments/txn-1/confirm", mockWebServer.takeRequest().path!!.removePrefix("/"))
    }

    @Test
    fun `initiatePayment should surface server errors`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(402)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"payment required"}""")
        )

        val response = apiService.initiatePayment("150000", "Iuran", "user1", "QRIS")

        assertFalse(response.isSuccessful)
        assertEquals(402, response.code())
    }
}