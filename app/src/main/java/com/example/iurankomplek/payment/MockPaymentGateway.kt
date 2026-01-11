package com.example.iurankomplek.payment

import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.delay
import com.example.iurankomplek.utils.OperationResult

class MockPaymentGateway : PaymentGateway {
    override suspend fun processPayment(request: PaymentRequest): OperationResult<PaymentResponse> {
        return try {
            delay(500)

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

            OperationResult.Success(response)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Payment failed")
        }
    }

    override suspend fun refundPayment(transactionId: String): OperationResult<RefundResponse> {
        return try {
            val refundAmount = calculateRefundAmount(transactionId)
            val response = RefundResponse(
                refundId = UUID.randomUUID().toString(),
                transactionId = transactionId,
                amount = refundAmount,
                status = RefundStatus.COMPLETED,
                refundTime = System.currentTimeMillis(),
                reason = "Mock refund"
            )

            OperationResult.Success(response)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Refund failed")
        }
    }

    private fun calculateRefundAmount(transactionId: String): BigDecimal {
        val hash = transactionId.hashCode().toString()
        val amountDigits = hash.takeLast(4)
        val amount = if (amountDigits.toIntOrNull() ?: 0 > 0) amountDigits.toInt() else com.example.iurankomplek.utils.Constants.Payment.DEFAULT_REFUND_AMOUNT_MIN
        return BigDecimal(amount.toString())
    }

    override suspend fun getPaymentStatus(transactionId: String): OperationResult<PaymentStatus> {
        return try {
            OperationResult.Success(PaymentStatus.COMPLETED)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Status check failed")
        }
    }
}
