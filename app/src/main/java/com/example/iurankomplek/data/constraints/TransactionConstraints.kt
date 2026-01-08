package com.example.iurankomplek.data.constraints

object TransactionConstraints {
    const val TABLE_NAME = "transactions"

    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val AMOUNT = "amount"
        const val CURRENCY = "currency"
        const val STATUS = "status"
        const val PAYMENT_METHOD = "payment_method"
        const val DESCRIPTION = "description"
        const val IS_DELETED = "is_deleted"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
        const val METADATA = "metadata"
    }

    object Constraints {
        val MAX_AMOUNT = java.math.BigDecimal("999999999.99")
        const val MAX_CURRENCY_LENGTH = 3
        const val MAX_DESCRIPTION_LENGTH = 500
        const val MAX_METADATA_LENGTH = 2000
    }

    object Indexes {
        const val IDX_USER_ID = "idx_transactions_user_id"
        const val IDX_STATUS = "idx_transactions_status"
        const val IDX_USER_STATUS = "idx_transactions_user_status"
        const val IDX_STATUS_DELETED = "idx_transactions_status_deleted"
        const val IDX_CREATED_AT = "idx_transactions_created_at"
        const val IDX_UPDATED_AT = "idx_transactions_updated_at"
    }

    val TABLE_SQL = """
        CREATE TABLE ${TABLE_NAME} (
            ${Columns.ID} TEXT PRIMARY KEY NOT NULL CHECK(length(${Columns.ID}) > 0),
            ${Columns.USER_ID} INTEGER NOT NULL,
            ${Columns.AMOUNT} NUMERIC NOT NULL CHECK(${Columns.AMOUNT} > 0 AND ${Columns.AMOUNT} <= ${Constraints.MAX_AMOUNT}),
            ${Columns.CURRENCY} TEXT NOT NULL DEFAULT 'IDR' CHECK(length(${Columns.CURRENCY}) <= ${Constraints.MAX_CURRENCY_LENGTH}),
            ${Columns.STATUS} TEXT NOT NULL CHECK(${Columns.STATUS} IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')),
            ${Columns.PAYMENT_METHOD} TEXT NOT NULL CHECK(${Columns.PAYMENT_METHOD} IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT')),
            ${Columns.DESCRIPTION} TEXT NOT NULL CHECK(length(${Columns.DESCRIPTION}) > 0 AND length(${Columns.DESCRIPTION}) <= ${Constraints.MAX_DESCRIPTION_LENGTH}),
            ${Columns.IS_DELETED} INTEGER NOT NULL DEFAULT 0 CHECK(${Columns.IS_DELETED} IN (0, 1)),
            ${Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            ${Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            ${Columns.METADATA} TEXT NOT NULL DEFAULT '' CHECK(length(${Columns.METADATA}) <= ${Constraints.MAX_METADATA_LENGTH}),
            FOREIGN KEY(${Columns.USER_ID}) REFERENCES ${UserConstraints.TABLE_NAME}(${UserConstraints.Columns.ID})
                ON DELETE RESTRICT
                ON UPDATE CASCADE
        )
    """.trimIndent()

    val INDEX_USER_ID_SQL = "CREATE INDEX ${Indexes.IDX_USER_ID} ON ${TABLE_NAME}(${Columns.USER_ID})"
    val INDEX_STATUS_SQL = "CREATE INDEX ${Indexes.IDX_STATUS} ON ${TABLE_NAME}(${Columns.STATUS})"
    val INDEX_USER_STATUS_SQL = "CREATE INDEX ${Indexes.IDX_USER_STATUS} ON ${TABLE_NAME}(${Columns.USER_ID}, ${Columns.STATUS})"
    val INDEX_STATUS_DELETED_SQL = "CREATE INDEX ${Indexes.IDX_STATUS_DELETED} ON ${TABLE_NAME}(${Columns.STATUS}, ${Columns.IS_DELETED}) WHERE ${Columns.IS_DELETED} = 0"
    val INDEX_CREATED_AT_SQL = "CREATE INDEX ${Indexes.IDX_CREATED_AT} ON ${TABLE_NAME}(${Columns.CREATED_AT})"
    val INDEX_UPDATED_AT_SQL = "CREATE INDEX ${Indexes.IDX_UPDATED_AT} ON ${TABLE_NAME}(${Columns.UPDATED_AT})"
}
