package com.example.iurankomplek.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import java.math.BigDecimal
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val description: String,
    val createdAt: Date,
    val updatedAt: Date,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun create(request: com.example.iurankomplek.payment.PaymentRequest): Transaction {
            val now = Date()
            return Transaction(
                id = java.util.UUID.randomUUID().toString(),
                userId = request.customerId,
                amount = request.amount,
                currency = request.currency,
                status = PaymentStatus.PENDING,
                paymentMethod = request.paymentMethod,
                description = request.description,
                createdAt = now,
                updatedAt = now,
                metadata = request.metadata
            )
        }
    }
}
