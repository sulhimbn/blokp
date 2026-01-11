package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 12 Down: Remove Composite Indexes
 *
 * Reverses Migration 12 by dropping all new composite indexes.
 * Database returns to previous index configuration.
 *
 * Note: This is a safe, reversible migration.
 * - All indexes from Migration 11 remain intact
 * - Only new indexes from Migration 12 are dropped
 * - No data loss or modification
 * - Query performance will revert to pre-Migration 12 levels
 */
val Migration12Down = object : Migration(12, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ===== FINANCIAL RECORDS TABLE =====
        database.execSQL("DROP INDEX IF EXISTS idx_financial_updated_desc_active")

        // ===== WEBHOOK EVENTS TABLE =====
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_status_created")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_transaction_created")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_type_created")

        // Note: All indexes from Migration 11 remain intact
        // - Partial indexes for soft delete optimization preserved
        // - WebhookEvent original indexes preserved
    }
}
