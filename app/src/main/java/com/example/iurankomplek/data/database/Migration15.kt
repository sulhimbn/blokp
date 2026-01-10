package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 15: Add CHECK Constraints to Users Table
 *
 * Issue Identified:
 * - UserEntity has validation in init block (application-level)
 * - Email validation includes '@' symbol check (email.contains("@"))
 * - Text field validation includes non-empty checks (isNotBlank())
 * - But actual database schema lacks these CHECK constraints (no data integrity at DB level)
 * - This allows invalid data to be inserted if validation is bypassed
 *
 * Solution: Recreate users table with additional CHECK constraints for data integrity
 *
 * Data Integrity Improvements:
 * - Email length > 0: Prevents empty email strings
 * - Email LIKE '%@%': Enforces email format (must contain @ symbol)
 * - First name length > 0: Prevents empty first name strings
 * - Last name length > 0: Prevents empty last name strings
 * - Alamat length > 0: Prevents empty alamat strings
 * - All existing CHECK constraints preserved: length limits, is_deleted enum
 *
 * Database-Level Integrity:
 * - Ensures data validation matches application-level checks
 * - Prevents data corruption from direct database modifications
 * - Improves data consistency across application lifetime
 * - Supports data integrity audits
 *
 * Migration Strategy:
 * - Create new table with enhanced CHECK constraints
 * - Copy existing data (data will be validated against constraints)
 * - Drop old table
 * - Rename new table to users
 * - Recreate indexes on new table
 *
 * Note: If any existing data violates new CHECK constraints,
 * migration will fail. This is intentional to prevent propagating invalid data.
 * Existing valid data will be preserved.
 */
class Migration15 : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create new users table with enhanced CHECK constraints
        database.execSQL(
            """
            CREATE TABLE users_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                email TEXT NOT NULL CHECK(length(email) > 0 AND length(email) <= 255 AND email LIKE '%@%'),
                first_name TEXT NOT NULL CHECK(length(first_name) > 0 AND length(first_name) <= 100),
                last_name TEXT NOT NULL CHECK(length(last_name) > 0 AND length(last_name) <= 100),
                alamat TEXT NOT NULL CHECK(length(alamat) > 0 AND length(alamat) <= 500),
                avatar TEXT NOT NULL CHECK(length(avatar) <= 2048),
                is_deleted INTEGER NOT NULL DEFAULT 0 CHECK(is_deleted IN (0, 1)),
                created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
                updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
            )
            """.trimIndent()
        )

        // Step 2: Copy data from old table to new table
        // Note: Any data violating CHECK constraints will cause migration to fail
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

        // Step 3: Drop old table
        database.execSQL("DROP TABLE users")

        // Step 4: Rename new table to users
        database.execSQL("ALTER TABLE users_new RENAME TO users")

        // Step 5: Recreate indexes on new table
        // Note: All indexes need to be recreated on the new table

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
