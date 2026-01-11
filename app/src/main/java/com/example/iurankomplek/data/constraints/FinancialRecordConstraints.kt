package com.example.iurankomplek.data.constraints

object FinancialRecordConstraints {
    const val TABLE_NAME = "financial_records"
    
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val IURAN_PERWARGA = "iuran_perwarga"
        const val JUMLAH_IURAN_BULANAN = "jumlah_iuran_bulanan"
        const val TOTAL_IURAN_INDIVIDU = "total_iuran_individu"
        const val PENGELUARAN_IURAN_WARGA = "pengeluaran_iuran_warga"
        const val TOTAL_IURAN_REKAP = "total_iuran_rekap"
        const val PEMANFAATAN_IURAN = "pemanfaatan_iuran"
        const val IS_DELETED = "is_deleted"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }
    
    object Constraints {
        const val MAX_PEMANFAATAN_LENGTH = 500
        const val MAX_NUMERIC_VALUE = 999999999
    }
    
    object Indexes {
        const val IDX_USER_ID = "idx_financial_user_id"
        const val IDX_UPDATED_AT = "idx_financial_updated_at"
        const val IDX_USER_REKAP = "idx_financial_user_rekap"
        const val IDX_ACTIVE_FINANCIAL_USER_UPDATED = "idx_financial_active_user_updated"
        const val IDX_ACTIVE_FINANCIAL = "idx_financial_active"
        const val IDX_ACTIVE_FINANCIAL_UPDATED = "idx_financial_active_updated"
    }
    
    val TABLE_SQL = """
        CREATE TABLE ${TABLE_NAME} (
            ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Columns.USER_ID} INTEGER NOT NULL,
            ${Columns.IURAN_PERWARGA} INTEGER NOT NULL DEFAULT 0,
            ${Columns.JUMLAH_IURAN_BULANAN} INTEGER NOT NULL DEFAULT 0,
            ${Columns.TOTAL_IURAN_INDIVIDU} INTEGER NOT NULL DEFAULT 0,
            ${Columns.PENGELUARAN_IURAN_WARGA} INTEGER NOT NULL DEFAULT 0,
            ${Columns.TOTAL_IURAN_REKAP} INTEGER NOT NULL DEFAULT 0,
            ${Columns.PEMANFAATAN_IURAN} TEXT NOT NULL,
            ${Columns.IS_DELETED} INTEGER NOT NULL DEFAULT 0,
            ${Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            ${Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            FOREIGN KEY(${Columns.USER_ID}) REFERENCES ${UserConstraints.TABLE_NAME}(${UserConstraints.Columns.ID})
                ON DELETE CASCADE
                ON UPDATE CASCADE
        )
    """.trimIndent()

    val INDEX_USER_ID_SQL = "CREATE INDEX ${Indexes.IDX_USER_ID} ON ${TABLE_NAME}(${Columns.USER_ID})"
    val INDEX_UPDATED_AT_SQL = "CREATE INDEX ${Indexes.IDX_UPDATED_AT} ON ${TABLE_NAME}(${Columns.UPDATED_AT} DESC)"
    val INDEX_USER_REKAP_SQL = "CREATE INDEX ${Indexes.IDX_USER_REKAP} ON ${TABLE_NAME}(${Columns.USER_ID}, ${Columns.TOTAL_IURAN_REKAP})"
}
