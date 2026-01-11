package com.example.iurankomplek.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration25Test {

    private lateinit var helper: MigrationTestHelper
    private lateinit var db: androidx.sqlite.db.SupportSQLiteDatabase

    @Before
    fun setup() {
        helper = MigrationTestHelper(
            androidx.test.InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java.canonicalName,
            androidx.test.InstrumentationRegistry.getInstrumentation()
        )
    }

    @After
    fun tearDown() {
        helper.closeWhenFinished(db)
    }

    @Test
    fun migrate24To25_allIndexesCreatedSuccessfully() {
        db = helper.createDatabase("test-migration-25", 24)

        val currentTime = System.currentTimeMillis() / 1000

        db.beginTransaction()
        try {
            db.execSQL("""
                INSERT INTO users (
                    id, email, first_name, last_name, alamat, avatar,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    1, 'test@example.com', 'John', 'Doe', 'Address', 'avatar.jpg',
                    0, $currentTime, $currentTime
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO users (
                    id, email, first_name, last_name, alamat, avatar,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    2, 'deleted@example.com', 'Jane', 'Smith', 'Deleted Address', 'deleted.jpg',
                    1, $currentTime, $currentTime + 100
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO financial_records (
                    id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu,
                    pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    1, 1, 1000, 2000, 3000, 500, 3500, 'Utility bills',
                    0, $currentTime, $currentTime
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO financial_records (
                    id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu,
                    pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    2, 1, 2000, 3000, 4000, 600, 4600, 'Maintenance',
                    1, $currentTime, $currentTime + 200
                )
            """.trimIndent())

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        val beforeUserIndexes = getIndexes(db, "users")
        val beforeFinancialIndexes = getIndexes(db, "financial_records")

        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val afterUserIndexes = getIndexes(db, "users")
        val afterFinancialIndexes = getIndexes(db, "financial_records")

        val newUserIndexes = afterUserIndexes - beforeUserIndexes
        val newFinancialIndexes = afterFinancialIndexes - beforeFinancialIndexes

        assertTrue(newUserIndexes.contains("idx_users_deleted_last_name_first_name"), "idx_users_deleted_last_name_first_name should be created")
        assertTrue(newUserIndexes.contains("idx_users_deleted_updated_at"), "idx_users_deleted_updated_at should be created")
        assertTrue(newFinancialIndexes.contains("idx_financial_records_user_deleted_updated_at"), "idx_financial_records_user_deleted_updated_at should be created")
        assertTrue(newFinancialIndexes.contains("idx_financial_records_deleted_updated_at"), "idx_financial_records_deleted_updated_at should be created")

        assertFalse(afterFinancialIndexes.contains("idx_financial_records_user_total"), "idx_financial_records_user_total should be removed")

        val indexInfo = db.query("PRAGMA index_info(idx_users_deleted_last_name_first_name)")
        var colIndex = 0
        var deletedColFound = false
        var lastNameColFound = false
        var firstNameColFound = false
        while (indexInfo.moveToNext()) {
            val columnName = indexInfo.getString(indexInfo.getColumnIndex("name"))
            if (columnName == "is_deleted") {
                assertEquals(colIndex, 0, "is_deleted should be first column in index")
                deletedColFound = true
            } else if (columnName == "last_name") {
                assertEquals(colIndex, 1, "last_name should be second column in index")
                lastNameColFound = true
            } else if (columnName == "first_name") {
                assertEquals(colIndex, 2, "first_name should be third column in index")
                firstNameColFound = true
            }
            colIndex++
        }
        indexInfo.close()
        assertTrue(deletedColFound, "is_deleted column should exist in idx_users_deleted_last_name_first_name")
        assertTrue(lastNameColFound, "last_name column should exist in idx_users_deleted_last_name_first_name")
        assertTrue(firstNameColFound, "first_name column should exist in idx_users_deleted_last_name_first_name")
    }

    @Test
    fun migrate24To25_existingDataPreserved() {
        db = helper.createDatabase("test-migration-25-data", 24)

        val currentTime = System.currentTimeMillis() / 1000

        db.beginTransaction()
        try {
            db.execSQL("""
                INSERT INTO users (
                    id, email, first_name, last_name, alamat, avatar,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    1, 'test@example.com', 'John', 'Doe', 'Address', 'avatar.jpg',
                    0, $currentTime, $currentTime
                ), (
                    2, 'deleted@example.com', 'Jane', 'Smith', 'Deleted', 'deleted.jpg',
                    1, $currentTime, $currentTime + 100
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO financial_records (
                    id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu,
                    pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    1, 1, 1000, 2000, 3000, 500, 3500, 'Utility bills',
                    0, $currentTime, $currentTime
                ), (
                    2, 1, 2000, 3000, 4000, 600, 4600, 'Maintenance',
                    1, $currentTime, $currentTime + 200
                )
            """.trimIndent())

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val userCursor = db.query("SELECT COUNT(*) FROM users")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()
        assertEquals(2, userCount, "All 2 users should be preserved")

        val activeUserCursor = db.query("SELECT * FROM users WHERE is_deleted = 0")
        assertEquals(1, activeUserCursor.count, "1 active user should exist")
        activeUserCursor.moveToFirst()
        assertEquals("test@example.com", activeUserCursor.getString(activeUserCursor.getColumnIndex("email")))
        assertEquals("John", activeUserCursor.getString(activeUserCursor.getColumnIndex("first_name")))
        assertEquals("Doe", activeUserCursor.getString(activeUserCursor.getColumnIndex("last_name")))
        activeUserCursor.close()

        val deletedUserCursor = db.query("SELECT * FROM users WHERE is_deleted = 1")
        assertEquals(1, deletedUserCursor.count, "1 deleted user should exist")
        deletedUserCursor.moveToFirst()
        assertEquals("deleted@example.com", deletedUserCursor.getString(deletedUserCursor.getColumnIndex("email")))
        deletedUserCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()
        assertEquals(2, financialCount, "All 2 financial records should be preserved")

        val activeFinancialCursor = db.query("SELECT * FROM financial_records WHERE is_deleted = 0")
        assertEquals(1, activeFinancialCursor.count, "1 active financial record should exist")
        activeFinancialCursor.moveToFirst()
        assertEquals("Utility bills", activeFinancialCursor.getString(activeFinancialCursor.getColumnIndex("pemanfaatan_iuran")))
        activeFinancialCursor.close()

        val deletedFinancialCursor = db.query("SELECT * FROM financial_records WHERE is_deleted = 1")
        assertEquals(1, deletedFinancialCursor.count, "1 deleted financial record should exist")
        deletedFinancialCursor.moveToFirst()
        assertEquals("Maintenance", deletedFinancialCursor.getString(deletedFinancialCursor.getColumnIndex("pemanfaatan_iuran")))
        deletedFinancialCursor.close()
    }

    @Test
    fun migrate25To24_indexesDroppedSuccessfully() {
        db = helper.createDatabase("test-migration-25-down", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val beforeUserIndexes = getIndexes(db, "users")
        val beforeFinancialIndexes = getIndexes(db, "financial_records")

        assertTrue(beforeUserIndexes.contains("idx_users_deleted_last_name_first_name"), "idx_users_deleted_last_name_first_name should exist before rollback")
        assertTrue(beforeUserIndexes.contains("idx_users_deleted_updated_at"), "idx_users_deleted_updated_at should exist before rollback")
        assertTrue(beforeFinancialIndexes.contains("idx_financial_records_user_deleted_updated_at"), "idx_financial_records_user_deleted_updated_at should exist before rollback")
        assertTrue(beforeFinancialIndexes.contains("idx_financial_records_deleted_updated_at"), "idx_financial_records_deleted_updated_at should exist before rollback")

        helper.runMigrationsAndValidate(db, 25, 24, true, Migration25Down())

        val afterUserIndexes = getIndexes(db, "users")
        val afterFinancialIndexes = getIndexes(db, "financial_records")

        assertFalse(afterUserIndexes.contains("idx_users_deleted_last_name_first_name"), "idx_users_deleted_last_name_first_name should be dropped after rollback")
        assertFalse(afterUserIndexes.contains("idx_users_deleted_updated_at"), "idx_users_deleted_updated_at should be dropped after rollback")
        assertFalse(afterFinancialIndexes.contains("idx_financial_records_user_deleted_updated_at"), "idx_financial_records_user_deleted_updated_at should be dropped after rollback")
        assertFalse(afterFinancialIndexes.contains("idx_financial_records_deleted_updated_at"), "idx_financial_records_deleted_updated_at should be dropped after rollback")

        assertTrue(afterFinancialIndexes.contains("idx_financial_records_user_total"), "idx_financial_records_user_total should be recreated after rollback")
    }

    @Test
    fun migrate25To24_existingDataPreserved() {
        db = helper.createDatabase("test-migration-25-down-data", 24)

        val currentTime = System.currentTimeMillis() / 1000

        db.beginTransaction()
        try {
            db.execSQL("""
                INSERT INTO users (
                    id, email, first_name, last_name, alamat, avatar,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    1, 'test@example.com', 'John', 'Doe', 'Address', 'avatar.jpg',
                    0, $currentTime, $currentTime
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO financial_records (
                    id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu,
                    pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran,
                    is_deleted, created_at, updated_at
                ) VALUES (
                    1, 1, 1000, 2000, 3000, 500, 3500, 'Utility bills',
                    0, $currentTime, $currentTime
                )
            """.trimIndent())

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())
        helper.runMigrationsAndValidate(db, 25, 24, true, Migration25Down())

        val userCursor = db.query("SELECT COUNT(*) FROM users")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()
        assertEquals(1, userCount, "User should be preserved during rollback")

        val testUserCursor = db.query("SELECT * FROM users WHERE id = 1")
        testUserCursor.moveToFirst()
        assertEquals("test@example.com", testUserCursor.getString(testUserCursor.getColumnIndex("email")))
        assertEquals("John", testUserCursor.getString(testUserCursor.getColumnIndex("first_name")))
        assertEquals(0, testUserCursor.getInt(testUserCursor.getColumnIndex("is_deleted")))
        testUserCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()
        assertEquals(1, financialCount, "Financial record should be preserved during rollback")

        val testFinancialCursor = db.query("SELECT * FROM financial_records WHERE id = 1")
        testFinancialCursor.moveToFirst()
        assertEquals("Utility bills", testFinancialCursor.getString(testFinancialCursor.getColumnIndex("pemanfaatan_iuran")))
        assertEquals(0, testFinancialCursor.getInt(testFinancialCursor.getColumnIndex("is_deleted")))
        testFinancialCursor.close()
    }

    @Test
    fun indexesSupportQueryOptimization_getAllUsers() {
        db = helper.createDatabase("test-migration-25-idx-1", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM users WHERE is_deleted = 0 ORDER BY last_name ASC, first_name ASC")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_users_deleted_last_name_first_name")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "getAllUsers() query should use idx_users_deleted_last_name_first_name index")
    }

    @Test
    fun indexesSupportQueryOptimization_getDeletedUsers() {
        db = helper.createDatabase("test-migration-25-idx-2", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM users WHERE is_deleted = 1 ORDER BY updated_at DESC")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_users_deleted_updated_at")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "getDeletedUsers() query should use idx_users_deleted_updated_at index")
    }

    @Test
    fun indexesSupportQueryOptimization_getFinancialRecordsByUserId() {
        db = helper.createDatabase("test-migration-25-idx-3", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM financial_records WHERE user_id = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_financial_records_user_deleted_updated_at")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "getFinancialRecordsByUserId() query should use idx_financial_records_user_deleted_updated_at index")
    }

    @Test
    fun indexesSupportQueryOptimization_getAllFinancialRecords() {
        db = helper.createDatabase("test-migration-25-idx-4", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM financial_records WHERE is_deleted = 0 ORDER BY updated_at DESC")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_financial_records_deleted_updated_at")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "getAllFinancialRecords() query should use idx_financial_records_deleted_updated_at index")
    }

    @Test
    fun indexesSupportQueryOptimization_getDeletedFinancialRecords() {
        db = helper.createDatabase("test-migration-25-idx-5", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val queryPlan = db.query("EXPLAIN QUERY PLAN SELECT * FROM financial_records WHERE is_deleted = 1 ORDER BY updated_at DESC")
        var usesIndex = false
        while (queryPlan.moveToNext()) {
            val detail = queryPlan.getString(queryPlan.getColumnIndex("detail"))
            if (detail.contains("idx_financial_records_deleted_updated_at")) {
                usesIndex = true
                break
            }
        }
        queryPlan.close()
        assertTrue(usesIndex, "getDeletedFinancialRecords() query should use idx_financial_records_deleted_updated_at index")
    }

    @Test
    fun indexColumnsAreInCorrectOrder() {
        db = helper.createDatabase("test-migration-25-order", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val userIndexInfo = db.query("PRAGMA index_info(idx_users_deleted_last_name_first_name)")
        var colIndex = 0
        var deletedColFound = false
        var lastNameColFound = false
        var firstNameColFound = false

        while (userIndexInfo.moveToNext()) {
            val columnName = userIndexInfo.getString(userIndexInfo.getColumnIndex("name"))
            if (columnName == "is_deleted") {
                assertEquals(colIndex, 0, "is_deleted should be at position 0")
                deletedColFound = true
            } else if (columnName == "last_name") {
                assertEquals(colIndex, 1, "last_name should be at position 1")
                lastNameColFound = true
            } else if (columnName == "first_name") {
                assertEquals(colIndex, 2, "first_name should be at position 2")
                firstNameColFound = true
            }
            colIndex++
        }
        userIndexInfo.close()
        assertTrue(deletedColFound, "is_deleted should exist in idx_users_deleted_last_name_first_name")
        assertTrue(lastNameColFound, "last_name should exist in idx_users_deleted_last_name_first_name")
        assertTrue(firstNameColFound, "first_name should exist in idx_users_deleted_last_name_first_name")

        val financialIndexInfo = db.query("PRAGMA index_info(idx_financial_records_user_deleted_updated_at)")
        colIndex = 0
        var userIdColFound = false
        var financialDeletedColFound = false
        var updatedAtColFound = false

        while (financialIndexInfo.moveToNext()) {
            val columnName = financialIndexInfo.getString(financialIndexInfo.getColumnIndex("name"))
            if (columnName == "user_id") {
                assertEquals(colIndex, 0, "user_id should be at position 0")
                userIdColFound = true
            } else if (columnName == "is_deleted") {
                assertEquals(colIndex, 1, "is_deleted should be at position 1")
                financialDeletedColFound = true
            } else if (columnName == "updated_at") {
                assertEquals(colIndex, 2, "updated_at should be at position 2")
                updatedAtColFound = true
            }
            colIndex++
        }
        financialIndexInfo.close()
        assertTrue(userIdColFound, "user_id should exist in idx_financial_records_user_deleted_updated_at")
        assertTrue(financialDeletedColFound, "is_deleted should exist in idx_financial_records_user_deleted_updated_at")
        assertTrue(updatedAtColFound, "updated_at should exist in idx_financial_records_user_deleted_updated_at")
    }

    @Test
    fun originalUserEmailIndexStillExists() {
        db = helper.createDatabase("test-migration-25-orig", 24)
        helper.runMigrationsAndValidate(db, 24, 25, true, Migration25())

        val userIndexes = getIndexes(db, "users")
        assertTrue(userIndexes.contains("idx_users_email"), "Original email index should still exist")
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
}
