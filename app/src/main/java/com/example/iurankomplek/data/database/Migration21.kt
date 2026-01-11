package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 21: Add Soft-Delete Pattern to WebhookEvent Table
 *
 * Issue Identified (DATA-001):
 * - WebhookEvent table lacks soft-delete pattern
 * - All other tables (users, financial_records, transactions) use is_deleted column
 * - Inconsistent data deletion strategy across database
 * - Hard DELETE prevents event recovery and audit trail
 *
 * Architectural Impact:
 * - WebhookEventCleaner uses hard DELETE (DELETE FROM webhook_events)
 * - Users, FinancialRecords, Transactions use soft-delete (UPDATE ... SET is_deleted = 1)
 * - Breaks consistency in data lifecycle management
 * - Prevents recovery of deleted webhook events
 *
 * Solution:
 * - Add is_deleted column to webhook_events table
 * - Update WebhookEvent entity to include is_deleted field
 * - Update WebhookEventDao queries to filter on is_deleted = 0
 * - Update WebhookEventCleaner to use soft-delete
 * - Add partial indexes on is_deleted WHERE is_deleted = 0
 * - Maintain backward compatibility for existing data
 *
 * Data Integrity:
 * - Existing records: is_deleted = 0 (default, all existing events active)
 * - New records: is_deleted = 0 by default
 * - Soft-delete: UPDATE webhook_events SET is_deleted = 1 WHERE id = :id
 * - Hard-delete: Still available for cleanup operations (DELETE FROM webhook_events WHERE is_deleted = 1 AND created_at < :cutoff)
 *
 * Migration Safety:
 * - Non-destructive: Adds new column with default value
 * - Reversible: Migration21Down removes is_deleted column
 * - Zero data loss: Existing records preserved with is_deleted = 0
 * - Backward compatible: Queries without is_deleted filter still work (but include deleted records)
 */
class Migration21 : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add is_deleted column with default value 0 (active)
        database.execSQL(
            """
            ALTER TABLE webhook_events
            ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1))
            """
        )

        // Create partial indexes on frequently queried columns for active records
        // Covers: getEventById, getEventByIdempotencyKey, getPendingEvents, getEventsByType
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_active
            ON webhook_events(is_deleted)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index on (status, next_retry_at) for active records
        // Covers: getPendingEventsByStatus query
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_retry_active
            ON webhook_events(status, next_retry_at)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index on (status, created_at) for active records
        // Covers: getPendingEvents, getEventsByType with ORDER BY created_at
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_created_active
            ON webhook_events(status, created_at)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index on (transaction_id, created_at) for active records
        // Covers: getEventsByTransactionId query
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_transaction_created_active
            ON webhook_events(transaction_id, created_at DESC)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index on (event_type, created_at) for active records
        // Covers: getEventsByType query
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_type_created_active
            ON webhook_events(event_type, created_at DESC)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index on (status, delivered_at) for active records
        // Covers: getDeliveredEventsOlderThan query
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_delivered_active
            ON webhook_events(status, delivered_at)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index on (status, created_at) for active records
        // Covers: getFailedEventsOlderThan query
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_failed_active
            ON webhook_events(status, created_at)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index on (idempotency_key) for active records
        // Covers: getEventByIdempotencyKey query
        database.execSQL(
            """
            CREATE UNIQUE INDEX idx_webhook_events_idempotency_key_active
            ON webhook_events(idempotency_key)
            WHERE is_deleted = 0
            """
        )
    }
}

/**
 * Migration 21 Down: Remove Soft-Delete Pattern from WebhookEvent Table (Rollback)
 *
 * Rollback Strategy:
 * - Drop all partial indexes created in Migration21
 * - Remove is_deleted column from webhook_events table
 * - Reverts to previous schema (hard-delete pattern)
 *
 * Safety:
 * - Permanently deletes is_deleted column
 * - Existing soft-deleted records become permanently deleted
 * - Recommended: Export soft-deleted records before rollback
 * - Consider data retention requirements
 */
object Migration21Down : Migration(21, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop all partial indexes created in Migration21
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_active")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_status_retry_active")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_status_created_active")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_transaction_created_active")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_type_created_active")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_status_delivered_active")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_status_failed_active")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_idempotency_key_active")

        // SQLite doesn't support DROP COLUMN directly
        // Recreate table without is_deleted column
        database.execSQL(
            """
            CREATE TABLE webhook_events_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idempotency_key TEXT NOT NULL,
                event_type TEXT NOT NULL,
                payload TEXT NOT NULL,
                transaction_id TEXT,
                status TEXT NOT NULL,
                retry_count INTEGER NOT NULL DEFAULT 0,
                max_retries INTEGER NOT NULL DEFAULT 3,
                next_retry_at INTEGER,
                delivered_at INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                last_error TEXT
            )
            """
        )

        // Copy active records (is_deleted = 0) to new table
        database.execSQL(
            """
            INSERT INTO webhook_events_new
            SELECT id, idempotency_key, event_type, payload, transaction_id, status,
                   retry_count, max_retries, next_retry_at, delivered_at,
                   created_at, updated_at, last_error
            FROM webhook_events
            WHERE is_deleted = 0
            """
        )

        // Drop old table
        database.execSQL("DROP TABLE webhook_events")

        // Rename new table to original name
        database.execSQL("ALTER TABLE webhook_events_new RENAME TO webhook_events")

        // Recreate original indexes (without partial indexes)
        database.execSQL("CREATE UNIQUE INDEX index_webhook_events_idempotency_key ON webhook_events(idempotency_key)")
        database.execSQL("CREATE INDEX index_webhook_events_status ON webhook_events(status)")
        database.execSQL("CREATE INDEX index_webhook_events_event_type ON webhook_events(event_type)")
        database.execSQL("CREATE INDEX index_webhook_events_status_next_retry_at ON webhook_events(status, next_retry_at)")
        database.execSQL("CREATE INDEX index_webhook_events_transaction_id ON webhook_events(transaction_id)")
    }
}
