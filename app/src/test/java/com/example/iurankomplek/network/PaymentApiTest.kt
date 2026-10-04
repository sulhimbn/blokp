package com.example.iurankomplek.network

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder

class PaymentApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/data/QjX6hB1ST2IDKaxB/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `initiatePayment posts to the initiate path and parses the flat response`() {
        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/json").setBody(
                """{"transactionId":"pay-1","status":"PENDING","paymentMethod":"BANK_TRANSFER",
                   "amount":"50000","currency":"IDR","transactionTime":1767225600000,
                   "referenceNumber":"REF-0001"}"""
            )
        )

        val response = runBlockingApi {
            api.initiatePayment("50000", "iuran", "user-1", "BANK_TRANSFER")
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(
            "path was ${request.path}",
            request.path!!.startsWith("/data/QjX6hB1ST2IDKaxB/payments/initiate")
        )

        val path = URLDecoder.decode(request.path!!, "UTF-8")
        assertTrue("path was $path", path.contains("amount=50000"))
        assertTrue("path was $path", path.contains("customerId=user-1"))
        assertTrue("path was $path", path.contains("paymentMethod=BANK_TRANSFER"))

        assertTrue(response.isSuccessful)
        val body = requireNotNull(response.body())
        assertEquals("pay-1", body.transactionId)
        assertEquals("PENDING", body.status)
        assertEquals("50000", body.amount)
        assertEquals("IDR", body.currency)
        assertEquals(1767225600000L, body.transactionTime)
        assertEquals("REF-0001", body.referenceNumber)
    }

    @Test
    fun `getPaymentStatus encodes the transaction id in the path`() {
        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/json").setBody(
                """{"transactionId":"pay-1","status":"COMPLETED","amount":"50000",
                   "currency":"IDR","updatedAt":1767225600000}"""
            )
        )

        val response = runBlockingApi { api.getPaymentStatus("pay-1") }

        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/pay-1/status", server.takeRequest().path)
        val body = requireNotNull(response.body())
        assertEquals("COMPLETED", body.status)
        assertEquals(1767225600000L, body.updatedAt)
    }

    @Test
    fun `confirmPayment posts to the confirm path and parses the confirmation`() {
        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/json").setBody(
                """{"transactionId":"pay-1","status":"CONFIRMED","confirmationTime":1767225600000}"""
            )
        )

        val response = runBlockingApi { api.confirmPayment("pay-1") }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/pay-1/confirm", request.path)
        assertEquals("CONFIRMED", requireNotNull(response.body()).status)
    }

    @Test
    fun `a declined payment is reported as an unsuccessful response with no body`() {
        server.enqueue(MockResponse().setResponseCode(402).setBody("{}"))

        val response = runBlockingApi { api.initiatePayment("1", "d", "u", "BANK_TRANSFER") }

        assertEquals(402, response.code())
        assertEquals(null, response.body())
    }

    @Test
    fun `a gateway timeout on status is surfaced verbatim`() {
        server.enqueue(MockResponse().setResponseCode(504))

        val response = runBlockingApi { api.getPaymentStatus("pay-1") }

        assertEquals(504, response.code())
    }

    private fun <T> runBlockingApi(block: suspend () -> T): T = kotlinx.coroutines.runBlocking { block() }
}
