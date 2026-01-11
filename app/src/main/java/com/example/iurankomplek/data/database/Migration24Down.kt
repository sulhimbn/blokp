package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 24 Down: Remove Composite Indexes from Transactions Table
 *
 * Purpose: Rollback Migration24 by dropping the 3 composite indexes
 *
 * Indexes Dropped:
 * 1. idx_transactions_user_deleted
 * 2. idx_transactions_status_deleted
 * 3. idx_transactions_deleted_updated
 *
 * Retained Index:
 * - idx_transactions_user_id (original single-column index on user_id)
 *
 * Safety:
 * - Migration is reversible without data loss
 * - Only DDL changes (DROP INDEX), no data modification
 * - Original functionality preserved
 */
class Migration24Down : Migration(24, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_user_deleted")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_status_deleted")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_deleted_updated")
    }
}
