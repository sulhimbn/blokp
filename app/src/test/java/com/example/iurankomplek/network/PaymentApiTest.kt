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

class PaymentApiTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        apiService = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `initiatePayment posts to payments initiate with query parameters`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"transactionId":"tx-1","status":"PENDING","paymentMethod":"CREDIT_CARD",
                   "amount":"10000","currency":"IDR","transactionTime":1700000000000,
                   "referenceNumber":"REF-1"}"""
            )
        )

        val response = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(response.isSuccessful)
        assertEquals("tx-1", response.body()?.transactionId)
        assertEquals("REF-1", response.body()?.referenceNumber)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/payments/initiate", request.path?.substringBefore("?"))
        val query = request.path!!.substringAfter("?")
        assertTrue(query.contains("amount=10000"))
        assertTrue(query.contains("customerId=test_user"))
    }

    @Test
    fun `getPaymentStatus requests the status path`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"transactionId":"tx-1","status":"COMPLETED","amount":"10000",
                   "currency":"IDR","updatedAt":1700000000000}"""
            )
        )

        val response = apiService.getPaymentStatus("tx-1")

        assertTrue(response.isSuccessful)
        assertEquals("COMPLETED", response.body()?.status)
        assertEquals("/payments/tx-1/status", server.takeRequest().path)
    }

    @Test
    fun `confirmPayment posts to the confirm path`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"transactionId":"tx-1","status":"CONFIRMED","confirmationTime":1700000000000}"""
            )
        )

        val response = apiService.confirmPayment("tx-1")

        assertTrue(response.isSuccessful)
        assertEquals("CONFIRMED", response.body()?.status)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/payments/tx-1/confirm", request.path)
    }

    @Test
    fun `failed payment initiation surfaces the HTTP error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(402).setBody("""{"error":"declined"}"""))

        val response = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertEquals(402, response.code())
        assertEquals(false, response.isSuccessful)
    }
}