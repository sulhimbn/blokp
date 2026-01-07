package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration2Down = object : Migration(2, 1) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP INDEX IF EXISTS index_webhook_events_event_type")
        database.execSQL("DROP INDEX IF EXISTS index_webhook_events_status")
        database.execSQL("DROP INDEX IF EXISTS index_webhook_events_idempotency_key")
        database.execSQL("DROP TABLE IF EXISTS webhook_events")
    }
}
