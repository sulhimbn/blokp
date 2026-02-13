package com.example.iurankomplek.payment

import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

class MockPaymentGateway : PaymentGateway {
    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        // Simulate payment processing
        return try {
            // In a real implementation, this would call actual payment gateway APIs
            delay(500) // Simulate network delay - non-blocking
            
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
            // In a real implementation, this would get the original transaction amount
            // For mock, we'll generate a refund amount based on the transaction ID
            val refundAmount = calculateRefundAmount(transactionId)
            val response = RefundResponse(
                refundId = UUID.randomUUID().toString(),
                transactionId = transactionId,
                amount = refundAmount,
                status = RefundStatus.COMPLETED,
                refundTime = System.currentTimeMillis(),
                reason = "Mock refund"
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateRefundAmount(transactionId: String): BigDecimal {
        // In a real implementation, this would look up the original transaction
        // For mock, we'll return a value based on the transaction ID
        val hash = transactionId.hashCode().toString()
        val amountDigits = hash.takeLast(4) // Take last 4 digits of the hash
        val amount = if (amountDigits.toIntOrNull() ?: 0 > 0) amountDigits.toInt() else 1000
        return BigDecimal(amount.toString())
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