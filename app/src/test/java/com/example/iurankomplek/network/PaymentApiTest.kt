package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
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

/**
 * Wire-format checks for the three payment endpoints. These are suspend functions
 * returning retrofit2.Response, so each call is awaited and inspected directly.
 */
class PaymentApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

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

    private fun enqueueJson(code: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    @Test
    fun `initiatePayment should parse response correctly`() = runTest {
        enqueueJson(
            200,
            gson.toJson(
                PaymentResponse(
                    transactionId = "txn-1",
                    status = "PENDING",
                    paymentMethod = "CREDIT_CARD",
                    amount = "10000",
                    currency = "IDR",
                    transactionTime = 1700000000000L,
                    referenceNumber = "ref-1"
                )
            )
        )

        val result = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(result.isSuccessful)
        val body: PaymentResponse = result.body()!!
        assertEquals("txn-1", body.transactionId)
        assertEquals("PENDING", body.status)
        assertEquals(
            "/payments/initiate?amount=10000&description=Test%20payment&customerId=test_user&paymentMethod=CREDIT_CARD",
            mockWebServer.takeRequest().path
        )
    }

    @Test
    fun `getPaymentStatus should parse response correctly`() = runTest {
        enqueueJson(
            200,
            gson.toJson(
                PaymentStatusResponse(
                    transactionId = "txn-1",
                    status = "COMPLETED",
                    amount = "10000",
                    currency = "IDR",
                    updatedAt = 1700000001000L
                )
            )
        )

        val result = apiService.getPaymentStatus("txn-1")

        assertTrue(result.isSuccessful)
        val body: PaymentStatusResponse = result.body()!!
        assertEquals("txn-1", body.transactionId)
        assertEquals("COMPLETED", body.status)
        assertEquals("/payments/txn-1/status", mockWebServer.takeRequest().path)
    }

    @Test
    fun `confirmPayment should parse response correctly`() = runTest {
        enqueueJson(
            200,
            gson.toJson(
                PaymentConfirmationResponse(
                    transactionId = "txn-1",
                    status = "CONFIRMED",
                    confirmationTime = 1700000002000L
                )
            )
        )

        val result = apiService.confirmPayment("txn-1")

        assertTrue(result.isSuccessful)
        val body: PaymentConfirmationResponse = result.body()!!
        assertEquals("txn-1", body.transactionId)
        assertEquals("CONFIRMED", body.status)
        assertEquals("/payments/txn-1/confirm", mockWebServer.takeRequest().path)
    }

    @Test
    fun `initiatePayment should surface server errors`() = runTest {
        enqueueJson(402, """{"error": "Payment Required"}""")

        val result = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertEquals(402, result.code())
        assertNotNull(result.errorBody())
    }
}