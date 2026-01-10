package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 12: Add Composite Indexes for Query Performance
 *
 * Issue Identified:
 * - FinancialRecordDao.getFinancialRecordsUpdatedSince() queries by updated_at + is_deleted = 0 + ORDER BY updated_at DESC
 * - WebhookEventDao queries frequently ORDER BY created_at with various WHERE conditions
 * - Existing indexes don't fully optimize ORDER BY clauses
 *
 * Solution: Add composite indexes for optimal query performance
 *
 * Performance Improvements:
 * - Faster incremental data fetch for financial records
 * - Optimized webhook event queries with ORDER BY support
 * - Eliminate table scans for sorted results
 * - Better use of indexes for ordering (index-only scans where possible)
 *
 * Index Strategy:
 * - Financial records: Composite index for incremental updates
 * - Webhook events: Composite indexes for status + created_at ordering
 * - Webhook events: Composite indexes for transaction_id + created_at ordering
 * - Estimated query performance improvement: 2-5x for affected queries
 */
class Migration12 : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ===== FINANCIAL RECORDS TABLE =====

        // Partial composite index: updated_at DESC + is_deleted = 0
        // Used by: getFinancialRecordsUpdatedSince() - WHERE updated_at >= :since AND is_deleted = 0 ORDER BY updated_at DESC
        // Rationale: Optimizes incremental data fetch with ordering
        // Before: Full table scan or index-only scan with sort
        // After: Index scan with results already ordered (no extra sort step)
        database.execSQL(
            """
            CREATE INDEX idx_financial_updated_desc_active
            ON financial_records(updated_at DESC)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // ===== WEBHOOK EVENTS TABLE =====

        // Composite index: status + created_at ASC for PENDING events
        // Used by: getPendingEvents() - WHERE status = 'PENDING' ORDER BY created_at ASC
        // Used by: getPendingEventsByStatus() - WHERE status = :status AND (...) ORDER BY created_at ASC
        // Rationale: Supports both status filtering and creation time ordering
        // Before: Uses index(status) + filesort for ORDER BY
        // After: Single index scan with results already ordered
        database.execSQL(
            """
            CREATE INDEX idx_webhook_status_created
            ON webhook_events(status, created_at ASC)
            """.trimIndent()
        )

        // Composite index: transaction_id + created_at DESC
        // Used by: getEventsByTransactionId() - WHERE transaction_id = :transactionId ORDER BY created_at DESC
        // Rationale: Optimizes retrieving events for specific transaction in reverse chronological order
        // Before: Uses index(transaction_id) + filesort for ORDER BY DESC
        // After: Single index scan with results already ordered
        database.execSQL(
            """
            CREATE INDEX idx_webhook_transaction_created
            ON webhook_events(transaction_id, created_at DESC)
            """.trimIndent()
        )

        // Composite index: event_type + created_at DESC
        // Used by: getEventsByType() - WHERE event_type = :eventType ORDER BY created_at DESC
        // Rationale: Optimizes retrieving events by type in reverse chronological order
        // Before: Uses index(event_type) + filesort for ORDER BY DESC
        // After: Single index scan with results already ordered
        database.execSQL(
            """
            CREATE INDEX idx_webhook_type_created
            ON webhook_events(event_type, created_at DESC)
            """.trimIndent()
        )

        // Note: Indexes from Migration 11 remain intact
        // - Partial indexes for soft delete optimization are preserved
        // - WebhookEvent indexes: idempotency_key (unique), status, event_type, (status, next_retry_at), transaction_id
    }
}
