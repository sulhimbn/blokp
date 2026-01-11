package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX idx_webhook_events_transaction_id ON webhook_events(transaction_id)")
    }
}
