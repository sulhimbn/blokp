package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import kotlinx.coroutines.runBlocking
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
 * Drives the real Retrofit service against a local HTTP server so the declared
 * paths, query names and JSON contract of the payment endpoints are proven to
 * match the mock API rather than merely compiling.
 */
class PaymentApiTest {

    private lateinit var server: MockWebServer
    private lateinit var service: ApiService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        service = Retrofit.Builder()
            .baseUrl(server.url("/data/QjX6hB1ST2IDKaxB/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueueJson(body: String) {
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))
    }

    @Test
    fun `initiatePayment posts to payments initiate with the documented query names`() = runBlocking {
        enqueueJson(
            """
            {"transactionId":"txn-1","status":"PENDING","paymentMethod":"CREDIT_CARD",
             "amount":"10000","currency":"IDR","transactionTime":1700000000000,
             "referenceNumber":"REF-1"}
            """.trimIndent()
        )

        val response = service.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(response.isSuccessful)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/initiate", request.requestUrl!!.encodedPath)
        val query = request.requestUrl!!.query ?: ""
        assertTrue("amount missing from $query", query.contains("amount=10000"))
        assertTrue("description missing from $query", query.contains("description=Test payment"))
        assertTrue("customerId missing from $query", query.contains("customerId=test_user"))
        assertTrue("paymentMethod missing from $query", query.contains("paymentMethod=CREDIT_CARD"))
    }

    @Test
    fun `initiatePayment deserializes the payment response`() = runBlocking {
        enqueueJson(
            """
            {"transactionId":"txn-9","status":"PENDING","paymentMethod":"E_WALLET",
             "amount":"25000","currency":"IDR","transactionTime":1700000000000,
             "referenceNumber":"REF-9"}
            """.trimIndent()
        )

        val body = service.initiatePayment("25000", "d", "u", "E_WALLET").body()

        assertEquals("txn-9", body?.transactionId)
        assertEquals("PENDING", body?.status)
        assertEquals("25000", body?.amount)
        assertEquals("IDR", body?.currency)
        assertEquals(1700000000000L, body?.transactionTime)
        assertEquals("REF-9", body?.referenceNumber)
    }

    @Test
    fun `getPaymentStatus reads the transaction id from the path`() = runBlocking {
        enqueueJson(
            """
            {"transactionId":"txn-5","status":"COMPLETED","amount":"75000",
             "currency":"IDR","updatedAt":1700000005000}
            """.trimIndent()
        )

        val body = service.getPaymentStatus("txn-5").body()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/txn-5/status", request.requestUrl!!.encodedPath)
        assertEquals("txn-5", body?.transactionId)
        assertEquals("COMPLETED", body?.status)
        assertEquals(1700000005000L, body?.updatedAt)
    }

    @Test
    fun `confirmPayment posts to the confirm path`() = runBlocking {
        enqueueJson(
            """
            {"transactionId":"txn-7","status":"COMPLETED","confirmationTime":1700000009000}
            """.trimIndent()
        )

        val body: PaymentConfirmationResponse? =
            service.confirmPayment("txn-7").body()

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/txn-7/confirm", request.requestUrl!!.encodedPath)
        assertEquals("txn-7", body?.transactionId)
        assertEquals("COMPLETED", body?.status)
        assertEquals(1700000009000L, body?.confirmationTime)
    }

    @Test
    fun `a rejected payment surfaces a non successful response`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"error":"already confirmed"}"""))

        val response = service.confirmPayment("txn-dup")

        assertEquals(409, response.code())
    }
}
