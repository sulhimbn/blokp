package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import com.example.iurankomplek.utils.CacheManager
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
        CacheManager.getInstance().clearSync()
        mockWebServer.shutdown()
    }

    @Test
    fun `ApiConfig should expose the payment endpoints`() {
        assertNotNull(ApiConfig.getApiService())
    }

    @Test
    fun `initiatePayment should forward every field as a query parameter`() {
        mockWebServer.enqueue(jsonResponse(gson.toJson(samplePayment())))

        val response = runBlocking {
            apiService.initiatePayment(
                amount = "10000",
                description = "Test payment",
                customerId = "test_user",
                paymentMethod = "CREDIT_CARD"
            )
        }

        assertTrue(response.isSuccessful)
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals(
            "/payments/initiate?amount=10000&description=Test%20payment" +
                "&customerId=test_user&paymentMethod=CREDIT_CARD",
            request.path
        )
    }

    @Test
    fun `initiatePayment should parse the transaction it receives`() {
        mockWebServer.enqueue(jsonResponse(gson.toJson(samplePayment())))

        val parsed = runBlocking {
            apiService.initiatePayment("10000", "Test payment", "test_user", "CREDIT_CARD")
        }.body()

        assertEquals("txn-1", parsed?.transactionId)
        assertEquals("SUCCESS", parsed?.status)
    }

    @Test
    fun `getPaymentStatus should read the status path`() {
        val status = PaymentStatusResponse("txn-1", "SUCCESS", "10000", "IDR", 1700000000L)
        mockWebServer.enqueue(jsonResponse(gson.toJson(status)))

        val response = runBlocking { apiService.getPaymentStatus("txn-1") }

        assertTrue(response.isSuccessful)
        assertEquals("/payments/txn-1/status", mockWebServer.takeRequest().path)
        assertEquals("SUCCESS", response.body()?.status)
    }

    @Test
    fun `confirmPayment should post to the confirmation path`() {
        val confirmation = PaymentConfirmationResponse("txn-1", "CONFIRMED", 1700000000L)
        mockWebServer.enqueue(jsonResponse(gson.toJson(confirmation)))

        val response = runBlocking { apiService.confirmPayment("txn-1") }

        assertTrue(response.isSuccessful)
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/payments/txn-1/confirm", request.path)
        assertEquals("CONFIRMED", response.body()?.status)
    }

    @Test
    fun `initiatePayment should surface a declined gateway response`() {
        mockWebServer.enqueue(MockResponse().apply { setResponseCode(402) })

        val response = runBlocking {
            apiService.initiatePayment("10000", "Test payment", "test_user", "CREDIT_CARD")
        }

        assertEquals(402, response.code())
    }

    private fun samplePayment() = PaymentResponse(
        transactionId = "txn-1",
        status = "SUCCESS",
        paymentMethod = "CREDIT_CARD",
        amount = "10000",
        currency = "IDR",
        transactionTime = 1700000000L,
        referenceNumber = "REF-1"
    )

    private fun jsonResponse(body: String) = MockResponse().apply {
        setResponseCode(200)
        setHeader("Content-Type", "application/json")
        setBody(body)
    }
}