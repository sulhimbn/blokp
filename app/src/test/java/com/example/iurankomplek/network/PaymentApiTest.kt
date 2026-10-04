package com.example.iurankomplek.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Verifies the payment endpoints of [ApiService]: request shape (method, path,
 * query parameters) and response parsing. Runs against a loopback MockWebServer
 * so no external payment service is contacted.
 */
class PaymentApiTest {

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

    @Test
    fun `initiatePayment should post payment details and parse the response`() = runTest {
        enqueue(
            """
            {
              "transactionId": "txn-1",
              "status": "PENDING",
              "paymentMethod": "CREDIT_CARD",
              "amount": "10000",
              "currency": "IDR",
              "transactionTime": 1700000000000,
              "referenceNumber": "REF-1"
            }
            """.trimIndent()
        )

        val response = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals("txn-1", body.transactionId)
        assertEquals("PENDING", body.status)
        assertEquals("REF-1", body.referenceNumber)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path!!.startsWith("/payments/initiate"))
        assertTrue(request.path!!.contains("amount=10000"))
        assertTrue(request.path!!.contains("customerId=test_user"))
    }

    @Test
    fun `getPaymentStatus should use the id path segment`() = runTest {
        enqueue(
            """
            {
              "transactionId": "txn-1",
              "status": "COMPLETED",
              "amount": "25000",
              "currency": "IDR",
              "updatedAt": 1700000001000
            }
            """.trimIndent()
        )

        val response = apiService.getPaymentStatus("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("COMPLETED", response.body()!!.status)
        assertEquals("/payments/txn-1/status", mockWebServer.takeRequest().path)
    }

    @Test
    fun `confirmPayment should post to the confirm endpoint`() = runTest {
        enqueue(
            """
            {
              "transactionId": "txn-1",
              "status": "CONFIRMED",
              "confirmationTime": 1700000002000
            }
            """.trimIndent()
        )

        val response = apiService.confirmPayment("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("CONFIRMED", response.body()!!.status)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/payments/txn-1/confirm", request.path)
    }

    @Test
    fun `initiatePayment should surface a declined 402 response`() = runTest {
        enqueue("""{"error":"card declined"}""", code = 402)

        val response = apiService.initiatePayment("10000", "Test payment", "test_user", "CREDIT_CARD")

        assertTrue(!response.isSuccessful)
        assertEquals(402, response.code())
    }
}