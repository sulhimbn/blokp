package com.example.iurankomplek.data.constraints

object DatabaseConstraints {
    
    object Users {
        const val TABLE_NAME = "users"
        
        object Columns {
            const val ID = "id"
            const val EMAIL = "email"
            const val FIRST_NAME = "first_name"
            const val LAST_NAME = "last_name"
            const val ALAMAT = "alamat"
            const val AVATAR = "avatar"
            const val CREATED_AT = "created_at"
            const val UPDATED_AT = "updated_at"
        }
        
        object Constraints {
            const val MAX_EMAIL_LENGTH = 255
            const val MAX_NAME_LENGTH = 100
            const val MAX_ALAMAT_LENGTH = 500
            const val MAX_AVATAR_LENGTH = 2048
        }
        
        object Indexes {
            const val IDX_EMAIL = "idx_users_email"
        }
        
        val TABLE_SQL = """
            CREATE TABLE ${TABLE_NAME} (
                ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Columns.EMAIL} TEXT NOT NULL UNIQUE CHECK(length(${Columns.EMAIL}) <= ${Constraints.MAX_EMAIL_LENGTH}),
                ${Columns.FIRST_NAME} TEXT NOT NULL CHECK(length(${Columns.FIRST_NAME}) <= ${Constraints.MAX_NAME_LENGTH}),
                ${Columns.LAST_NAME} TEXT NOT NULL CHECK(length(${Columns.LAST_NAME}) <= ${Constraints.MAX_NAME_LENGTH}),
                ${Columns.ALAMAT} TEXT NOT NULL CHECK(length(${Columns.ALAMAT}) <= ${Constraints.MAX_ALAMAT_LENGTH}),
                ${Columns.AVATAR} TEXT NOT NULL CHECK(length(${Columns.AVATAR}) <= ${Constraints.MAX_AVATAR_LENGTH}),
                ${Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                ${Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
            )
        """.trimIndent()
        
        val INDEX_EMAIL_SQL = "CREATE INDEX ${Indexes.IDX_EMAIL} ON ${TABLE_NAME}(${Columns.EMAIL})"
    }
    
    object FinancialRecords {
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
        }
        
        val TABLE_SQL = """
            CREATE TABLE ${TABLE_NAME} (
                ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Columns.USER_ID} INTEGER NOT NULL,
                ${Columns.IURAN_PERWARGA} INTEGER NOT NULL DEFAULT 0 CHECK(${Columns.IURAN_PERWARGA} >= 0),
                ${Columns.JUMLAH_IURAN_BULANAN} INTEGER NOT NULL DEFAULT 0 CHECK(${Columns.JUMLAH_IURAN_BULANAN} >= 0),
                ${Columns.TOTAL_IURAN_INDIVIDU} INTEGER NOT NULL DEFAULT 0 CHECK(${Columns.TOTAL_IURAN_INDIVIDU} >= 0),
                ${Columns.PENGELUARAN_IURAN_WARGA} INTEGER NOT NULL DEFAULT 0 CHECK(${Columns.PENGELUARAN_IURAN_WARGA} >= 0),
                ${Columns.TOTAL_IURAN_REKAP} INTEGER NOT NULL DEFAULT 0 CHECK(${Columns.TOTAL_IURAN_REKAP} >= 0),
                ${Columns.PEMANFAATAN_IURAN} TEXT NOT NULL CHECK(length(${Columns.PEMANFAATAN_IURAN}) > 0),
                ${Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                ${Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY(${Columns.USER_ID}) REFERENCES ${Users.TABLE_NAME}(${Users.Columns.ID}) 
                    ON DELETE CASCADE 
                    ON UPDATE CASCADE
            )
        """.trimIndent()
        
        val INDEX_USER_ID_SQL = "CREATE INDEX ${Indexes.IDX_USER_ID} ON ${TABLE_NAME}(${Columns.USER_ID})"
        val INDEX_UPDATED_AT_SQL = "CREATE INDEX ${Indexes.IDX_UPDATED_AT} ON ${TABLE_NAME}(${Columns.UPDATED_AT} DESC)"
    }
    
    object ValidationRules {
        const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        
        object Numeric {
            const val MIN_VALUE = 0
            const val MAX_VALUE = 999999999
        }
        
        object Text {
            const val MIN_LENGTH = 1
        }
    }
}
