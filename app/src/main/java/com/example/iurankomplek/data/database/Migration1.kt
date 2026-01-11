package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.iurankomplek.data.constraints.UserConstraints
import com.example.iurankomplek.data.constraints.FinancialRecordConstraints

class Migration1 : Migration(0, 1) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(UserConstraints.TABLE_SQL)
        db.execSQL(UserConstraints.INDEX_EMAIL_SQL)
        db.execSQL(FinancialRecordConstraints.TABLE_SQL)
        db.execSQL(FinancialRecordConstraints.INDEX_USER_ID_SQL)
        db.execSQL(FinancialRecordConstraints.INDEX_UPDATED_AT_SQL)
    }
}
