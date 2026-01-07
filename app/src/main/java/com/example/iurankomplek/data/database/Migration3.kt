package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Priority 1: Users table - Composite index for sorting by last_name, first_name
        // Eliminates filesort on getAllUsers() ORDER BY last_name ASC, first_name ASC
        // Estimated improvement: 10-100x faster for user lists with 1000+ users
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_users_name_sort 
            ON users(last_name ASC, first_name ASC)
        """)
        
        // Priority 1: FinancialRecords table - Composite index for user queries with ordering
        // Optimizes getFinancialRecordsByUserId() ORDER BY updated_at DESC
        // Estimated improvement: 2-10x faster for user financial record queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_financial_user_updated 
            ON financial_records(user_id, updated_at DESC)
        """)
        
        // Priority 2: WebhookEvents table - Composite index for retry queue processing
        // Optimizes retry queue queries: WHERE status = :status AND next_retry_at <= :now
        // Estimated improvement: 2-5x faster for webhook retry processing
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_webhook_retry_queue 
            ON webhook_events(status, next_retry_at)
        """)
    }
}
