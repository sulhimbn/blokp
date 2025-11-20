package com.example.iurankomplek.payment

import java.math.BigDecimal
import java.util.Date
import java.util.UUID

class MockPaymentGateway : PaymentGateway {
    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        // Simulate payment processing
        return try {
            // In a real implementation, this would call actual payment gateway APIs
            Thread.sleep(500) // Simulate network delay
            
            val response = PaymentResponse(
                transactionId = UUID.randomUUID().toString(),
                status = PaymentStatus.COMPLETED,
                paymentMethod = request.paymentMethod,
                amount = request.amount,
                currency = request.currency,
                transactionTime = System.currentTimeMillis(),
                referenceNumber = "REF-${System.currentTimeMillis()}",
                metadata = request.metadata
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refundPayment(transactionId: String): Result<RefundResponse> {
        return try {
            val response = RefundResponse(
                refundId = UUID.randomUUID().toString(),
                transactionId = transactionId,
                amount = BigDecimal("0.00"), // Would be actual refund amount in real implementation
                status = RefundStatus.COMPLETED,
                refundTime = System.currentTimeMillis(),
                reason = "Mock refund"
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPaymentStatus(transactionId: String): Result<PaymentStatus> {
        return try {
            // In a real implementation, this would query the payment gateway for status
            Result.success(PaymentStatus.COMPLETED)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}