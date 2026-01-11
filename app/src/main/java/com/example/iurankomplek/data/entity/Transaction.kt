package com.example.iurankomplek.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.iurankomplek.data.constraints.TransactionConstraints
import com.example.iurankomplek.data.constraints.UserConstraints
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import java.math.BigDecimal
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["status"]),
        Index(value = ["user_id", "status"]),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"])
    ]
)
data class Transaction(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "amount")
    val amount: Long,

    @ColumnInfo(name = "currency", defaultValue = "'IDR'")
    val currency: String = "IDR",

    @ColumnInfo(name = "status")
    val status: PaymentStatus,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: PaymentMethod,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),

    @ColumnInfo(name = "metadata")
    val metadata: String = ""
) {
    init {
        validate()
    }

    private fun validate() {
        require(id.isNotBlank()) { "Transaction ID cannot be blank" }
        require(userId > 0) { "User ID must be positive" }
        require(amount > 0) { "Amount must be positive (stored as cents, minimum 1 cent)" }
        require(amount <= 99999999999L) { "Amount exceeds max value (stored as cents, max 99999999999 = 999999999.99)" }
        require(currency.isNotBlank()) { "Currency cannot be blank" }
        require(currency.length <= TransactionConstraints.Constraints.MAX_CURRENCY_LENGTH) { "Currency too long" }
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(description.length <= TransactionConstraints.Constraints.MAX_DESCRIPTION_LENGTH) { "Description too long" }
        require(metadata.length <= TransactionConstraints.Constraints.MAX_METADATA_LENGTH) { "Metadata too long" }
    }

    companion object {
        private val BD_HUNDRED = BigDecimal("100")

        fun create(request: com.example.iurankomplek.payment.PaymentRequest): Transaction {
            val now = Date()
            val amountInCents = request.amount.multiply(BD_HUNDRED)
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .toLong()
            return Transaction(
                id = java.util.UUID.randomUUID().toString(),
                userId = request.customerId.toLongOrNull() ?: 0L,
                amount = amountInCents,
                currency = request.currency,
                status = PaymentStatus.PENDING,
                paymentMethod = request.paymentMethod,
                description = request.description,
                createdAt = now,
                updatedAt = now,
                metadata = request.metadata.entries.joinToString(",") { "${it.key}=${it.value}" }
            )
        }
    }
}
