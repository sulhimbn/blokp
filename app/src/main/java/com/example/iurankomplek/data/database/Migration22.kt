package com.example.iurankomplek.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 22: Remove Redundant Full Indexes (Cleanup)
 *
 * Issue Identified (DATA-001):
 * - Room entity @Index annotations create full indexes (include all records)
 * - Migrations 16, 18, 20, 21 added partial indexes (WHERE is_deleted = 0)
 * - Both types of indexes exist on same columns
 * - Doubles storage, slows writes, increases maintenance overhead
 *
 * Root Cause:
 * - Migration 16 added partial indexes for financial_records and transactions
 * - Migration 18 added partial indexes for users
 * - Migration 21 added partial indexes for webhook_events
 * - Room entity @Index annotations still create full indexes
 * - Result: Redundant indexes consuming storage and CPU
 *
 * Affected Tables:
 *
 * Users Table:
 * - Full index: index_users_last_name_first_name (from UserEntity @Index)
 * - Partial index: idx_users_name_active (Migration18, covers same query)
 * - Query: getAllUsers() - WHERE is_deleted = 0 ORDER BY last_name, first_name
 * - Partial index is superior (only active records, smaller index)
 *
 * Financial Records Table:
 * - Full index: index_financial_records_user_id_updated_at (from FinancialRecordEntity @Index)
 * - Partial index: idx_financial_records_user_updated_active (Migration16, same columns, partial)
 * - Query: getFinancialRecordsByUserId() - WHERE user_id = :userId AND is_deleted = 0 ORDER BY updated_at DESC
 * - Partial index is superior (only active records, includes ORDER BY column)
 *
 * - Full index: index_financial_records_user_id_total_iuran_rekap (from FinancialRecordEntity @Index)
 * - No corresponding partial index exists for total_iuran_rekap
 * - This index is NOT redundant (needed for aggregation queries)
 * - Decision: KEEP this index (unique use case)
 *
 * Transactions Table:
 * - Full index: index_transactions_user_id (from Transaction @Index)
 * - Partial index: idx_transactions_user_id_active (Migration16, same column, partial)
 * - Query: getTransactionsByUserId() - WHERE user_id = :userId AND is_deleted = 0
 * - Partial index is superior (only active records)
 *
 * - Full index: index_transactions_status (from Transaction @Index)
 * - Partial index: idx_transactions_status_active (Migration16, same column, partial)
 * - Query: getTransactionsByStatus() - WHERE status = :status AND is_deleted = 0
 * - Partial index is superior (only active records)
 *
 * - Full index: index_transactions_user_id_status (from Transaction @Index)
 * - Partial index: idx_transactions_user_status_active (Migration16, same columns, partial)
 * - Query: Same pattern as individual queries
 * - Partial index is superior (only active records)
 *
 * - Full index: index_transactions_created_at (from Transaction @Index)
 * - Partial index: idx_transactions_created_at_active (Migration16, same column, partial)
 * - Partial index is superior (only active records)
 *
 * - Full index: index_transactions_updated_at (from Transaction @Index)
 * - Partial index: idx_transactions_updated_at_active (Migration16, same column, partial)
 * - Partial index is superior (only active records)
 *
 * Webhook Events Table:
 * - All indexes are partial (Migration21)
 * - No redundant full indexes exist
 * - No action needed
 *
 * Impact of Redundant Indexes:
 * - Storage: ~2x index size (full + partial for same columns)
 * - Write Performance: 2x index maintenance (every INSERT/UPDATE writes to both indexes)
 * - Disk I/O: More pages to read/write
 * - Memory: Larger index structures in RAM cache
 * - Vacuum Time: Longer cleanup operations
 *
 * Database Performance Benefits After Cleanup:
 * - Reduced Storage: ~40-50% index size reduction (depends on delete ratio)
 * - Faster Writes: ~50% fewer index updates per transaction
 * - Better I/O: Fewer index pages to read/write
 * - Lower Memory: Smaller index structures fit better in cache
 * - Simplified Maintenance: One index per query pattern
 *
 * Migration Safety:
 * - Destructive: Drops indexes only (no data loss)
 * - Reversible: Migration22Down recreates dropped indexes
 * - No Data Loss: Only index structures affected, table data unchanged
 * - Backward Compatible: Partial indexes already cover all query patterns
 * - Minimal Downtime: DROP INDEX doesn't block queries (uses existing index until drop completes)
 *
 * Query Coverage Analysis:
 * - All UserDao queries: Covered by partial indexes (Migration18)
 * - All FinancialRecordDao queries: Covered by partial indexes (Migration16)
 * - All TransactionDao queries: Covered by partial indexes (Migration16)
 * - All WebhookEventDao queries: Covered by partial indexes (Migration21)
 * - Zero Performance Impact: Partial indexes are optimized for actual query patterns
 *
 * Future Migration Strategy:
 * - Migration23: Remove redundant @Index annotations from entity classes
 * - Room will only create partial indexes from migrations
 * - Clean architecture: Single source of truth (migrations, not annotations)
 */
class Migration22 : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop redundant full indexes from users table
        // Partial indexes from Migration18 provide better performance for active queries
        database.execSQL("DROP INDEX IF EXISTS index_users_last_name_first_name")

        // Drop redundant full indexes from financial_records table
        // Partial indexes from Migration16 provide better performance
        database.execSQL("DROP INDEX IF EXISTS index_financial_records_user_id_updated_at")

        // Note: NOT dropping index_financial_records_user_id_total_iuran_rekap
        // This index serves unique use case (total_iuran_rekap aggregation)
        // No partial index exists for this query pattern

        // Drop redundant full indexes from transactions table
        // Partial indexes from Migration16 provide better performance
        database.execSQL("DROP INDEX IF EXISTS index_transactions_user_id")
        database.execSQL("DROP INDEX IF EXISTS index_transactions_status")
        database.execSQL("DROP INDEX IF EXISTS index_transactions_user_id_status")
        database.execSQL("DROP INDEX IF EXISTS index_transactions_created_at")
        database.execSQL("DROP INDEX IF EXISTS index_transactions_updated_at")
    }
}

/**
 * Migration 22 Down: Recreate Dropped Full Indexes (Rollback)
 *
 * Rollback Strategy:
 * - Recreate all full indexes that were dropped in Migration22
 * - Restores Room entity @Index index definitions
 * - Maintains query coverage if partial indexes are insufficient
 *
 * Safety:
 * - Restores previous index state
 * - No data modification required
 * - Instant rollback (only CREATE INDEX statements)
 */
object Migration22Down : Migration(22, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recreate full indexes for users table
        database.execSQL("CREATE INDEX index_users_last_name_first_name ON users(last_name, first_name)")

        // Recreate full indexes for financial_records table
        database.execSQL("CREATE INDEX index_financial_records_user_id_updated_at ON financial_records(user_id, updated_at)")

        // Recreate full indexes for transactions table
        database.execSQL("CREATE INDEX index_transactions_user_id ON transactions(user_id)")
        database.execSQL("CREATE INDEX index_transactions_status ON transactions(status)")
        database.execSQL("CREATE INDEX index_transactions_user_id_status ON transactions(user_id, status)")
        database.execSQL("CREATE INDEX index_transactions_created_at ON transactions(created_at)")
        database.execSQL("CREATE INDEX index_transactions_updated_at ON transactions(updated_at)")
    }
}
