package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration3Down = object : Migration(3, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop composite indexes created in Migration 3
        // This down migration preserves all data, only removes performance indexes

        // Drop users name sort index
        db.execSQL("DROP INDEX IF EXISTS idx_users_name_sort")

        // Drop financial records user+updated index
        db.execSQL("DROP INDEX IF EXISTS idx_financial_user_updated")

        // Drop webhook retry queue index
        db.execSQL("DROP INDEX IF EXISTS idx_webhook_retry_queue")
    }
}
