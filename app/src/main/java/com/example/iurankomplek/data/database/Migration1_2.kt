package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.iurankomplek.data.constraints.TransactionConstraints

class Migration1_2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.apply {
            execSQL("PRAGMA foreign_keys = OFF")

            execSQL("ALTER TABLE transactions RENAME TO transactions_old")

            execSQL(TransactionConstraints.TABLE_SQL)

            execSQL("INSERT INTO transactions (" +
                    TransactionConstraints.Columns.ID + ", " +
                    TransactionConstraints.Columns.USER_ID + ", " +
                    TransactionConstraints.Columns.AMOUNT + ", " +
                    TransactionConstraints.Columns.CURRENCY + ", " +
                    TransactionConstraints.Columns.STATUS + ", " +
                    TransactionConstraints.Columns.PAYMENT_METHOD + ", " +
                    TransactionConstraints.Columns.DESCRIPTION + ", " +
                    TransactionConstraints.Columns.CREATED_AT + ", " +
                    TransactionConstraints.Columns.UPDATED_AT + ", " +
                    TransactionConstraints.Columns.METADATA +
                    ") SELECT " +
                    "id, " +
                    "CAST(userId AS INTEGER), " +
                    "amount, " +
                    "currency, " +
                    "status, " +
                    "payment_method, " +
                    "description, " +
                    "created_at, " +
                    "updated_at, " +
                    "COALESCE(metadata_json, '') FROM transactions_old")

            execSQL(TransactionConstraints.INDEX_USER_ID_SQL)
            execSQL(TransactionConstraints.INDEX_STATUS_SQL)
            execSQL(TransactionConstraints.INDEX_USER_STATUS_SQL)
            execSQL(TransactionConstraints.INDEX_CREATED_AT_SQL)
            execSQL(TransactionConstraints.INDEX_UPDATED_AT_SQL)

            execSQL("DROP TABLE transactions_old")

            execSQL("PRAGMA foreign_keys = ON")
        }
    }
}
