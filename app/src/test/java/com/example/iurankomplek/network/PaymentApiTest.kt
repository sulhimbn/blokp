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
 * Verifies the payment endpoints declared on [ApiService] against a real
 * Retrofit + MockWebServer stack: paths, query parameters and response mapping.
 */
class PaymentApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    private fun enqueue(code: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
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
    fun `initiatePayment posts to payments initiate with every query parameter`() = runTest {
        val payload = PaymentResponse(
            transactionId = "txn-1",
            status = "PENDING",
            paymentMethod = "CREDIT_CARD",
            amount = "10000",
            currency = "IDR",
            transactionTime = 1700000000000L,
            referenceNumber = "ref-1"
        )
        enqueue(200, gson.toJson(payload))

        val response = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(response.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/payments/initiate", recorded.requestUrl!!.encodedPath)
        val url = recorded.requestUrl
        assertNotNull(url)
        assertEquals("10000", url!!.queryParameter("amount"))
        assertEquals("Test payment", url.queryParameter("description"))
        assertEquals("test_user", url.queryParameter("customerId"))
        assertEquals("CREDIT_CARD", url.queryParameter("paymentMethod"))

        val body = response.body()
        assertNotNull(body)
        assertEquals("txn-1", body!!.transactionId)
        assertEquals("PENDING", body.status)
        assertEquals(1700000000000L, body.transactionTime)
    }

    @Test
    fun `getPaymentStatus reads the status from the path segment`() = runTest {
        val payload = PaymentStatusResponse(
            transactionId = "txn-1",
            status = "COMPLETED",
            amount = "10000",
            currency = "IDR",
            updatedAt = 1700000001000L
        )
        enqueue(200, gson.toJson(payload))

        val response = apiService.getPaymentStatus("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("/payments/txn-1/status", mockWebServer.takeRequest().requestUrl!!.encodedPath)
        assertEquals("COMPLETED", response.body()!!.status)
    }

    @Test
    fun `getPaymentStatus surfaces a not found response`() = runTest {
        enqueue(404, """{"error":"unknown transaction"}""")

        val response = apiService.getPaymentStatus("missing")

        assertEquals(404, response.code())
        assertEquals(null, response.body())
    }

    @Test
    fun `confirmPayment posts to the confirm endpoint`() = runTest {
        val payload = PaymentConfirmationResponse(
            transactionId = "txn-1",
            status = "COMPLETED",
            confirmationTime = 1700000002000L
        )
        enqueue(200, gson.toJson(payload))

        val response = apiService.confirmPayment("txn-1")

        assertTrue(response.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/payments/txn-1/confirm", recorded.requestUrl!!.encodedPath)
        assertEquals("COMPLETED", response.body()!!.status)
    }
}