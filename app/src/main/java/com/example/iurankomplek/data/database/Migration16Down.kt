package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 16 Down: Remove Partial Indexes for Soft-Delete Optimization
 *
 * Purpose: Reverse Migration16 by removing all partial indexes
 *
 * Rollback Safety:
 * - Reversible migration - removes all indexes added in Migration16
 * - Non-destructive to data - only drops indexes
 * - Restores previous schema state
 *
 * Performance Impact:
 * - Queries will use full indexes instead of partial indexes
 * - Slight performance degradation for soft-delete filtered queries
 * - No data loss or corruption
 */
object Migration16Down : Migration(16, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop partial indexes for financial_records table

        database.execSQL("DROP INDEX IF EXISTS idx_financial_records_active")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_records_active_updated_desc")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_records_user_id_active")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_records_user_updated_active")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_records_id_active")

        // Drop partial indexes for transactions table

        database.execSQL("DROP INDEX IF EXISTS idx_transactions_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_user_id_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_status_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_user_status_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_id_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_created_at_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_updated_at_active")
    }
}
