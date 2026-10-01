package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PaymentApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)
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

    private fun enqueueSuccess() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{}")
        )
    }

    @Test
    fun `initiatePayment should return a payment response`() {
        enqueueSuccess()

        val response: Response<PaymentResponse> = runBlocking {
            apiService.initiatePayment(
                amount = "10000",
                description = "Test payment",
                customerId = "test_user",
                paymentMethod = "CREDIT_CARD"
            )
        }

        assertNotNull(response)
        assertTrue(response.isSuccessful)
    }

    @Test
    fun `getPaymentStatus should return a status response`() {
        enqueueSuccess()

        val response: Response<PaymentStatusResponse> = runBlocking {
            apiService.getPaymentStatus("test_id")
        }

        assertNotNull(response)
        assertTrue(response.isSuccessful)
    }

    @Test
    fun `confirmPayment should return a confirmation response`() {
        enqueueSuccess()

        val response: Response<PaymentConfirmationResponse> = runBlocking {
            apiService.confirmPayment("test_id")
        }

        assertNotNull(response)
        assertTrue(response.isSuccessful)
    }

    @Test
    fun `initiatePayment should send amount customer and method as query params`() {
        enqueueSuccess()

        runBlocking {
            apiService.initiatePayment(
                amount = "10000",
                description = "Test payment",
                customerId = "test_user",
                paymentMethod = "CREDIT_CARD"
            )
        }

        val path = mockWebServer.takeRequest().path.orEmpty()
        assertTrue(path.startsWith("/payments/initiate?"))
        assertTrue(path.contains("amount=10000"))
        assertTrue(path.contains("customerId=test_user"))
        assertTrue(path.contains("paymentMethod=CREDIT_CARD"))
    }
}
