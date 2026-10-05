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

class PaymentApiTest {

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

    private fun enqueueJson(code: Int, payload: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(payload))
        )
    }

    @Test
    fun `initiatePayment should post amount and method as query parameters`() = runTest {
        enqueueJson(
            200,
            PaymentResponse(
                transactionId = "txn_1",
                status = "PENDING",
                paymentMethod = "CREDIT_CARD",
                amount = "10000",
                currency = "IDR",
                transactionTime = 1700000000000L,
                referenceNumber = "REF-1"
            )
        )

        val response = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(response.isSuccessful)
        assertEquals("txn_1", response.body()?.transactionId)
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/payments/initiate", request.requestUrl?.encodedPath)
        assertEquals("10000", request.requestUrl?.queryParameter("amount"))
        assertEquals("CREDIT_CARD", request.requestUrl?.queryParameter("paymentMethod"))
    }

    @Test
    fun `getPaymentStatus should encode the transaction id in the path`() = runTest {
        enqueueJson(
            200,
            PaymentStatusResponse(
                transactionId = "txn_1",
                status = "COMPLETED",
                amount = "10000",
                currency = "IDR",
                updatedAt = 1700000000000L
            )
        )

        val response = apiService.getPaymentStatus("txn_1")

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals("COMPLETED", body?.status)
        assertEquals("/payments/txn_1/status", mockWebServer.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun `confirmPayment should post to the confirm path`() = runTest {
        enqueueJson(
            200,
            PaymentConfirmationResponse(
                transactionId = "txn_1",
                status = "CONFIRMED",
                confirmationTime = 1700000000000L
            )
        )

        val response = apiService.confirmPayment("txn_1")

        assertTrue(response.isSuccessful)
        assertEquals("CONFIRMED", response.body()?.status)
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/payments/txn_1/confirm", request.requestUrl?.encodedPath)
    }

    @Test
    fun `getPaymentStatus should surface a not found error`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"not found\"}")
        )

        val response = apiService.getPaymentStatus("missing")

        assertTrue(!response.isSuccessful)
        assertEquals(404, response.code())
    }
}