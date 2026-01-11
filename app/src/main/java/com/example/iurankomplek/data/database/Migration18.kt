package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 18: Add Partial Indexes for Users Table
 *
 * Issue Identified:
 * - Most UserDao queries filter on is_deleted = 0 (soft-delete pattern)
 * - financial_records and transactions tables have partial indexes for is_deleted = 0 (Migration16)
 * - users table is missing partial indexes for is_deleted = 0
 * - Inconsistent optimization pattern across tables
 * - Users table queries scan all rows including deleted records
 *
 * Affected Queries (UserDao):
 * - getAllUsers(): WHERE is_deleted = 0 ORDER BY last_name ASC, first_name ASC
 * - getUserById(): WHERE id = :userId AND is_deleted = 0
 * - getUserByEmail(): WHERE email = :email AND is_deleted = 0
 * - getUserWithFinancialRecords(): WHERE id = :userId AND is_deleted = 0
 * - getAllUsersWithFinancialRecords(): WHERE is_deleted = 0
 * - emailExists(): SELECT EXISTS(...) WHERE email = :email AND is_deleted = 0
 * - getUsersByEmails(): WHERE email IN (:emails) AND is_deleted = 0
 * - getLatestUpdatedAt(): SELECT MAX(updated_at) FROM users WHERE is_deleted = 0
 *
 * Solution:
 * - Create partial indexes on is_deleted = 0 for users table
 * - Partial indexes only store active records, reducing index size
 * - Faster scans, less memory usage, better query performance
 * - Aligns with existing partial indexes pattern in financial_records and transactions tables
 *
 * Database Performance Benefits:
 * - Reduced index size: Only active records indexed (~80-90% reduction in typical soft-delete scenarios)
 * - Faster scans: Partial indexes skip deleted records
 * - Lower memory: Smaller index structures in RAM
 * - Better I/O: Fewer pages read from disk
 *
 * Migration Safety:
 * - Non-destructive: Only adds new indexes
 * - Backward compatible: No schema changes to tables
 * - Instant: Index creation doesn't block reads
 *
 * Partial Index Strategy:
 * - Single column partial index on is_deleted WHERE is_deleted = 0
 * - Covers all queries filtering on is_deleted = 0
 * - Composite index on (last_name, first_name) WHERE is_deleted = 0 for ORDER BY optimization
 */
class Migration18 : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Partial index: is_deleted = 0 for all UserDao queries
        database.execSQL(
            """
            CREATE INDEX idx_users_active
            ON users(is_deleted)
            WHERE is_deleted = 0
            """
        )

        // Partial composite index: (last_name ASC, first_name ASC) for active records
        // Covers getAllUsers() query: WHERE is_deleted = 0 ORDER BY last_name ASC, first_name ASC
        database.execSQL(
            """
            CREATE INDEX idx_users_name_active
            ON users(last_name ASC, first_name ASC)
            WHERE is_deleted = 0
            """
        )

        // Partial index: email for active records
        // Covers getUserByEmail(), emailExists(), getUsersByEmails()
        database.execSQL(
            """
            CREATE INDEX idx_users_email_active
            ON users(email)
            WHERE is_deleted = 0
            """
        )

        // Partial index: id for active records
        // Covers getUserById(), getUserWithFinancialRecords()
        database.execSQL(
            """
            CREATE INDEX idx_users_id_active
            ON users(id)
            WHERE is_deleted = 0
            """
        )

        // Partial index: updated_at for getLatestUpdatedAt()
        database.execSQL(
            """
            CREATE INDEX idx_users_updated_at_active
            ON users(updated_at)
            WHERE is_deleted = 0
            """
        )
    }
}
