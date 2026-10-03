package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun `payment endpoints post and get the expected paths`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(503))
        val initiatePaymentResponse: Response<PaymentResponse> = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )
        assertEquals(503, initiatePaymentResponse.code())
        val initiateRequest = mockWebServer.takeRequest()
        assertEquals("POST", initiateRequest.method)
        assertEquals("/payments/initiate", initiateRequest.requestUrl?.encodedPath)

        mockWebServer.enqueue(MockResponse().setResponseCode(503))
        val statusResponse: Response<PaymentStatusResponse> =
            apiService.getPaymentStatus("test_id")
        assertEquals(503, statusResponse.code())
        assertEquals("/payments/test_id/status", mockWebServer.takeRequest().path)

        mockWebServer.enqueue(MockResponse().setResponseCode(503))
        val confirmResponse: Response<PaymentConfirmationResponse> =
            apiService.confirmPayment("test_id")
        assertEquals(503, confirmResponse.code())
        assertEquals("/payments/test_id/confirm", mockWebServer.takeRequest().path)
    }
}
