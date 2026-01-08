package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration2Down = object : Migration(2, 1) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS index_webhook_events_event_type")
        db.execSQL("DROP INDEX IF EXISTS index_webhook_events_status")
        db.execSQL("DROP INDEX IF EXISTS index_webhook_events_idempotency_key")
        db.execSQL("DROP TABLE IF EXISTS webhook_events")
    }
}
