package com.example.iurankomplek.network

import com.example.iurankomplek.model.PaymentResponse
import com.example.iurankomplek.model.PaymentStatusResponse
import com.example.iurankomplek.model.PaymentConfirmationResponse
import org.junit.Test
import retrofit2.Call
import kotlin.test.assertNotNull

class PaymentApiTest {
    @Test
    fun `api service should have payment endpoints`() {
        val apiService = ApiConfig.getApiService()
        
        // Test that the payment endpoints exist and return the correct types
        val initiatePaymentCall: Call<PaymentResponse> = apiService.initiatePayment(
            amount = "10000",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "CREDIT_CARD"
        )
        assertNotNull(initiatePaymentCall)
        
        val getPaymentStatusCall: Call<PaymentStatusResponse> = apiService.getPaymentStatus("test_id")
        assertNotNull(getPaymentStatusCall)
        
        val confirmPaymentCall: Call<PaymentConfirmationResponse> = apiService.confirmPayment("test_id")
        assertNotNull(confirmPaymentCall)
    }
}