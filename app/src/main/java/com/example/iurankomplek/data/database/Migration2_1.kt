package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.iurankomplek.data.constraints.TransactionConstraints

class Migration2_1 : Migration(2, 1) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.apply {
            execSQL("PRAGMA foreign_keys = OFF")

            execSQL("ALTER TABLE transactions RENAME TO transactions_new")

            execSQL("CREATE TABLE transactions (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "userId TEXT NOT NULL, " +
                    "amount NUMERIC NOT NULL, " +
                    "currency TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "payment_method TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "created_at INTEGER NOT NULL, " +
                    "updated_at INTEGER NOT NULL, " +
                    "metadata_json TEXT)")

            execSQL("INSERT INTO transactions (" +
                    "id, userId, amount, currency, status, payment_method, description, created_at, updated_at, metadata_json" +
                    ") SELECT " +
                    TransactionConstraints.Columns.ID + ", " +
                    "CAST(" + TransactionConstraints.Columns.USER_ID + " AS TEXT), " +
                    TransactionConstraints.Columns.AMOUNT + ", " +
                    TransactionConstraints.Columns.CURRENCY + ", " +
                    TransactionConstraints.Columns.STATUS + ", " +
                    TransactionConstraints.Columns.PAYMENT_METHOD + ", " +
                    TransactionConstraints.Columns.DESCRIPTION + ", " +
                    TransactionConstraints.Columns.CREATED_AT + ", " +
                    TransactionConstraints.Columns.UPDATED_AT + ", " +
                    TransactionConstraints.Columns.METADATA +
                    " FROM transactions_new")

            execSQL("DROP TABLE transactions_new")

            execSQL("PRAGMA foreign_keys = ON")
        }
    }
}
