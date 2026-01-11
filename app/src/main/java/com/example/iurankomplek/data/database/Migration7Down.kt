package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration7Down = object : Migration(7, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS idx_financial_active_updated")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_active")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_active_user_updated")
        db.execSQL("DROP INDEX IF EXISTS idx_users_active_updated")
        db.execSQL("DROP INDEX IF EXISTS idx_users_active")
    }
}
