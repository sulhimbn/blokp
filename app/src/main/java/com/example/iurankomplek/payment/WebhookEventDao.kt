package com.example.iurankomplek.payment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookEventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(webhookEvent: WebhookEvent): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoreConflict(webhookEvent: WebhookEvent): Long?

    @Update
    suspend fun update(webhookEvent: WebhookEvent)

    @Query("SELECT * FROM webhook_events WHERE id = :id")
    suspend fun getEventById(id: Long): WebhookEvent?

    @Query("SELECT * FROM webhook_events WHERE idempotency_key = :idempotencyKey")
    suspend fun getEventByIdempotencyKey(idempotencyKey: String): WebhookEvent?

    @Query("SELECT * FROM webhook_events WHERE status = :status AND (next_retry_at IS NULL OR next_retry_at <= :currentTime) ORDER BY created_at ASC LIMIT :limit")
    suspend fun getPendingEventsByStatus(status: WebhookDeliveryStatus, currentTime: Long, limit: Int = 10): List<WebhookEvent>

    @Query("SELECT * FROM webhook_events WHERE status = 'PENDING' ORDER BY created_at ASC")
    fun getPendingEvents(): Flow<List<WebhookEvent>>

    @Query("SELECT * FROM webhook_events WHERE transaction_id = :transactionId ORDER BY created_at DESC")
    fun getEventsByTransactionId(transactionId: String): Flow<List<WebhookEvent>>

    @Query("SELECT * FROM webhook_events WHERE event_type = :eventType ORDER BY created_at DESC")
    fun getEventsByType(eventType: String): Flow<List<WebhookEvent>>

    @Query("UPDATE webhook_events SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: WebhookDeliveryStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE webhook_events SET retry_count = :retryCount, next_retry_at = :nextRetryAt, last_error = :lastError, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateRetryInfo(
        id: Long,
        retryCount: Int,
        nextRetryAt: Long?,
        lastError: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE webhook_events SET status = 'DELIVERED', delivered_at = :deliveredAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun markAsDelivered(
        id: Long,
        deliveredAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE webhook_events SET status = 'FAILED', updated_at = :updatedAt WHERE id = :id")
    suspend fun markAsFailed(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM webhook_events WHERE status = 'FAILED' AND created_at < :cutoffTime")
    suspend fun getFailedEventsOlderThan(cutoffTime: Long): List<WebhookEvent>

    @Query("SELECT * FROM webhook_events WHERE status = 'DELIVERED' AND delivered_at < :cutoffTime")
    suspend fun getDeliveredEventsOlderThan(cutoffTime: Long): List<WebhookEvent>

    @Query("DELETE FROM webhook_events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM webhook_events WHERE created_at < :cutoffTime")
    suspend fun deleteEventsOlderThan(cutoffTime: Long): Int

    @Query("SELECT COUNT(*) FROM webhook_events WHERE status = :status")
    suspend fun countByStatus(status: WebhookDeliveryStatus): Int

    @Query("SELECT * FROM webhook_events ORDER BY created_at DESC LIMIT :limit")
    fun getAllEvents(limit: Int = 100): Flow<List<WebhookEvent>>

    @Transaction
    suspend fun insertOrUpdate(webhookEvent: WebhookEvent): Long {
        val existing = getEventByIdempotencyKey(webhookEvent.idempotencyKey)
        return if (existing != null) {
            update(webhookEvent.copy(id = existing.id))
            existing.id
        } else {
            insert(webhookEvent)
        }
    }

    @Transaction
    suspend fun deleteEventsOlderThanAndCount(cutoffTime: Long): Int {
        val count = countByStatus(WebhookDeliveryStatus.DELIVERED) + countByStatus(WebhookDeliveryStatus.FAILED)
        deleteEventsOlderThan(cutoffTime)
        return count
    }
}
