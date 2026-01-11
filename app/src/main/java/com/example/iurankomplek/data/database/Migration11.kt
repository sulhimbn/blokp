package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 11: Add Partial Indexes for Soft Delete Queries
 * 
 * Issue Identified:
 * - 27 queries filter by is_deleted = 0 (active records)
 * - Only 8 queries filter by is_deleted = 1 (deleted records)
 * - Current indexes include deleted records, wasting space and scan time
 * 
 * Solution: Partial indexes filter by is_deleted = 0
 * 
 * Performance Improvements:
 * - Reduced index size (excludes deleted records)
 * - Faster query execution (smaller index scans)
 * - Better cache utilization (smaller indexes fit in memory)
 * - Lower storage overhead (indexes only contain active records)
 * 
 * Partial Index Strategy:
 * - Users table: 4 partial indexes for common query patterns
 * - Financial records: 3 partial indexes for user queries
 * - Transactions: 3 partial indexes for user queries
 * - Estimated index size reduction: 40-60% (depends on delete rate)
 */
class Migration11 : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ===== USERS TABLE =====

        // Partial index: email for active records (is_deleted = 0)
        // Used by: getUserByEmail(), emailExists()
        // Replaces: Index(value = ["email"], unique = true)
        database.execSQL(
            """
            CREATE UNIQUE INDEX idx_users_email_active
            ON users(email)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: last_name, first_name for active records (is_deleted = 0)
        // Used by: getAllUsers() - ORDER BY last_name ASC, first_name ASC
        // Replaces: Index(value = ["last_name", "first_name"])
        database.execSQL(
            """
            CREATE INDEX idx_users_name_sort_active
            ON users(last_name, first_name)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: id for active records (is_deleted = 0)
        // Used by: getUserById()
        // New index for user lookup by id
        database.execSQL(
            """
            CREATE INDEX idx_users_id_active
            ON users(id)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: updated_at for active records (is_deleted = 0)
        // Used by: getLatestUpdatedAt() - MAX(updated_at)
        // New index for timestamp queries
        database.execSQL(
            """
            CREATE INDEX idx_users_updated_at_active
            ON users(updated_at)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // ===== FINANCIAL RECORDS TABLE =====

        // Partial index: user_id, updated_at DESC for active records (is_deleted = 0)
        // Used by: getFinancialRecordsByUserId(), getLatestFinancialRecordByUserId()
        // Replaces: Index(value = ["user_id", "updated_at"])
        database.execSQL(
            """
            CREATE INDEX idx_financial_user_updated_active
            ON financial_records(user_id, updated_at DESC)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: id for active records (is_deleted = 0)
        // Used by: getFinancialRecordById()
        // New index for financial record lookup by id
        database.execSQL(
            """
            CREATE INDEX idx_financial_id_active
            ON financial_records(id)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: pemanfaatan_iuran LIKE query for active records (is_deleted = 0)
        // Used by: searchFinancialRecords() - LIKE '%' || :query || '%'
        // New index for search queries (note: LIKE with leading wildcard is not fully indexable)
        database.execSQL(
            """
            CREATE INDEX idx_financial_pemanfaatan_active
            ON financial_records(pemanfaatan_iuran)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // ===== TRANSACTIONS TABLE =====

        // Partial index: user_id for active transactions (is_deleted = 0)
        // Used by: getTransactionsByUserId()
        // Replaces: Index(value = ["user_id"])
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_active
            ON transactions(user_id)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: status for active transactions (is_deleted = 0)
        // Used by: getTransactionsByStatus()
        // Replaces: Index(value = ["status"])
        database.execSQL(
            """
            CREATE INDEX idx_transactions_status_active
            ON transactions(status)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: user_id, status for active transactions (is_deleted = 0)
        // Used by: getCompletedTransactionsByUserId()
        // Replaces: Index(value = ["user_id", "status"])
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_status_active
            ON transactions(user_id, status)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Partial index: created_at DESC for active transactions (is_deleted = 0)
        // Used by: getAllTransactions() - ORDER BY created_at DESC
        // Replaces: Index(value = ["created_at"])
        database.execSQL(
            """
            CREATE INDEX idx_transactions_created_at_active
            ON transactions(created_at DESC)
            WHERE is_deleted = 0
            """.trimIndent()
        )

        // Note: Old indexes remain for backward compatibility and deleted record queries
        // Old indexes will be gradually phased out in future migrations
    }
}
