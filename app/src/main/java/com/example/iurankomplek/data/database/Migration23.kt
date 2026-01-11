package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 23: Add Foreign Key Constraint - WebhookEvent.transaction_id → Transaction.id
 *
 * Issue Identified (DATA-008):
 * - WebhookEvent.transaction_id references Transaction.id without FK constraint
 * - WebhookEventDao has getEventsByTransactionId() query indicating relationship
 * - Index exists on transaction_id but no referential integrity enforcement
 * - Orphaned webhook_events possible if transaction is deleted
 *
 * Root Cause:
 * - WebhookEvent entity missing @ForeignKey annotation
 * - Only index exists: Index(value = ["transaction_id"])
 * - No CASCADE/RESTRICT action defined
 * - Database cannot enforce referential integrity
 *
 * Data Integrity Impact:
 * - Orphaned webhook_events when transaction deleted
 * - Cannot trace webhook delivery history
 * - Inconsistent with other tables (users, financial_records, transactions all have FKs)
 * - No guarantee transaction_id points to valid transaction
 *
 * Affected Queries:
 * - getEventsByTransactionId() - queries by transaction_id but no FK guarantee
 * - INSERT - can insert invalid transaction_id
 * - DELETE - can delete transaction without handling webhook_events
 *
 * Business Rationale for ON DELETE SET NULL:
 * - transaction_id is nullable (String?)
 * - Webhook events should be preserved for audit trail
 * - Setting NULL indicates transaction no longer available
 * - Preserves webhook delivery history for troubleshooting
 * - Prevents cascade delete of webhook events (important for monitoring)
 *
 * Foreign Key Constraint Details:
 * - Table: webhook_events
 * - Column: transaction_id
 * - References: transactions(id)
 * - ON DELETE: SET NULL (preserve webhook events, NULL indicates deleted transaction)
 * - ON UPDATE: CASCADE (keep references in sync if transaction.id changes)
 * - DEFERRABLE: INITIALLY DEFERRED (allows transaction-level integrity checks)
 *
 * Migration Safety:
 * - Non-destructive: Only adds FK constraint
 * - No data modification required
 * - Reversible: Migration23Down drops FK constraint
 * - Zero data loss: Existing data preserved
 * - Backward Compatible: Existing queries still work
 *
 * Verification:
 * - Test INSERT with invalid transaction_id → should fail (FK violation)
 * - Test DELETE transaction → webhook_events.transaction_id should be set to NULL
 * - Test UPDATE transaction.id → webhook_events should cascade
 *
 * Compatibility with Soft Delete:
 * - Soft delete uses is_deleted flag
 * - FK constraint works with soft delete (SET NULL on hard delete)
 * - TransactionRepository uses hard DELETE for transactions (no soft delete)
 * - Appropriate to use FK constraint
 */
class Migration23 : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQLite doesn't support adding FOREIGN KEY constraints via ALTER TABLE
        // Must recreate table with FK constraint and migrate data

        // Step 1: Create new table with FK constraint
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
                is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1)),
                FOREIGN KEY(transaction_id)
                    REFERENCES transactions(id)
                    ON DELETE SET NULL
                    ON UPDATE CASCADE
                    DEFERRABLE INITIALLY DEFERRED
            )
            """
        )

        // Step 2: Copy all data from old table to new table
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

        // Step 4: Rename new table to original name
        database.execSQL("ALTER TABLE webhook_events_new RENAME TO webhook_events")

        // Step 5: Recreate all indexes on new table

        // Unique index on idempotency_key (from entity)
        database.execSQL("CREATE UNIQUE INDEX index_webhook_events_idempotency_key ON webhook_events(idempotency_key)")

        // Partial indexes for active records (from Migration21)
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

        // Index on transaction_id (from entity)
        database.execSQL("CREATE INDEX index_webhook_events_transaction_id ON webhook_events(transaction_id)")

        // Index on status (from entity)
        database.execSQL("CREATE INDEX index_webhook_events_status ON webhook_events(status)")

        // Index on event_type (from entity)
        database.execSQL("CREATE INDEX index_webhook_events_event_type ON webhook_events(event_type)")

        // Index on (status, next_retry_at) (from entity)
        database.execSQL("CREATE INDEX index_webhook_events_status_next_retry_at ON webhook_events(status, next_retry_at)")
    }
}

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
