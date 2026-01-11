package com.example.iurankomplek.payment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.iurankomplek.utils.Constants

@Entity(
    tableName = "webhook_events",
    indices = [
        Index(value = ["idempotency_key"], unique = true),
        Index(value = ["status"]),
        Index(value = ["event_type"]),
        Index(value = ["status", "next_retry_at"]),
        Index(value = ["transaction_id"])
    ]
)
data class WebhookEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "payload")
    val payload: String,

    @ColumnInfo(name = "transaction_id")
    val transactionId: String?,

    @ColumnInfo(name = "status")
    val status: WebhookDeliveryStatus,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = 3,

    @ColumnInfo(name = "next_retry_at")
    val nextRetryAt: Long? = null,

    @ColumnInfo(name = "delivered_at")
    val deliveredAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

enum class WebhookDeliveryStatus {
    PENDING,
    PROCESSING,
    DELIVERED,
    FAILED,
    CANCELLED
}
