package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS webhook_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idempotency_key TEXT NOT NULL,
                event_type TEXT NOT NULL,
                payload TEXT NOT NULL,
                transaction_id TEXT,
                status TEXT NOT NULL,
                retry_count INTEGER NOT NULL DEFAULT 0,
                max_retries INTEGER NOT NULL DEFAULT 5,
                next_retry_at INTEGER,
                delivered_at INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                last_error TEXT
            )
        """)
        
        db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS index_webhook_events_idempotency_key 
            ON webhook_events(idempotency_key)
        """)
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_webhook_events_status 
            ON webhook_events(status)
        """)
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_webhook_events_event_type 
            ON webhook_events(event_type)
        """)
    }
}
