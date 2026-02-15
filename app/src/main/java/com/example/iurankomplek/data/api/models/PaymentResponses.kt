package com.example.iurankomplek.data.api.models

// Response models for payment-related API calls
data class PaymentResponse(
    val transactionId: String,
    val status: String,
    val paymentMethod: String,
    val amount: String,
    val currency: String,
    val transactionTime: Long,
    val referenceNumber: String
)

data class PaymentStatusResponse(
    val transactionId: String,
    val status: String,
    val amount: String,
    val currency: String,
    val updatedAt: Long
)

data class PaymentConfirmationResponse(
    val transactionId: String,
    val status: String,
    val confirmationTime: Long
)
