package com.example.iurankomplek.payment

import java.math.BigDecimal

interface PaymentGateway {
    suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse>
    suspend fun refundPayment(transactionId: String): Result<RefundResponse>
    suspend fun getPaymentStatus(transactionId: String): Result<PaymentStatus>
}

enum class PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER,
    E_WALLET,
    VIRTUAL_ACCOUNT
}

data class PaymentRequest(
    val amount: BigDecimal,
    val currency: String = "IDR",
    val description: String,
    val customerId: String,
    val paymentMethod: PaymentMethod,
    val metadata: Map<String, String> = emptyMap()
)

data class PaymentResponse(
    val transactionId: String,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val amount: BigDecimal,
    val currency: String,
    val transactionTime: Long,
    val referenceNumber: String,
    val metadata: Map<String, String> = emptyMap()
)

data class RefundResponse(
    val refundId: String,
    val transactionId: String,
    val amount: BigDecimal,
    val status: RefundStatus,
    val refundTime: Long,
    val reason: String? = null
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}

enum class RefundStatus {
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED
}

// Extension function to convert PaymentResponse to API model
fun PaymentResponse.toApiPaymentResponse(): com.example.iurankomplek.model.PaymentResponse {
    return com.example.iurankomplek.model.PaymentResponse(
        transactionId = this.transactionId,
        status = this.status.name,
        paymentMethod = this.paymentMethod.name,
        amount = this.amount.toString(),
        currency = this.currency,
        transactionTime = this.transactionTime,
        referenceNumber = this.referenceNumber
    )
}