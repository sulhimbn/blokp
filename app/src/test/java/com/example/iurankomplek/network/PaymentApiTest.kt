package com.example.iurankomplek.network

import com.google.gson.Gson
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
    fun `initiatePayment should send query params and parse response`() = runBlocking {
        enqueue(
            Gson().toJson(
                mapOf(
                    "transactionId" to "txn_123",
                    "status" to "PENDING",
                    "paymentMethod" to "CREDIT_CARD",
                    "amount" to "10000",
                    "currency" to "IDR",
                    "transactionTime" to 1700000000000L,
                    "referenceNumber" to "REF-1"
                )
            )
        )

        val response = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals("txn_123", body?.transactionId)
        assertEquals("PENDING", body?.status)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/payments/initiate", recorded.path?.substringBefore("?"))
        assertTrue(recorded.path!!.contains("amount=10000"))
        assertTrue(recorded.path!!.contains("customerId=test_user"))
    }

    @Test
    fun `getPaymentStatus should parse response`() = runBlocking {
        enqueue(
            Gson().toJson(
                mapOf(
                    "transactionId" to "txn_123",
                    "status" to "COMPLETED",
                    "amount" to "10000",
                    "currency" to "IDR",
                    "updatedAt" to 1700000001000L
                )
            )
        )

        val response = apiService.getPaymentStatus("txn_123")

        assertTrue(response.isSuccessful)
        assertEquals("COMPLETED", response.body()?.status)
        assertEquals("/payments/txn_123/status", mockWebServer.takeRequest().path?.substringBefore("?"))
    }

    @Test
    fun `confirmPayment should parse response`() = runBlocking {
        enqueue(
            Gson().toJson(
                mapOf(
                    "transactionId" to "txn_123",
                    "status" to "COMPLETED",
                    "confirmationTime" to 1700000002000L
                )
            )
        )

        val response = apiService.confirmPayment("txn_123")

        assertTrue(response.isSuccessful)
        assertEquals("COMPLETED", response.body()?.status)
        assertEquals(1700000002000L, response.body()?.confirmationTime)
        assertEquals("/payments/txn_123/confirm", mockWebServer.takeRequest().path?.substringBefore("?"))
    }

    @Test
    fun `initiatePayment should surface server error`() = runBlocking {
        enqueue("""{"error":"gateway timeout"}""", code = 504)

        val response = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(!response.isSuccessful)
        assertEquals(504, response.code())
    }
}