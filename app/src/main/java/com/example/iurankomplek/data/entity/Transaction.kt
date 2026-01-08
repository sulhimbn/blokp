package com.example.iurankomplek.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.iurankomplek.data.constraints.DatabaseConstraints
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import java.math.BigDecimal
import java.util.Date

@Entity(
    tableName = DatabaseConstraints.Transactions.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [DatabaseConstraints.Users.Columns.ID],
            childColumns = [DatabaseConstraints.Transactions.Columns.USER_ID],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [DatabaseConstraints.Transactions.Columns.USER_ID]),
        Index(value = [DatabaseConstraints.Transactions.Columns.STATUS]),
        Index(value = [DatabaseConstraints.Transactions.Columns.USER_ID, DatabaseConstraints.Transactions.Columns.STATUS]),
        Index(value = [DatabaseConstraints.Transactions.Columns.CREATED_AT]),
        Index(value = [DatabaseConstraints.Transactions.Columns.UPDATED_AT])
    ]
)
data class Transaction(
    @PrimaryKey
    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.ID)
    val id: String,

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.USER_ID)
    val userId: Long,

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.AMOUNT)
    val amount: BigDecimal,

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.CURRENCY, defaultValue = "'IDR'")
    val currency: String = "IDR",

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.STATUS)
    val status: PaymentStatus,

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.PAYMENT_METHOD)
    val paymentMethod: PaymentMethod,

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.DESCRIPTION)
    val description: String,

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.CREATED_AT)
    val createdAt: Date = Date(),

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.UPDATED_AT)
    val updatedAt: Date = Date(),

    @ColumnInfo(name = DatabaseConstraints.Transactions.Columns.METADATA)
    val metadata: String = ""
) {
    init {
        validate()
    }

    private fun validate() {
        require(id.isNotBlank()) { "Transaction ID cannot be blank" }
        require(userId > 0) { "User ID must be positive" }
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        require(amount <= DatabaseConstraints.Transactions.Constraints.MAX_AMOUNT) { "Amount exceeds max value" }
        require(currency.isNotBlank()) { "Currency cannot be blank" }
        require(currency.length <= DatabaseConstraints.Transactions.Constraints.MAX_CURRENCY_LENGTH) { "Currency too long" }
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(description.length <= DatabaseConstraints.Transactions.Constraints.MAX_DESCRIPTION_LENGTH) { "Description too long" }
        require(metadata.length <= DatabaseConstraints.Transactions.Constraints.MAX_METADATA_LENGTH) { "Metadata too long" }
    }

    companion object {
        fun create(request: com.example.iurankomplek.payment.PaymentRequest): Transaction {
            val now = Date()
            return Transaction(
                id = java.util.UUID.randomUUID().toString(),
                userId = request.customerId.toLongOrNull() ?: 0L,
                amount = request.amount,
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
