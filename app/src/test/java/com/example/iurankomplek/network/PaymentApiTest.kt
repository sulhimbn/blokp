package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
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

    @Test
    fun `initiatePayment should parse response correctly`() = runBlocking {
        mockWebServer.enqueue(
            json(
                """
                {
                  "transactionId": "TX-1",
                  "status": "PENDING",
                  "paymentMethod": "CREDIT_CARD",
                  "amount": "10000",
                  "currency": "IDR",
                  "transactionTime": 1700000000000,
                  "referenceNumber": "REF-1"
                }
                """.trimIndent()
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
        assertEquals("TX-1", body!!.transactionId)
        assertEquals("PENDING", body.status)
        assertEquals("10000", body.amount)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals(
            "/payments/initiate?amount=10000&description=Test%20payment" +
                "&customerId=test_user&paymentMethod=CREDIT_CARD",
            recorded.path
        )
    }

    @Test
    fun `getPaymentStatus should parse response correctly`() = runBlocking {
        mockWebServer.enqueue(
            json(
                """
                {
                  "transactionId": "TX-1",
                  "status": "COMPLETED",
                  "amount": "10000",
                  "currency": "IDR",
                  "updatedAt": 1700000001000
                }
                """.trimIndent()
            )
        )

        val response = apiService.getPaymentStatus("TX-1")

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals("COMPLETED", body!!.status)
        assertEquals("/payments/TX-1/status", mockWebServer.takeRequest().path)
    }

    @Test
    fun `confirmPayment should parse response correctly`() = runBlocking {
        mockWebServer.enqueue(
            json(
                """
                {
                  "transactionId": "TX-1",
                  "status": "CONFIRMED",
                  "confirmationTime": 1700000002000
                }
                """.trimIndent()
            )
        )

        val response = apiService.confirmPayment("TX-1")

        assertTrue(response.isSuccessful)
        val body = response.body()
        assertNotNull(body)
        assertEquals("CONFIRMED", body!!.status)
        assertEquals("/payments/TX-1/confirm", mockWebServer.takeRequest().path)
    }

    @Test
    fun `payment endpoints should report a declined status without throwing`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(402)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"Payment required"}""")
        )

        val response: retrofit2.Response<PaymentStatusResponse> = apiService.getPaymentStatus("TX-1")

        assertEquals(402, response.code())
        assertTrue(!response.isSuccessful)
    }

    @Test
    fun `ApiConfig should expose the payment endpoints`() {
        val service = ApiConfig.getApiService()

        assertNotNull(service)
        assertEquals("ApiService", service.javaClass.interfaces.single().simpleName)
    }

    private fun json(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}
