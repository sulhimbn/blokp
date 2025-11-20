package com.example.iurankomplek.receipt

import java.math.BigDecimal
import java.util.Date
import java.util.UUID

data class Receipt(
    val id: String,
    val transactionId: String,
    val userId: String,
    val amount: BigDecimal,
    val description: String,
    val paymentMethod: String,
    val transactionDate: Date,
    val receiptNumber: String,
    val qrCode: String? = null
)