package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration9Down = object : Migration(9, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop partial indexes created in Migration9
        db.execSQL("DROP INDEX IF EXISTS idx_transactions_user_id")
        db.execSQL("DROP INDEX IF EXISTS idx_transactions_status")
        db.execSQL("DROP INDEX IF EXISTS idx_transactions_user_status")
        db.execSQL("DROP INDEX IF EXISTS idx_transactions_created")
        db.execSQL("DROP INDEX IF EXISTS idx_transactions_updated")
        
        // Recreate full indexes (original entity annotation style)
        // Restores database to version 8 state for rollback purposes
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_user_id ON transactions(user_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_status ON transactions(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_user_id_status ON transactions(user_id, status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_created_at ON transactions(created_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_updated_at ON transactions(updated_at)")
    }
}
