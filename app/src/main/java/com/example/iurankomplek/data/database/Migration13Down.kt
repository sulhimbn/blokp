package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 13 Down: Revert CHECK Constraints from Transactions Table
 *
 * Reverses Migration 13 by removing CHECK constraints from transactions table.
 *
 * Note: This is a reversible migration that preserves all data while removing
 * database-level validation. Application-level validation in Transaction.init()
 * will continue to enforce data integrity.
 *
 * Migration Strategy:
 * - Create new table without CHECK constraints
 * - Copy all existing data
 * - Drop table with CHECK constraints
 * - Rename new table to transactions
 * - Recreate indexes on new table
 *
 * Data Loss: None (all data is preserved)
 */
val Migration13Down = object : Migration(13, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Disable foreign keys temporarily
        database.execSQL("PRAGMA foreign_keys = OFF")

        // Step 2: Create new transactions table WITHOUT CHECK constraints
        database.execSQL(
            """
            CREATE TABLE transactions_new (
                id TEXT PRIMARY KEY NOT NULL,
                user_id INTEGER NOT NULL,
                amount NUMERIC NOT NULL,
                currency TEXT NOT NULL DEFAULT 'IDR',
                status TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                description TEXT NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                metadata TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
            )
            """.trimIndent()
        )

        // Step 3: Copy all data from old table to new table
        database.execSQL(
            """
            INSERT INTO transactions_new (
                id, user_id, amount, currency, status, payment_method,
                description, is_deleted, created_at, updated_at, metadata
            )
            SELECT
                id, user_id, amount, currency, payment_method,
                description, is_deleted, created_at, updated_at, COALESCE(metadata, '')
            FROM transactions
            """.trimIndent()
        )

        // Step 4: Drop table with CHECK constraints
        database.execSQL("DROP TABLE transactions")

        // Step 5: Rename new table to transactions
        database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // Step 6: Recreate indexes on new table
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_updated
            ON transactions(user_id, updated_at DESC)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_id
            ON transactions(user_id)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_status
            ON transactions(status)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_status
            ON transactions(user_id, status)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_created_at
            ON transactions(created_at DESC)
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_updated_at
            ON transactions(updated_at)
            """
        )

        // Partial indexes for soft-delete optimization
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_active
            ON transactions(user_id)
            WHERE is_deleted = 0
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_status_active
            ON transactions(status)
            WHERE is_deleted = 0
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_status_active
            ON transactions(user_id, status)
            WHERE is_deleted = 0
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_created_at_active
            ON transactions(created_at DESC)
            WHERE is_deleted = 0
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_transactions_status_deleted
            ON transactions(status, is_deleted)
            WHERE is_deleted = 0
            """
        )

        // Step 7: Re-enable foreign keys
        database.execSQL("PRAGMA foreign_keys = ON")
    }
}
