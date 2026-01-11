package com.example.iurankomplek.network

import com.example.iurankomplek.model.PaymentResponse
import com.example.iurankomplek.model.PaymentStatusResponse
import com.example.iurankomplek.model.PaymentConfirmationResponse
import com.example.iurankomplek.network.model.InitiatePaymentRequest
import org.junit.Test
import retrofit2.Call
import kotlin.test.assertNotNull

class PaymentApiTest {
    @Test
    fun `api service v1 should have payment endpoints`() {
        val apiServiceV1 = ApiConfig.getApiServiceV1()

        // Test that payment endpoints exist and return correct types
        val initiatePaymentCall: Call<com.example.iurankomplek.data.api.models.ApiResponse<PaymentResponse>> = apiServiceV1.initiatePayment(
            InitiatePaymentRequest(
                userId = "test_user",
                amount = 10000.0,
                paymentMethod = "CREDIT_CARD",
                description = "Test payment"
            )
        )
        assertNotNull(initiatePaymentCall)

        val getPaymentStatusCall: Call<com.example.iurankomplek.data.api.models.ApiResponse<PaymentStatusResponse>> = apiServiceV1.getPaymentStatus("test_id")
        assertNotNull(getPaymentStatusCall)

        val confirmPaymentCall: Call<com.example.iurankomplek.data.api.models.ApiResponse<PaymentConfirmationResponse>> = apiServiceV1.confirmPayment("test_id")
        assertNotNull(confirmPaymentCall)
    }
}
}