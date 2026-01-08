package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.iurankomplek.data.constraints.DatabaseConstraints

class Migration1 : Migration(0, 1) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(DatabaseConstraints.Users.TABLE_SQL)
        db.execSQL(DatabaseConstraints.Users.INDEX_EMAIL_SQL)
        db.execSQL(DatabaseConstraints.FinancialRecords.TABLE_SQL)
        db.execSQL(DatabaseConstraints.FinancialRecords.INDEX_USER_ID_SQL)
        db.execSQL(DatabaseConstraints.FinancialRecords.INDEX_UPDATED_AT_SQL)
    }
}
