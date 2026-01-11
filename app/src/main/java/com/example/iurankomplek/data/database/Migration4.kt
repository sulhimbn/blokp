package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_financial_user_rekap
            ON financial_records(user_id, total_iuran_rekap)
        """)
    }
}
