package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration5Down = object : Migration(5, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS idx_transactions_not_deleted")
        
        db.execSQL("ALTER TABLE transactions DROP COLUMN is_deleted")
        
        db.execSQL("DROP INDEX IF EXISTS idx_financial_not_deleted")
        
        db.execSQL("ALTER TABLE financial_records DROP COLUMN is_deleted")
        
        db.execSQL("DROP INDEX IF EXISTS idx_users_not_deleted")
        
        db.execSQL("ALTER TABLE users DROP COLUMN is_deleted")
    }
}
