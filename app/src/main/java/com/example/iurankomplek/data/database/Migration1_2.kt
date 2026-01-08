package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.iurankomplek.data.constraints.DatabaseConstraints

class Migration1_2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.apply {
            execSQL("PRAGMA foreign_keys = OFF")

            execSQL("ALTER TABLE transactions RENAME TO transactions_old")

            execSQL(DatabaseConstraints.Transactions.TABLE_SQL)

            execSQL("INSERT INTO transactions (" +
                    DatabaseConstraints.Transactions.Columns.ID + ", " +
                    DatabaseConstraints.Transactions.Columns.USER_ID + ", " +
                    DatabaseConstraints.Transactions.Columns.AMOUNT + ", " +
                    DatabaseConstraints.Transactions.Columns.CURRENCY + ", " +
                    DatabaseConstraints.Transactions.Columns.STATUS + ", " +
                    DatabaseConstraints.Transactions.Columns.PAYMENT_METHOD + ", " +
                    DatabaseConstraints.Transactions.Columns.DESCRIPTION + ", " +
                    DatabaseConstraints.Transactions.Columns.CREATED_AT + ", " +
                    DatabaseConstraints.Transactions.Columns.UPDATED_AT + ", " +
                    DatabaseConstraints.Transactions.Columns.METADATA +
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

            execSQL(DatabaseConstraints.Transactions.INDEX_USER_ID_SQL)
            execSQL(DatabaseConstraints.Transactions.INDEX_STATUS_SQL)
            execSQL(DatabaseConstraints.Transactions.INDEX_USER_STATUS_SQL)
            execSQL(DatabaseConstraints.Transactions.INDEX_CREATED_AT_SQL)
            execSQL(DatabaseConstraints.Transactions.INDEX_UPDATED_AT_SQL)

            execSQL("DROP TABLE transactions_old")

            execSQL("PRAGMA foreign_keys = ON")
        }
    }
}
