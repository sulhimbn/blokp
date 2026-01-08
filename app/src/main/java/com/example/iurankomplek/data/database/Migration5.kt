package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE users ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
        
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_not_deleted ON users(is_deleted) WHERE is_deleted = 0")
        
        db.execSQL("ALTER TABLE financial_records ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
        
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_not_deleted ON financial_records(is_deleted) WHERE is_deleted = 0")
        
        db.execSQL("ALTER TABLE transactions ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
        
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_not_deleted ON transactions(is_deleted) WHERE is_deleted = 0")
    }
}
