package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration8Down = object : Migration(8, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop partial indexes
        db.execSQL("DROP INDEX IF EXISTS idx_users_active")
        db.execSQL("DROP INDEX IF EXISTS idx_users_active_updated")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_active_user_updated")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_active")
        db.execSQL("DROP INDEX IF EXISTS idx_financial_active_updated")
        
        // Recreate full indexes (old entity annotation style)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_active ON users(id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_active_updated ON users(id, updated_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_records_user_id_updated_at ON financial_records(user_id, updated_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_records_updated_at ON financial_records(updated_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_records_id ON financial_records(id)")
    }
}
