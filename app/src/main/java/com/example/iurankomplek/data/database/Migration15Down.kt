package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 15 Down: Revert CHECK Constraints Addition to Users Table
 *
 * Reverses Migration15 by removing additional CHECK constraints from users table.
 * This restores the database schema to Migration14 state.
 *
 * Rollback Strategy:
 * - Create new table without enhanced CHECK constraints (reverts to Migration14 state)
 * - Copy existing data (all data should be valid as it passed Migration15 constraints)
 * - Drop new table
 * - Rename old table to users
 * - Recreate indexes on old table
 *
 * This migration removes the following CHECK constraints:
 * - Email length > 0 constraint
 * - Email LIKE '%@%' constraint
 * - First name length > 0 constraint
 * - Last name length > 0 constraint
 * - Alamat length > 0 constraint
 *
 * Preserved CHECK constraints:
 * - Email length <= 255
 * - First name length <= 100
 * - Last name length <= 100
 * - Alamat length <= 500
 * - Avatar length <= 2048
 * - is_deleted IN (0, 1)
 */
val Migration15Down = object : Migration(15, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create users table with Migration14 state CHECK constraints
        database.execSQL(
            """
            CREATE TABLE users_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                email TEXT NOT NULL CHECK(length(email) <= 255),
                first_name TEXT NOT NULL CHECK(length(first_name) <= 100),
                last_name TEXT NOT NULL CHECK(length(last_name) <= 100),
                alamat TEXT NOT NULL CHECK(length(alamat) <= 500),
                avatar TEXT NOT NULL CHECK(length(avatar) <= 2048),
                is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1)),
                created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
            )
            """.trimIndent()
        )

        // Step 2: Copy data from current table to old table structure
        // Note: All existing data passed Migration15 constraints, so it's valid for this structure
        database.execSQL(
            """
            INSERT INTO users_new (
                id, email, first_name, last_name, alamat, avatar,
                is_deleted, created_at, updated_at
            )
            SELECT
                id, email, first_name, last_name, alamat, avatar,
                is_deleted, created_at, updated_at
            FROM users
            """.trimIndent()
        )

        // Step 3: Drop current table
        database.execSQL("DROP TABLE users")

        // Step 4: Rename old table to users
        database.execSQL("ALTER TABLE users_new RENAME TO users")

        // Step 5: Recreate indexes on table (Migration14 state)
        // Note: All indexes need to be recreated on the table

        // Index from Migration1 - email (unique)
        database.execSQL(
            """
            CREATE INDEX idx_users_email
            ON users(email)
            """
        )

        // Index from UserEntity - email (unique, explicit)
        database.execSQL(
            """
            CREATE UNIQUE INDEX index_users_email
            ON users(email)
            """
        )

        // Index from UserEntity - last_name, first_name (name sort)
        database.execSQL(
            """
            CREATE INDEX idx_users_name_sort
            ON users(last_name, first_name)
            """
        )

        // Partial index from Migration5 - is_deleted = 0
        database.execSQL(
            """
            CREATE INDEX idx_users_not_deleted
            ON users(is_deleted)
            WHERE is_deleted = 0
            """
        )

        // Partial indexes from Migration7 for soft-delete optimization
        database.execSQL(
            """
            CREATE INDEX idx_users_active
            ON users(is_deleted)
            WHERE is_deleted = 0
            """
        )

        database.execSQL(
            """
            CREATE INDEX idx_users_active_updated
            ON users(updated_at)
            WHERE is_deleted = 0
            """
        )

        // Partial index from Migration11 - email for active records
        database.execSQL(
            """
            CREATE UNIQUE INDEX idx_users_email_active
            ON users(email)
            WHERE is_deleted = 0
            """
        )

        // Partial index from Migration11 - name sort for active records
        database.execSQL(
            """
            CREATE INDEX idx_users_name_sort_active
            ON users(last_name, first_name)
            WHERE is_deleted = 0
            """
        )

        // Partial index from Migration11 - id for active records
        database.execSQL(
            """
            CREATE INDEX idx_users_id_active
            ON users(id)
            WHERE is_deleted = 0
            """
        )

        // Partial index from Migration11 - updated_at for active records
        database.execSQL(
            """
            CREATE INDEX idx_users_updated_at_active
            ON users(updated_at)
            WHERE is_deleted = 0
            """
        )
    }
}
