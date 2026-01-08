package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder

val Migration1Down = object : Migration(1, 0) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val query = SupportSQLiteQueryBuilder.builder("sqlite_master")
            .selection("type = ? AND name IN (?, ?)", arrayOf("table", "users", "financial_records"))
            .create()
        
        val tables = mutableListOf<String>()
        db.query(query).use { cursor ->
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(1))
            }
        }
        
        for (table in tables) {
            val indexQuery = SupportSQLiteQueryBuilder.builder("sqlite_master")
                .selection("type = ? AND tbl_name = ?", arrayOf("index", table))
                .create()
            
            db.query(indexQuery).use { cursor ->
                while (cursor.moveToNext()) {
                    db.execSQL("DROP INDEX IF EXISTS ${cursor.getString(1)}")
                }
            }
            
            db.execSQL("DROP TABLE IF EXISTS $table")
        }
    }
}
