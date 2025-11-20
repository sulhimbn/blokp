package com.example.iurankomplek.model

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