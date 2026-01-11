package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 18 Down: Remove Partial Indexes for Users Table
 *
 * Purpose: Rollback Migration18 by removing partial indexes from users table
 *
 * Indexes to Drop:
 * - idx_users_active: Partial index on is_deleted WHERE is_deleted = 0
 * - idx_users_name_active: Partial composite index on (last_name, first_name) WHERE is_deleted = 0
 * - idx_users_email_active: Partial index on email WHERE is_deleted = 0
 * - idx_users_id_active: Partial index on id WHERE is_deleted = 0
 * - idx_users_updated_at_active: Partial index on updated_at WHERE is_deleted = 0
 *
 * Rollback Safety:
 * - Dropped indexes were added in Migration18
 * - No data is modified or lost
 * - Original table indexes from UserEntity remain intact:
 *   - index_users_email (unique) on email
 *   - index_users_last_name_first_name on (last_name, first_name)
 */
object Migration18Down : Migration(18, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop partial indexes in reverse order of creation

        database.execSQL("DROP INDEX IF EXISTS idx_users_updated_at_active")
        database.execSQL("DROP INDEX IF EXISTS idx_users_id_active")
        database.execSQL("DROP INDEX IF EXISTS idx_users_email_active")
        database.execSQL("DROP INDEX IF EXISTS idx_users_name_active")
        database.execSQL("DROP INDEX IF EXISTS idx_users_active")
    }
}
