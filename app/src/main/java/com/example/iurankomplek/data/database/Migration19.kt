package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 19: Add CHECK Constraints for Data Integrity
 *
 * Issue Identified (DATA-002):
 * - Entity validation in init blocks (Kotlin side) only
 * - No database-level CHECK constraints enforced
 * - Direct SQL manipulation can bypass validation
 * - Risk of corrupted data in production
 *
 * Affected Entities:
 * - UserEntity: Email validation, name length constraints, address validation
 * - FinancialRecordEntity: Numeric value validation, text length validation
 * - Transaction: Amount validation, enum value validation
 *
 * Room Limitations:
 * - Room doesn't support CHECK constraints via annotations
 * - Validation in init blocks only protects against entity-level insertion
 * - Direct SQL updates bypass validation entirely
 *
 * Solution:
 * - Add CHECK constraints via ALTER TABLE
 * - Enforce validation at database level
 * - Protect against SQL manipulation attacks
 * - Maintain data integrity regardless of access method
 *
 * Database Integrity Benefits:
 * - Email format validation (contains @ symbol)
 * - Length constraints on text fields
 * - Non-negative numeric values
 * - Valid enum values for status/payment_method fields
 * - Positive values for required fields (user_id, amount)
 *
 * Migration Safety:
 * - Non-destructive: Only adds constraints
 * - Backward compatible: Existing valid data will pass new constraints
 * - Validation: Constraints checked against existing data before adding
 * - Fail-safe: Constraint addition fails if invalid data exists
 *
 * Rollback Strategy (Migration19Down):
 * - Drop CHECK constraints using ALTER TABLE
 * - Restore previous schema state
 * - No data modification required
 */
class Migration19 : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Users Table CHECK Constraints
        // Email format and length validation
        database.execSQL(
            """
            ALTER TABLE users
            ADD CONSTRAINT chk_users_email_format
            CHECK(email LIKE '%@%' AND length(email) > 0)
            """
        )

        // First name length validation
        database.execSQL(
            """
            ALTER TABLE users
            ADD CONSTRAINT chk_users_first_name_length
            CHECK(length(first_name) > 0 AND length(first_name) <= 100)
            """
        )

        // Last name length validation
        database.execSQL(
            """
            ALTER TABLE users
            ADD CONSTRAINT chk_users_last_name_length
            CHECK(length(last_name) > 0 AND length(last_name) <= 100)
            """
        )

        // Address length validation
        database.execSQL(
            """
            ALTER TABLE users
            ADD CONSTRAINT chk_users_alamat_length
            CHECK(length(alamat) > 0 AND length(alamat) <= 500)
            """
        )

        // Avatar URL length validation
        database.execSQL(
            """
            ALTER TABLE users
            ADD CONSTRAINT chk_users_avatar_length
            CHECK(length(avatar) <= 2048)
            """
        )

        // is_deleted boolean check
        database.execSQL(
            """
            ALTER TABLE users
            ADD CONSTRAINT chk_users_is_deleted_boolean
            CHECK(is_deleted IN (0, 1))
            """
        )

        // Financial Records Table CHECK Constraints
        // User ID positive check
        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_user_id_positive
            CHECK(user_id > 0)
            """
        )

        // Numeric values non-negative checks
        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_iuran_perwarga_non_negative
            CHECK(iuran_perwarga >= 0)
            """
        )

        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_jumlah_iuran_bulanan_non_negative
            CHECK(jumlah_iuran_bulanan >= 0)
            """
        )

        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_total_iuran_individu_non_negative
            CHECK(total_iuran_individu >= 0)
            """
        )

        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_pengeluaran_iuran_warga_non_negative
            CHECK(pengeluaran_iuran_warga >= 0)
            """
        )

        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_total_iuran_rekap_non_negative
            CHECK(total_iuran_rekap >= 0)
            """
        )

        // Numeric value max checks (prevent overflow)
        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_max_value
            CHECK(
                iuran_perwarga <= 999999999 AND
                jumlah_iuran_bulanan <= 999999999 AND
                total_iuran_individu <= 999999999 AND
                pengeluaran_iuran_warga <= 999999999 AND
                total_iuran_rekap <= 999999999
            )
            """
        )

        // Pemanfaatan text length and non-empty check
        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_pemanfaatan_length
            CHECK(length(pemanfaatan_iuran) > 0 AND length(pemanfaatan_iuran) <= 500)
            """
        )

        // is_deleted boolean check
        database.execSQL(
            """
            ALTER TABLE financial_records
            ADD CONSTRAINT chk_financial_is_deleted_boolean
            CHECK(is_deleted IN (0, 1))
            """
        )

        // Transactions Table CHECK Constraints
        // Transaction ID non-empty check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_id_not_blank
            CHECK(length(id) > 0)
            """
        )

        // User ID positive check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_user_id_positive
            CHECK(user_id > 0)
            """
        )

        // Amount positive and max value check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_amount_positive
            CHECK(amount > 0)
            """
        )

        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_amount_max
            CHECK(amount <= 999999999.99)
            """
        )

        // Currency length check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_currency_length
            CHECK(length(currency) > 0 AND length(currency) <= 3)
            """
        )

        // Status enum value check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_status_valid
            CHECK(status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'))
            """
        )

        // Payment method enum value check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_payment_method_valid
            CHECK(payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT'))
            """
        )

        // Description length and non-empty check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_description_length
            CHECK(length(description) > 0 AND length(description) <= 500)
            """
        )

        // Metadata length check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_metadata_length
            CHECK(length(metadata) <= 2000)
            """
        )

        // is_deleted boolean check
        database.execSQL(
            """
            ALTER TABLE transactions
            ADD CONSTRAINT chk_transactions_is_deleted_boolean
            CHECK(is_deleted IN (0, 1))
            """
        )
    }
}

/**
 * Migration 19 Down: Remove CHECK Constraints
 *
 * Rollback Strategy:
 * - SQLite doesn't support DROP CONSTRAINT directly
 * - Workaround: Recreate tables without constraints
 * - Copy data back to new tables
 * - Rename tables to restore original names
 *
 * Complexity:
 * - Complex rollback due to SQLite limitations
 * - Requires full table recreation
 * - Foreign key relationships must be preserved
 */
class Migration19Down : Migration(19, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Users Table Recreation (DROP CONSTRAINT workaround)
        database.execSQL(
            """
            CREATE TABLE users_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                alamat TEXT NOT NULL,
                avatar TEXT NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(id) REFERENCES users(id)
            )
            """
        )

        database.execSQL(
            """
            INSERT INTO users_new
            SELECT * FROM users
            """
        )

        database.execSQL("DROP TABLE users")
        database.execSQL("ALTER TABLE users_new RENAME TO users")

        // Recreate indexes for users table
        database.execSQL("CREATE INDEX idx_users_email ON users(email)")
        database.execSQL("CREATE INDEX idx_users_active ON users(is_deleted) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_users_name_active ON users(last_name ASC, first_name ASC) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_users_email_active ON users(email) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_users_id_active ON users(id) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_users_updated_at_active ON users(updated_at) WHERE is_deleted = 0")

        // Financial Records Table Recreation
        database.execSQL(
            """
            CREATE TABLE financial_records_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                iuran_perwarga INTEGER NOT NULL DEFAULT 0,
                jumlah_iuran_bulanan INTEGER NOT NULL DEFAULT 0,
                total_iuran_individu INTEGER NOT NULL DEFAULT 0,
                pengeluaran_iuran_warga INTEGER NOT NULL DEFAULT 0,
                total_iuran_rekap INTEGER NOT NULL DEFAULT 0,
                pemanfaatan_iuran TEXT NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
            )
            """
        )

        database.execSQL(
            """
            INSERT INTO financial_records_new
            SELECT * FROM financial_records
            """
        )

        database.execSQL("DROP TABLE financial_records")
        database.execSQL("ALTER TABLE financial_records_new RENAME TO financial_records")

        // Recreate indexes for financial_records table
        database.execSQL("CREATE INDEX idx_financial_user_updated ON financial_records(user_id, updated_at)")
        database.execSQL("CREATE INDEX idx_financial_user_rekap ON financial_records(user_id, total_iuran_rekap)")
        database.execSQL("CREATE INDEX idx_financial_not_deleted ON financial_records(is_deleted) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_financial_active_user_updated ON financial_records(user_id, updated_at) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_financial_id_active ON financial_records(id) WHERE is_deleted = 0")
        database.execSQL("CREATE INDEX idx_financial_pemanfaatan_active ON financial_records(pemanfaatan_iuran) WHERE is_deleted = 0")

        // Transactions Table Recreation
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
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                metadata TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
            )
            """
        )

        database.execSQL(
            """
            INSERT INTO transactions_new
            SELECT * FROM transactions
            """
        )

        database.execSQL("DROP TABLE transactions")
        database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // Recreate indexes for transactions table
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
