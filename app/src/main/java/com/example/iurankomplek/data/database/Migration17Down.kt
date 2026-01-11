package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 17 Down: Remove Composite Indexes for WebhookEventDao Query Optimization
 *
 * Purpose: Reverse Migration17 by removing all composite indexes
 *
 * Rollback Safety:
 * - Reversible migration - removes all indexes added in Migration17
 * - Non-destructive to data - only drops indexes
 * - Restores previous schema state
 *
 * Performance Impact:
 * - Queries will use single-column indexes instead of composite indexes
 * - Additional sorting required for ORDER BY clauses
 * - Slight performance degradation for sorted webhook event queries
 * - No data loss or corruption
 */
object Migration17Down : Migration(17, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop composite indexes for WebhookEventDao optimization

        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_event_type_created_desc")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_status_created_asc")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_transaction_created_desc")
        database.execSQL("DROP INDEX IF EXISTS idx_webhook_events_created_desc")
    }
}
