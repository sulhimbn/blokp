package com.example.iurankomplek.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration24Test {

    private lateinit var helper: MigrationTestHelper
    private lateinit var db: androidx.sqlite.db.SupportSQLiteDatabase

    @Before
    fun setup() {
        helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java.canonicalName,
            androidx.test.InstrumentationRegistry.getInstrumentation()
        )
    }

    @After
    fun tearDown() {
        helper.closeWhenFinished(db)
    }

    @Test
    fun migrate23To24_allIndexesCreatedSuccessfully() {
        db = helper.createDatabase("test-migration-24", 23)

        db.beginTransaction()
        try {
            val testUserId = 1L
            val currentTime = System.currentTimeMillis() / 1000

            db.execSQL("""
                INSERT INTO transactions (
                    id, user_id, amount, currency, status, payment_method,
                    description, is_deleted, created_at, updated_at, metadata
                ) VALUES (
                    'test-001', $testUserId, 10000, 'IDR', 'COMPLETED', 'CREDIT_CARD',
                    'Test transaction 1', 0, $currentTime, $currentTime, ''
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO transactions (
                    id, user_id, amount, currency, status, payment_method,
                    description, is_deleted, created_at, updated_at, metadata
                ) VALUES (
                    'test-002', $testUserId, 20000, 'IDR', 'PENDING', 'BANK_TRANSFER',
                    'Test transaction 2', 0, $currentTime, $currentTime, ''
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO transactions (
                    id, user_id, amount, currency, status, payment_method,
                    description, is_deleted, created_at, updated_at, metadata
                ) VALUES (
                    'test-003', ${testUserId + 1}, 30000, 'IDR', 'FAILED', 'E_WALLET',
                    'Test transaction 3', 1, $currentTime, $currentTime + 100, ''
                )
            """.trimIndent())

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        val beforeIndexes = getIndexes(db, "transactions")

        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val afterIndexes = getIndexes(db, "transactions")

        val newIndexes = afterIndexes - beforeIndexes
        assertTrue(newIndexes.contains("idx_transactions_user_deleted"), "idx_transactions_user_deleted should be created")
        assertTrue(newIndexes.contains("idx_transactions_status_deleted"), "idx_transactions_status_deleted should be created")
        assertTrue(newIndexes.contains("idx_transactions_deleted_updated"), "idx_transactions_deleted_updated should be created")

        val indexInfo = db.query("PRAGMA index_info(idx_transactions_user_deleted)")
        var colIndex = 0
        var userColFound = false
        var deletedColFound = false
        while (indexInfo.moveToNext()) {
            val columnName = indexInfo.getString(indexInfo.getColumnIndex("name"))
            if (columnName == "user_id") {
                assertEquals(colIndex, 0, "user_id should be first column in index")
                userColFound = true
            } else if (columnName == "is_deleted") {
                assertEquals(colIndex, 1, "is_deleted should be second column in index")
                deletedColFound = true
            }
            colIndex++
        }
        indexInfo.close()
        assertTrue(userColFound, "user_id column should exist in idx_transactions_user_deleted")
        assertTrue(deletedColFound, "is_deleted column should exist in idx_transactions_user_deleted")
    }

    @Test
    fun migrate23To24_existingDataPreserved() {
        db = helper.createDatabase("test-migration-24-data", 23)

        val testUserId = 1L
        val currentTime = System.currentTimeMillis() / 1000

        db.beginTransaction()
        try {
            db.execSQL("""
                INSERT INTO transactions (
                    id, user_id, amount, currency, status, payment_method,
                    description, is_deleted, created_at, updated_at, metadata
                ) VALUES (
                    'test-001', $testUserId, 10000, 'IDR', 'COMPLETED', 'CREDIT_CARD',
                    'Test transaction 1', 0, $currentTime, $currentTime, ''
                ), (
                    'test-002', $testUserId, 20000, 'IDR', 'PENDING', 'BANK_TRANSFER',
                    'Test transaction 2', 0, $currentTime, $currentTime, ''
                ), (
                    'test-003', ${testUserId + 1}, 30000, 'IDR', 'FAILED', 'E_WALLET',
                    'Test transaction 3', 1, $currentTime, $currentTime + 100, ''
                )
            """.trimIndent())
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val cursor = db.query("SELECT COUNT(*) FROM transactions")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        assertEquals(3, count, "All 3 transactions should be preserved")

        val completedCursor = db.query("SELECT * FROM transactions WHERE status = 'COMPLETED' AND is_deleted = 0")
        assertEquals(1, completedCursor.count, "1 COMPLETED transaction should exist")
        completedCursor.moveToFirst()
        assertEquals("test-001", completedCursor.getString(completedCursor.getColumnIndex("id")))
        completedCursor.close()

        val deletedCursor = db.query("SELECT * FROM transactions WHERE is_deleted = 1")
        assertEquals(1, deletedCursor.count, "1 deleted transaction should exist")
        deletedCursor.moveToFirst()
        assertEquals("test-003", deletedCursor.getString(deletedCursor.getColumnIndex("id")))
        deletedCursor.close()
    }

    @Test
    fun migrate24To23_indexesDroppedSuccessfully() {
        db = helper.createDatabase("test-migration-24-down", 24)
        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val beforeIndexes = getIndexes(db, "transactions")
        assertTrue(beforeIndexes.contains("idx_transactions_user_deleted"), "idx_transactions_user_deleted should exist before rollback")
        assertTrue(beforeIndexes.contains("idx_transactions_status_deleted"), "idx_transactions_status_deleted should exist before rollback")
        assertTrue(beforeIndexes.contains("idx_transactions_deleted_updated"), "idx_transactions_deleted_updated should exist before rollback")

        helper.runMigrationsAndValidate(db, 24, 23, true, Migration24Down())

        val afterIndexes = getIndexes(db, "transactions")
        assertFalse(afterIndexes.contains("idx_transactions_user_deleted"), "idx_transactions_user_deleted should be dropped after rollback")
        assertFalse(afterIndexes.contains("idx_transactions_status_deleted"), "idx_transactions_status_deleted should be dropped after rollback")
        assertFalse(afterIndexes.contains("idx_transactions_deleted_updated"), "idx_transactions_deleted_updated should be dropped after rollback")
    }

    @Test
    fun migrate24To23_existingDataPreserved() {
        db = helper.createDatabase("test-migration-24-down-data", 24)

        val testUserId = 1L
        val currentTime = System.currentTimeMillis() / 1000

        db.beginTransaction()
        try {
            db.execSQL("""
                INSERT INTO transactions (
                    id, user_id, amount, currency, status, payment_method,
                    description, is_deleted, created_at, updated_at, metadata
                ) VALUES (
                    'test-001', $testUserId, 10000, 'IDR', 'COMPLETED', 'CREDIT_CARD',
                    'Test transaction 1', 0, $currentTime, $currentTime, ''
                ), (
                    'test-002', $testUserId, 20000, 'IDR', 'PENDING', 'BANK_TRANSFER',
                    'Test transaction 2', 0, $currentTime, $currentTime, ''
                )
            """.trimIndent())
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())
        helper.runMigrationsAndValidate(db, 24, 23, true, Migration24Down())

        val cursor = db.query("SELECT COUNT(*) FROM transactions")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        assertEquals(2, count, "All 2 transactions should be preserved during rollback")

        val completedCursor = db.query("SELECT * FROM transactions WHERE id = 'test-001'")
        completedCursor.moveToFirst()
        assertEquals("COMPLETED", completedCursor.getString(completedCursor.getColumnIndex("status")))
        assertEquals(0, completedCursor.getInt(completedCursor.getColumnIndex("is_deleted")))
        completedCursor.close()
    }

    @Test
    fun indexesSupportQueryOptimization_userDeletedIndex() {
        db = helper.createDatabase("test-migration-24-idx-1", 23)
        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM transactions WHERE user_id = 1 AND is_deleted = 0")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_transactions_user_deleted")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "Query should use idx_transactions_user_deleted index")
    }

    @Test
    fun indexesSupportQueryOptimization_statusDeletedIndex() {
        db = helper.createDatabase("test-migration-24-idx-2", 23)
        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM transactions WHERE status = 'COMPLETED' AND is_deleted = 0")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_transactions_status_deleted")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "Query should use idx_transactions_status_deleted index")
    }

    @Test
    fun indexesSupportQueryOptimization_deletedUpdatedIndex() {
        db = helper.createDatabase("test-migration-24-idx-3", 23)
        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val testUserId = 1L
        val currentTime = System.currentTimeMillis() / 1000

        db.beginTransaction()
        try {
            db.execSQL("""
                INSERT INTO transactions (
                    id, user_id, amount, currency, status, payment_method,
                    description, is_deleted, created_at, updated_at, metadata
                ) VALUES (
                    'test-001', $testUserId, 10000, 'IDR', 'COMPLETED', 'CREDIT_CARD',
                    'Test transaction 1', 1, $currentTime, $currentTime + 100, ''
                ), (
                    'test-002', $testUserId, 20000, 'IDR', 'PENDING', 'BANK_TRANSFER',
                    'Test transaction 2', 1, $currentTime, $currentTime + 200, ''
                ), (
                    'test-003', ${testUserId + 1}, 30000, 'IDR', 'FAILED', 'E_WALLET',
                    'Test transaction 3', 1, $currentTime, $currentTime + 50, ''
                )
            """.trimIndent())
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM transactions WHERE is_deleted = 1 ORDER BY updated_at DESC")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_transactions_deleted_updated")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "Query should use idx_transactions_deleted_updated index")
    }

    @Test
    fun indexColumnsAreInCorrectOrder() {
        db = helper.createDatabase("test-migration-24-order", 23)
        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val indexInfo = db.query("PRAGMA index_info(idx_transactions_user_deleted)")
        var colIndex = 0
        var userColFound = false
        var deletedColFound = false

        while (indexInfo.moveToNext()) {
            val columnName = indexInfo.getString(indexInfo.getColumnIndex("name"))
            if (columnName == "user_id") {
                assertEquals(colIndex, 0, "user_id should be at position 0")
                userColFound = true
            } else if (columnName == "is_deleted") {
                assertEquals(colIndex, 1, "is_deleted should be at position 1")
                deletedColFound = true
            }
            colIndex++
        }
        indexInfo.close()

        assertTrue(userColFound, "user_id should exist in idx_transactions_user_deleted")
        assertTrue(deletedColFound, "is_deleted should exist in idx_transactions_user_deleted")
    }

    @Test
    fun originalUserIndexStillExists() {
        db = helper.createDatabase("test-migration-24-orig", 23)
        helper.runMigrationsAndValidate(db, 23, 24, true, Migration24())

        val indexes = getIndexes(db, "transactions")
        assertTrue(indexes.contains("idx_transactions_user_id"), "Original user_id index should still exist")
    }

    private fun getIndexes(db: androidx.sqlite.db.SupportSQLiteDatabase, tableName: String): Set<String> {
        val indexes = mutableSetOf<String>()
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = '$tableName'")
        while (cursor.moveToNext()) {
            val indexName = cursor.getString(cursor.getColumnIndex("name"))
            if (!indexName.startsWith("sqlite_")) {
                indexes.add(indexName)
            }
        }
        cursor.close()
        return indexes
    }

    private val InstrumentationRegistry = androidx.test.platform.app.InstrumentationRegistry
}
