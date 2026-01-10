package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 14 Down: Revert CHECK Constraints from Webhook Events Table
 *
 * Reverses Migration 14 by removing CHECK constraints from webhook_events table.
 *
 * Note: This is a reversible migration that preserves all data while removing
 * database-level validation. Application-level validation should enforce
 * data integrity if needed.
 *
 * Migration Strategy:
 * - Create new table without CHECK constraints
 * - Copy all existing data
 * - Drop table with CHECK constraints
 * - Rename new table to webhook_events
 * - Recreate indexes on new table
 *
 * Data Loss: None (all data is preserved)
 */
val Migration14Down = object : Migration(14, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create new webhook_events table WITHOUT CHECK constraints
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
                max_retries INTEGER NOT NULL DEFAULT 5,
                next_retry_at INTEGER,
                delivered_at INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                last_error TEXT
            )
            """.trimIndent()
        )

        // Step 2: Copy all data from old table to new table
        database.execSQL(
            """
            INSERT INTO webhook_events_new (
                id, idempotency_key, event_type, payload, transaction_id,
                status, retry_count, max_retries, next_retry_at,
                delivered_at, created_at, updated_at, last_error
            )
            SELECT
                id, idempotency_key, event_type, payload, transaction_id,
                status, retry_count, max_retries, next_retry_at,
                delivered_at, created_at, updated_at, last_error
            FROM webhook_events
            """.trimIndent()
        )

        // Step 3: Drop table with CHECK constraints
        database.execSQL("DROP TABLE webhook_events")

        // Step 4: Rename new table to webhook_events
        database.execSQL("ALTER TABLE webhook_events_new RENAME TO webhook_events")

        // Step 5: Recreate indexes on new table
        database.execSQL(
            """
            CREATE UNIQUE INDEX index_webhook_events_idempotency_key
            ON webhook_events(idempotency_key)
            """
        )

        database.execSQL(
            """
            CREATE INDEX index_webhook_events_status
            ON webhook_events(status)
            """
        )

        database.execSQL(
            """
            CREATE INDEX index_webhook_events_event_type
            ON webhook_events(event_type)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_webhook_retry_queue
            ON webhook_events(status, next_retry_at)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_webhook_transaction_id
            ON webhook_events(transaction_id)
            """
        )

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
