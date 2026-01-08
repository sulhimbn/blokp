package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop duplicate full indexes that were created by entity annotations
        // These indexes conflicted with partial index names from Migration7
        
        // Drop users table duplicate indexes
        db.execSQL("DROP INDEX IF EXISTS idx_users_active")
        db.execSQL("DROP INDEX IF EXISTS idx_users_active_updated")
        
        // Drop financial_records table duplicate indexes
        db.execSQL("DROP INDEX IF EXISTS idx_financial_records_user_id_updated_at")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_records_updated_at")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_active_user_updated")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_records_id")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_records_updated_at_2")
        
        // Recreate partial indexes from Migration7 (now they won't conflict)
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_users_active 
            ON users(id) 
            WHERE is_deleted = 0
            """
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_users_active_updated 
            ON users(id, updated_at) 
            WHERE is_deleted = 0
            """
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_financial_active_user_updated 
            ON financial_records(user_id, updated_at) 
            WHERE is_deleted = 0
            """
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_financial_active 
            ON financial_records(id) 
            WHERE is_deleted = 0
            """
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_financial_active_updated 
            ON financial_records(updated_at) 
            WHERE is_deleted = 0
            """
        )
    }
}
