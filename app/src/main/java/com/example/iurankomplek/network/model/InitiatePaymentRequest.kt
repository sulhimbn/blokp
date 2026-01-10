package com.example.iurankomplek.network.model

data class InitiatePaymentRequest(
    val userId: String,
    val amount: Double,
    val paymentMethod: String,
    val description: String? = null
)
