package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 17: Add Composite Indexes for WebhookEventDao Query Optimization
 *
 * Issue Identified:
 * - WebhookEventDao queries use ORDER BY clauses with WHERE filters
 * - Missing composite indexes for efficient query execution
 * - Queries perform inefficient table scans or multiple index lookups
 *
 * Affected Queries:
 * - getEventsByType(): WHERE event_type = :eventType ORDER BY created_at DESC
 *   - Existing: index on event_type
 *   - Missing: ORDER BY created_at DESC optimization
 * - getPendingEvents(): WHERE status = 'PENDING' ORDER BY created_at ASC
 *   - Existing: index on status
 *   - Missing: ORDER BY created_at ASC optimization
 * - getEventsByTransactionId(): WHERE transaction_id = :transactionId ORDER BY created_at DESC
 *   - Existing: index on transaction_id
 *   - Missing: ORDER BY created_at DESC optimization
 *
 * Current Indexes (from WebhookEvent.kt):
 * - idx_webhook_events_idempotency_key (unique) on idempotency_key
 * - idx_webhook_events_status on status
 * - idx_webhook_events_event_type on event_type
 * - idx_webhook_events_status_next_retry_at on (status, next_retry_at)
 * - idx_webhook_events_transaction_id on transaction_id
 *
 * Solution:
 * - Add composite indexes that cover both WHERE filter and ORDER BY clause
 * - Enables index-only scans without additional sorting
 * - Reduces query execution time significantly for sorted results
 *
 * Database Performance Benefits:
 * - Index-only scans: Query satisfied entirely from index
 * - No sorting required: Results already in correct order
 * - Reduced I/O: Fewer pages read from disk
 * - Faster response times: Especially for large webhook event tables
 *
 * Migration Safety:
 * - Non-destructive: Only adds new indexes
 * - Backward compatible: No schema changes to tables
 * - Instant: Index creation doesn't block reads
 */
class Migration17 : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Composite index for getEventsByType()
        // Covers: WHERE event_type = :eventType ORDER BY created_at DESC
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_event_type_created_desc
            ON webhook_events(event_type, created_at DESC)
            """
        )

        // Composite index for getPendingEvents()
        // Covers: WHERE status = 'PENDING' ORDER BY created_at ASC
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_created_asc
            ON webhook_events(status, created_at ASC)
            """
        )

        // Composite index for getEventsByTransactionId()
        // Covers: WHERE transaction_id = :transactionId ORDER BY created_at DESC
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_transaction_created_desc
            ON webhook_events(transaction_id, created_at DESC)
            """
        )

        // Additional optimization: Composite index for getAllEvents() with ORDER BY
        // Covers: ORDER BY created_at DESC (for getAllEvents with limit)
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_created_desc
            ON webhook_events(created_at DESC)
            """
        )
    }
}
