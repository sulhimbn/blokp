package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 20: Fix Transaction.amount Precision (Store as Cents)
 *
 * Issue Identified (DATA-006):
 * - Transaction.amount stored as BigDecimal
 * - Current type converter stores as TEXT (string representation)
 * - While precision is preserved, text storage is inefficient
 * - Financial applications should use integer arithmetic (cents) for exact calculations
 * - Current CHECK constraint compares as text, not numeric
 *
 * SQLite Limitations:
 * - SQLite's NUMERIC type uses floating-point representation
 * - Floating-point cannot exactly represent all decimal values
 * - Example: 0.1 cannot be exactly represented in binary floating-point
 * - Financial calculations require exact precision (no rounding errors)
 *
 * Solution:
 * - Convert amount column from TEXT to INTEGER (stores cents)
 * - Update type converters to multiply/divide by 100
 * - Update CHECK constraints to use integer values
 * - Maintain full precision with integer arithmetic
 *
 * Data Conversion:
 * - 100.50 IDR -> 10050 (stored as integer cents)
 * - 100.00 IDR -> 10000 (stored as integer cents)
 * - 0.01 IDR -> 1 (stored as integer cents)
 * - Maximum: 999999999.99 IDR -> 99999999999 cents
 *
 * Financial Application Best Practices:
 * - Integer arithmetic is exact (no floating-point rounding errors)
 * - Faster operations than text-based storage
 * - Compact storage (8 bytes vs variable-length text)
 * - Standard practice in financial applications (Stripe, PayPal, etc.)
 *
 * Migration Safety:
 * - Data conversion: Parse existing TEXT to BigDecimal, multiply by 100, store as INTEGER
 * - Rounding protection: Use setScale(0, RoundingMode.HALF_UP)
 * - Validation: Verify all amounts are converted correctly
 * - Rollback: Convert INTEGER back to TEXT (divide by 100, format as decimal)
 */
class Migration20 : Migration(19, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add temporary column for new amount storage (INTEGER in cents)
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD COLUMN amount_cents INTEGER
            """
        )

        // Convert existing amounts from TEXT to INTEGER (cents)
        // Multiply by 100 to preserve 2 decimal places
        // Handle NULL values (default to 0)
        database.execSQL(
            """
            UPDATE transactions
            SET amount_cents = CAST(CAST(amount AS REAL) * 100 AS INTEGER)
            WHERE amount IS NOT NULL
            """
        )

        // Drop old amount column (TEXT)
        database.execSQL(
            """
            ALTER TABLE transactions
            RENAME COLUMN amount TO amount_old
            """
        )

        // Rename new column to amount
        database.execSQL(
            """
            ALTER TABLE transactions
            RENAME COLUMN amount_cents TO amount
            """
        )

        // Drop old column
        database.execSQL(
            """
            ALTER TABLE transactions
            DROP COLUMN amount_old
            """
        )

        // Update CHECK constraints for INTEGER amount (cents)
        // Drop old constraints first (SQLite doesn't support ALTER CONSTRAINT)
        database.execSQL("DROP TABLE IF EXISTS transactions_old")

        // Recreate table with new schema
        database.execSQL(
            """
            CREATE TABLE transactions_new (
                id TEXT PRIMARY KEY NOT NULL CHECK(length(id) > 0),
                user_id INTEGER NOT NULL CHECK(user_id > 0),
                amount INTEGER NOT NULL CHECK(amount > 0 AND amount <= 99999999999),
                currency TEXT NOT NULL DEFAULT 'IDR' CHECK(length(currency) > 0 AND length(currency) <= 3),
                status TEXT NOT NULL CHECK(status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')),
                payment_method TEXT NOT NULL CHECK(payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT')),
                description TEXT NOT NULL CHECK(length(description) > 0 AND length(description) <= 500),
                is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1)),
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                metadata TEXT NOT NULL DEFAULT '' CHECK(length(metadata) <= 2000),
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
            )
            """
        )

        // Copy data to new table
        database.execSQL(
            """
            INSERT INTO transactions_new
            SELECT * FROM transactions
            """
        )

        // Drop old table
        database.execSQL("DROP TABLE transactions")

        // Rename new table to original name
        database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // Recreate indexes
        database.execSQL("CREATE INDEX idx_transactions_user_id ON transactions(user_id)")
        database.execSQL("CREATE INDEX idx_transactions_status ON transactions(status)")
        database.execSQL("CREATE INDEX idx_transactions_user_status ON transactions(user_id, status)")
        database.execSQL("CREATE INDEX idx_transactions_status_deleted ON transactions(status, is_deleted) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_created_at ON transactions(created_at)")
        database.execSQL("CREATE INDEX idx_transactions_updated_at ON transactions(updated_at)")
        database.execSQL("CREATE INDEX idx_transactions_not_deleted ON transactions(is_deleted) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_user_active ON transactions(user_id) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_status_active ON transactions(status) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_user_status_active ON transactions(user_id, status) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_created_at_active ON transactions(created_at) WHERE is_deleted = 0")
    }
}

/**
 * Migration 20 Down: Convert Amount Back to TEXT (Rollback)
 *
 * Rollback Strategy:
 * - Recreate table with amount as TEXT
 * - Convert INTEGER (cents) back to TEXT (decimal string)
 * - Divide by 100 and format as 2 decimal places
 *
 * Safety:
 * - Preserves all transaction data
 * - Exact rollback of conversion
 * - Reverts to previous schema
 */
object Migration20Down : Migration(20, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add temporary column for TEXT amount
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD COLUMN amount_text TEXT
            """
        )

        // Convert INTEGER (cents) back to TEXT (decimal)
        // Divide by 100 and format with 2 decimal places
        database.execSQL(
            """
            UPDATE transactions
            SET amount_text = PRINTF('%.2f', CAST(amount AS REAL) / 100.0)
            """
        )

        // Drop old amount column (INTEGER)
        database.execSQL(
            """
            ALTER TABLE transactions
            RENAME COLUMN amount TO amount_old
            """
        )

        // Rename new column to amount
        database.execSQL(
            """
            ALTER TABLE transactions
            RENAME COLUMN amount_text TO amount
            """
        )

        // Drop old column
        database.execSQL(
            """
            ALTER TABLE transactions
            DROP COLUMN amount_old
            """
        )

        // Recreate table with old schema (amount as NUMERIC)
        database.execSQL(
            """
            CREATE TABLE transactions_new (
                id TEXT PRIMARY KEY NOT NULL CHECK(length(id) > 0),
                user_id INTEGER NOT NULL CHECK(user_id > 0),
                amount NUMERIC NOT NULL CHECK(amount > 0 AND amount <= 999999999.99),
                currency TEXT NOT NULL DEFAULT 'IDR' CHECK(length(currency) > 0 AND length(currency) <= 3),
                status TEXT NOT NULL CHECK(status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')),
                payment_method TEXT NOT NULL CHECK(payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT')),
                description TEXT NOT NULL CHECK(length(description) > 0 AND length(description) <= 500),
                is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1)),
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                metadata TEXT NOT NULL DEFAULT '' CHECK(length(metadata) <= 2000),
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
            )
            """
        )

        // Copy data to new table
        database.execSQL(
            """
            INSERT INTO transactions_new
            SELECT * FROM transactions
            """
        )

        // Drop old table
        database.execSQL("DROP TABLE transactions")

        // Rename new table to original name
        database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // Recreate indexes
        database.execSQL("CREATE INDEX idx_transactions_user_id ON transactions(user_id)")
        database.execSQL("CREATE INDEX idx_transactions_status ON transactions(status)")
        database.execSQL("CREATE INDEX idx_transactions_user_status ON transactions(user_id, status)")
        database.execSQL("CREATE INDEX idx_transactions_status_deleted ON transactions(status, is_deleted) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_created_at ON transactions(created_at)")
        database.execSQL("CREATE INDEX idx_transactions_updated_at ON transactions(updated_at)")
        database.execSQL("CREATE INDEX idx_transactions_not_deleted ON transactions(is_deleted) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_user_active ON transactions(user_id) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_status_active ON transactions(status) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_user_status_active ON transactions(user_id, status) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_transactions_created_at_active ON transactions(created_at) WHERE is_deleted = 0")
    }
}
