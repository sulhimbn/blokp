package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 25 Down: Remove Composite Indexes from User and FinancialRecord Tables
 *
 * Purpose: Rollback Migration25 by dropping new indexes and recreating removed index
 *
 * Indexes Dropped from users Table:
 * 1. idx_users_deleted_last_name_first_name
 * 2. idx_users_deleted_updated_at
 *
 * Indexes Dropped from financial_records Table:
 * 3. idx_financial_records_user_deleted_updated_at
 * 4. idx_financial_records_deleted_updated_at
 *
 * Indexes Recreated (for rollback):
 * - idx_financial_records_user_total: Original non-optimal index
 *    - Recreated: CREATE INDEX idx_financial_records_user_total ON financial_records(user_id, total_iuran_rekap)
 *
 * Retained Indexes:
 * - idx_users_email (unique index on email)
 *
 * Safety:
 * - Migration is reversible without data loss
 * - Only DDL changes (DROP/CREATE INDEX), no data modification
 * - Original functionality preserved
 */
class Migration25Down : Migration(25, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP INDEX IF EXISTS idx_users_deleted_last_name_first_name")
        database.execSQL("DROP INDEX IF EXISTS idx_users_deleted_updated_at")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_records_user_deleted_updated_at")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_records_deleted_updated_at")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_records_user_total ON financial_records(user_id, total_iuran_rekap)")
    }
}
