package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.iurankomplek.data.constraints.DatabaseConstraints

class Migration1 : Migration(0, 1) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(DatabaseConstraints.Users.TABLE_SQL)
        database.execSQL(DatabaseConstraints.Users.INDEX_EMAIL_SQL)
        database.execSQL(DatabaseConstraints.FinancialRecords.TABLE_SQL)
        database.execSQL(DatabaseConstraints.FinancialRecords.INDEX_USER_ID_SQL)
        database.execSQL(DatabaseConstraints.FinancialRecords.INDEX_UPDATED_AT_SQL)
    }
}
