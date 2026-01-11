package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 23 Down: Remove Foreign Key Constraint from WebhookEvent (Rollback)
 *
 * Rollback Strategy:
 * - Recreate webhook_events table without FK constraint
 * - Restore original schema from Migration22
 * - Preserves all data
 *
 * Safety:
 * - Removes referential integrity enforcement
 * - Allows orphaned webhook_events
 * - Data remains intact
 * - Instant rollback
 */
object Migration23Down : Migration(23, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQLite doesn't support dropping FK constraints directly
        // Must recreate table without FK and migrate data
        
        // Step 1: Create table without FK constraint (original schema)
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
                last_error TEXT,
                is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1))
            )
            """
        )
        
        // Step 2: Copy all data
        database.execSQL(
            """
            INSERT INTO webhook_events_new
            (id, idempotency_key, event_type, payload, transaction_id, status,
             retry_count, max_retries, next_retry_at, delivered_at,
             created_at, updated_at, last_error, is_deleted)
            SELECT id, idempotency_key, event_type, payload, transaction_id, status,
                   retry_count, max_retries, next_retry_at, delivered_at,
                   created_at, updated_at, last_error, is_deleted
            FROM webhook_events
            """
        )
        
        // Step 3: Drop old table
        database.execSQL("DROP TABLE webhook_events")
        
        // Step 4: Rename new table
        database.execSQL("ALTER TABLE webhook_events_new RENAME TO webhook_events")
        
        // Step 5: Recreate all indexes (same as Migration22)
        database.execSQL("CREATE UNIQUE INDEX index_webhook_events_idempotency_key ON webhook_events(idempotency_key)")
        
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_active
            ON webhook_events(is_deleted)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_retry_active
            ON webhook_events(status, next_retry_at)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_created_active
            ON webhook_events(status, created_at)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_transaction_created_active
            ON webhook_events(transaction_id, created_at DESC)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_type_created_active
            ON webhook_events(event_type, created_at DESC)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_delivered_active
            ON webhook_events(status, delivered_at)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL(
            """
            CREATE INDEX idx_webhook_events_status_failed_active
            ON webhook_events(status, created_at)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL(
            """
            CREATE UNIQUE INDEX idx_webhook_events_idempotency_key_active
            ON webhook_events(idempotency_key)
            WHERE is_deleted = 0
            """
        )
        
        database.execSQL("CREATE INDEX index_webhook_events_transaction_id ON webhook_events(transaction_id)")
        database.execSQL("CREATE INDEX index_webhook_events_status ON webhook_events(status)")
        database.execSQL("CREATE INDEX index_webhook_events_event_type ON webhook_events(event_type)")
        database.execSQL("CREATE INDEX index_webhook_events_status_next_retry_at ON webhook_events(status, next_retry_at)")
    }
}
