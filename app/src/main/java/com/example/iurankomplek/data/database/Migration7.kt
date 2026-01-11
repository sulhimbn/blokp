package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
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
