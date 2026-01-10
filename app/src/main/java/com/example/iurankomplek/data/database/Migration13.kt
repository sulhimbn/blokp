package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 13: Add CHECK Constraints to Transactions Table
 *
 * Issue Identified:
 * - Transaction entity has validation in init block (application-level)
 * - TransactionConstraints defines CHECK constraints (documentation-level)
 * - But actual database schema lacks CHECK constraints (no data integrity at DB level)
 * - This allows invalid data to be inserted if validation is bypassed
 *
 * Solution: Recreate transactions table with CHECK constraints for data integrity
 *
 * Data Integrity Improvements:
 * - AMOUNT > 0: Prevents zero or negative amounts
 * - AMOUNT <= 999999999.99: Enforces maximum transaction amount
 * - STATUS IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'):
 *   Prevents invalid status values
 * - PAYMENT_METHOD IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT'):
 *   Prevents invalid payment method values
 * - CURRENCY length <= 3: Enforces ISO 4217 currency code length
 * - DESCRIPTION length > 0 and <= 500: Prevents empty or too-long descriptions
 * - METADATA length <= 2000: Prevents metadata overflow
 * - IS_DELETED IN (0, 1): Prevents boolean values other than 0 or 1
 *
 * Database-Level Integrity:
 * - Ensures data validation even if application-level checks are bypassed
 * - Prevents data corruption from direct database modifications
 * - Improves data consistency across application lifetime
 * - Supports data integrity audits
 *
 * Migration Strategy:
 * - Create new table with CHECK constraints
 * - Copy existing data (data will be validated against constraints)
 * - Drop old table
 * - Rename new table to transactions
 * - Recreate indexes on new table
 *
 * Note: If any existing data violates the new CHECK constraints,
 * the migration will fail. This is intentional to prevent propagating invalid data.
 * Existing valid data will be preserved.
 */
class Migration13 : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Disable foreign keys temporarily
        database.execSQL("PRAGMA foreign_keys = OFF")

        // Step 2: Create new transactions table with CHECK constraints
        database.execSQL(
            """
            CREATE TABLE transactions_new (
                id TEXT PRIMARY KEY NOT NULL CHECK(length(id) > 0),
                user_id INTEGER NOT NULL,
                amount NUMERIC NOT NULL CHECK(amount > 0 AND amount <= 999999999.99),
                currency TEXT NOT NULL DEFAULT 'IDR' CHECK(length(currency) <= 3),
                status TEXT NOT NULL CHECK(
                    status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')
                ),
                payment_method TEXT NOT NULL CHECK(
                    payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT')
                ),
                description TEXT NOT NULL CHECK(length(description) > 0 AND length(description) <= 500),
                is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1)),
                created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                metadata TEXT NOT NULL DEFAULT '' CHECK(length(metadata) <= 2000),
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
            )
            """.trimIndent()
        )

        // Step 3: Copy data from old table to new table
        // Note: Any data violating CHECK constraints will cause migration to fail
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

        // Step 4: Drop old table
        database.execSQL("DROP TABLE transactions")

        // Step 5: Rename new table to transactions
        database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // Step 6: Recreate indexes on the new table
        // Note: All indexes need to be recreated on the new table

        // Index from Migration 3 (user_id, updated_at) - recreated with DESC ordering
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_updated
            ON transactions(user_id, updated_at DESC)
            """
        )

        // Index from Transaction entity - user_id
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_id
            ON transactions(user_id)
            """
        )

        // Index from Transaction entity - status
        database.execSQL(
            """
            CREATE INDEX idx_transactions_status
            ON transactions(status)
            """
        )

        // Index from Transaction entity - (user_id, status)
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_status
            ON transactions(user_id, status)
            """
        )

        // Index from Transaction entity - created_at DESC
        database.execSQL(
            """
            CREATE INDEX idx_transactions_created_at
            ON transactions(created_at DESC)
            """
        )

        // Index from Transaction entity - updated_at
        database.execSQL(
            """
            CREATE INDEX idx_transactions_updated_at
            ON transactions(updated_at)
            """
        )

        // Partial indexes from Migration 11 for soft-delete optimization
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

        // Partial index from Migration 6
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
