package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 16: Add Partial Indexes for Soft-Delete Optimization
 *
 * Issue Identified:
 * - Most queries filter on is_deleted = 0 (soft-delete pattern)
 * - No partial indexes exist for financial_records and transactions tables
 * - Full indexes scan all rows including deleted records
 * - Increased memory usage and slower query performance
 *
 * Affected Queries (FinancialRecordDao):
 * - getAllFinancialRecords(): WHERE is_deleted = 0
 * - getFinancialRecordById(): WHERE id = :recordId AND is_deleted = 0
 * - getFinancialRecordsByUserId(): WHERE user_id = :userId AND is_deleted = 0
 * - getLatestFinancialRecordByUserId(): WHERE user_id = :userId AND is_deleted = 0
 * - searchFinancialRecordsByPemanfaatan(): WHERE ... AND is_deleted = 0
 * - getCount(): WHERE is_deleted = 0
 * - getCountByUserId(): WHERE user_id = :userId AND is_deleted = 0
 * - getTotalRekapByUserId(): WHERE user_id = :userId AND is_deleted = 0
 * - getFinancialRecordsUpdatedSince(): WHERE ... AND is_deleted = 0
 * - getFinancialRecordsByUserIds(): WHERE user_id IN (...) AND is_deleted = 0
 *
 * Affected Queries (TransactionDao):
 * - getAllTransactions(): WHERE is_deleted = 0
 * - getTransactionById(): WHERE id = :id AND is_deleted = 0
 * - getTransactionsByUserId(): WHERE user_id = :userId AND is_deleted = 0
 * - getTransactionsByStatus(): WHERE status = :status AND is_deleted = 0
 *
 * Solution:
 * - Create partial indexes on is_deleted = 0 for all tables
 * - Partial indexes only store active records, reducing index size
 * - Faster scans, less memory usage, better query performance
 * - Aligns with existing partial indexes pattern in users table (Migration7, Migration11)
 *
 * Database Performance Benefits:
 * - Reduced index size: Only active records indexed (~80-90% reduction in typical soft-delete scenarios)
 * - Faster scans: Partial indexes skip deleted records
 * - Lower memory: Smaller index structures in RAM
 * - Better I/O: Fewer pages read from disk
 *
 * Migration Safety:
 * - Non-destructive: Only adds new indexes
 * - Backward compatible: No schema changes to tables
 * - Instant: Index creation doesn't block reads
 *
 * Partial Index Strategy:
 * - Single column partial index on is_deleted WHERE is_deleted = 0
 * - Covers all queries filtering on is_deleted = 0
 * - Works with existing composite indexes as additional optimization
 */
class Migration16 : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Partial indexes for financial_records table

        // Index: is_deleted = 0 for getAllFinancialRecords()
        database.execSQL(
            """
            CREATE INDEX idx_financial_records_active
            ON financial_records(is_deleted)
            WHERE is_deleted = 0
            """
        )

        // Partial index: updated_at DESC for active records (getAllFinancialRecords ORDER BY)
        database.execSQL(
            """
            CREATE INDEX idx_financial_records_active_updated_desc
            ON financial_records(updated_at DESC)
            WHERE is_deleted = 0
            """
        )

        // Partial index: user_id for active records (getFinancialRecordsByUserId)
        database.execSQL(
            """
            CREATE INDEX idx_financial_records_user_id_active
            ON financial_records(user_id)
            WHERE is_deleted = 0
            """
        )

        // Partial index: (user_id, updated_at DESC) for active records
        // Covers getFinancialRecordsByUserId() with ORDER BY updated_at DESC
        database.execSQL(
            """
            CREATE INDEX idx_financial_records_user_updated_active
            ON financial_records(user_id, updated_at DESC)
            WHERE is_deleted = 0
            """
        )

        // Partial index: id for active records (getFinancialRecordById)
        database.execSQL(
            """
            CREATE INDEX idx_financial_records_id_active
            ON financial_records(id)
            WHERE is_deleted = 0
            """
        )

        // Partial indexes for transactions table

        // Index: is_deleted = 0 for getAllTransactions()
        database.execSQL(
            """
            CREATE INDEX idx_transactions_active
            ON transactions(is_deleted)
            WHERE is_deleted = 0
            """
        )

        // Partial index: user_id for active records (getTransactionsByUserId)
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_id_active
            ON transactions(user_id)
            WHERE is_deleted = 0
            """
        )

        // Partial index: status for active records (getTransactionsByStatus)
        database.execSQL(
            """
            CREATE INDEX idx_transactions_status_active
            ON transactions(status)
            WHERE is_deleted = 0
            """
        )

        // Partial index: (user_id, status) for active records
        // Covers queries filtering on both user_id and status with is_deleted = 0
        database.execSQL(
            """
            CREATE INDEX idx_transactions_user_status_active
            ON transactions(user_id, status)
            WHERE is_deleted = 0
            """
        )

        // Partial index: id for active records (getTransactionById)
        database.execSQL(
            """
            CREATE INDEX idx_transactions_id_active
            ON transactions(id)
            WHERE is_deleted = 0
            """
        )

        // Partial index: created_at DESC for active records
        database.execSQL(
            """
            CREATE INDEX idx_transactions_created_at_active
            ON transactions(created_at DESC)
            WHERE is_deleted = 0
            """
        )

        // Partial index: updated_at DESC for active records
        database.execSQL(
            """
            CREATE INDEX idx_transactions_updated_at_active
            ON transactions(updated_at DESC)
            WHERE is_deleted = 0
            """
        )
    }
}
