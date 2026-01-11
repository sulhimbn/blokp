package com.example.iurankomplek.data.constraints

object UserConstraints {
    const val TABLE_NAME = "users"
    
    object Columns {
        const val ID = "id"
        const val EMAIL = "email"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val ALAMAT = "alamat"
        const val AVATAR = "avatar"
        const val IS_DELETED = "is_deleted"
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
        const val IDX_ACTIVE_USERS = "idx_users_active"
        const val IDX_ACTIVE_USERS_UPDATED = "idx_users_active_updated"
    }
    
    val TABLE_SQL = """
        CREATE TABLE ${TABLE_NAME} (
            ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Columns.EMAIL} TEXT NOT NULL UNIQUE,
            ${Columns.FIRST_NAME} TEXT NOT NULL,
            ${Columns.LAST_NAME} TEXT NOT NULL,
            ${Columns.ALAMAT} TEXT NOT NULL,
            ${Columns.AVATAR} TEXT NOT NULL,
            ${Columns.IS_DELETED} INTEGER NOT NULL DEFAULT 0,
            ${Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            ${Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
        )
    """.trimIndent()

    val INDEX_EMAIL_SQL = "CREATE INDEX ${Indexes.IDX_EMAIL} ON ${TABLE_NAME}(${Columns.EMAIL})"
}
