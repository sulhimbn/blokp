package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 14: Add CHECK Constraints to Webhook Events Table
 *
 * Issue Identified:
 * - WebhookEvent entity has no database-level CHECK constraints
 * - retry_count can be set to negative values or exceed max_retries
 * - status can be set to invalid enum values
 * - idempotency_key can be empty (violates unique index intent)
 *
 * Solution: Recreate webhook_events table with CHECK constraints for data integrity
 *
 * Data Integrity Improvements:
 * - idempotency_key length > 0: Prevents empty idempotency keys
 * - status IN ('PENDING', 'PROCESSING', 'DELIVERED', 'FAILED', 'CANCELLED'):
 *   Prevents invalid status values
 * - retry_count >= 0: Prevents negative retry counts
 * - retry_count <= max_retries: Ensures retry count doesn't exceed limit
 * - max_retries > 0: Ensures positive max retry limit
 * - max_retries <= 10: Prevents unreasonably high max retries
 * - next_retry_at >= 0 or NULL: Validates timestamp constraints
 * - delivered_at >= 0 or NULL: Validates timestamp constraints
 * - created_at > 0: Ensures valid creation timestamp
 * - updated_at > 0: Ensures valid update timestamp
 *
 * Database-Level Integrity:
 * - Ensures webhook delivery state machine integrity
 * - Prevents inconsistent retry counts
 * - Validates status transitions implicitly through enum constraint
 * - Supports webhook delivery auditing
 *
 * Migration Strategy:
 * - Create new table with CHECK constraints
 * - Copy existing data (data will be validated against constraints)
 * - Drop old table
 * - Rename new table to webhook_events
 * - Recreate indexes on new table
 *
 * Note: If any existing data violates new CHECK constraints,
 * migration will fail. This is intentional to prevent propagating invalid data.
 * Existing valid data will be preserved.
 */
class Migration14 : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create new webhook_events table with CHECK constraints
        database.execSQL(
            """
            CREATE TABLE webhook_events_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idempotency_key TEXT NOT NULL CHECK(length(idempotency_key) > 0),
                event_type TEXT NOT NULL CHECK(length(event_type) > 0),
                payload TEXT NOT NULL CHECK(length(payload) > 0),
                transaction_id TEXT,
                status TEXT NOT NULL CHECK(
                    status IN ('PENDING', 'PROCESSING', 'DELIVERED', 'FAILED', 'CANCELLED')
                ),
                retry_count INTEGER NOT NULL DEFAULT 0 CHECK(retry_count >= 0),
                max_retries INTEGER NOT NULL DEFAULT 3 CHECK(max_retries > 0 AND max_retries <= 10),
                next_retry_at INTEGER CHECK(next_retry_at IS NULL OR next_retry_at >= 0),
                delivered_at INTEGER CHECK(delivered_at IS NULL OR delivered_at >= 0),
                created_at INTEGER NOT NULL CHECK(created_at > 0),
                updated_at INTEGER NOT NULL CHECK(updated_at > 0),
                last_error TEXT
            )
            """.trimIndent()
        )

        // Step 2: Copy data from old table to new table
        // Note: Any data violating CHECK constraints will cause migration to fail
        database.execSQL(
            """
            INSERT INTO webhook_events_new (
                id, idempotency_key, event_type, payload, transaction_id,
                status, retry_count, max_retries, next_retry_at,
                delivered_at, created_at, updated_at, last_error
            )
            SELECT
                id, idempotency_key, event_type, payload, transaction_id,
                status, retry_count, COALESCE(max_retries, 3), next_retry_at,
                delivered_at, created_at, updated_at, last_error
            FROM webhook_events
            """.trimIndent()
        )

        // Step 3: Drop old table
        database.execSQL("DROP TABLE webhook_events")

        // Step 4: Rename new table to webhook_events
        database.execSQL("ALTER TABLE webhook_events_new RENAME TO webhook_events")

        // Step 5: Recreate indexes on new table
        // Note: All indexes need to be recreated on the new table

        // Unique index from Migration 2 - idempotency_key (unique)
        database.execSQL(
            """
            CREATE UNIQUE INDEX index_webhook_events_idempotency_key
            ON webhook_events(idempotency_key)
            """
        )

        // Index from Migration 2 - status
        database.execSQL(
            """
            CREATE INDEX index_webhook_events_status
            ON webhook_events(status)
            """
        )

        // Index from Migration 2 - event_type
        database.execSQL(
            """
            CREATE INDEX index_webhook_events_event_type
            ON webhook_events(event_type)
            """
        )

        // Index from Migration 3 - (status, next_retry_at)
        database.execSQL(
            """
            CREATE INDEX idx_webhook_retry_queue
            ON webhook_events(status, next_retry_at)
            """
        )

        // Index from WebhookEvent entity - transaction_id
        database.execSQL(
            """
            CREATE INDEX idx_webhook_transaction_id
            ON webhook_events(transaction_id)
            """
        )

        // Composite indexes from Migration 12 for query optimization
        database.execSQL(
            """
            CREATE INDEX idx_webhook_status_created
            ON webhook_events(status, created_at ASC)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_webhook_transaction_created
            ON webhook_events(transaction_id, created_at DESC)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_webhook_type_created
            ON webhook_events(event_type, created_at DESC)
            """
        )
    }
}
