package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 11 Down: Remove Partial Indexes
 *
 * Reverses Migration 11 by dropping all partial indexes.
 * Database returns to using original indexes that include deleted records.
 *
 * Note: This is a safe, reversible migration.
 * - Old indexes remain intact
 * - Only new partial indexes are dropped
 * - No data loss or modification
 */
val Migration11Down = object : Migration(11, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ===== USERS TABLE =====
        database.execSQL("DROP INDEX IF EXISTS idx_users_email_active")
        database.execSQL("DROP INDEX IF EXISTS idx_users_name_sort_active")
        database.execSQL("DROP INDEX IF EXISTS idx_users_id_active")
        database.execSQL("DROP INDEX IF EXISTS idx_users_updated_at_active")

        // ===== FINANCIAL RECORDS TABLE =====
        database.execSQL("DROP INDEX IF EXISTS idx_financial_user_updated_active")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_id_active")
        database.execSQL("DROP INDEX IF EXISTS idx_financial_pemanfaatan_active")

        // ===== TRANSACTIONS TABLE =====
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_user_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_status_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_user_status_active")
        database.execSQL("DROP INDEX IF EXISTS idx_transactions_created_at_active")

        // Note: Original indexes remain intact for deleted record queries
        // - Index(users, email) - unique
        // - Index(users, last_name, first_name)
        // - Index(financial_records, user_id, updated_at)
        // - Index(transactions, user_id)
        // - Index(transactions, status)
        // - Index(transactions, user_id, status)
        // - Index(transactions, created_at)
        // - Index(transactions, updated_at)
    }
}
