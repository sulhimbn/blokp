package com.example.iurankomplek.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1DownTest {
    private val TEST_DB = "migration-test-down"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To0_shouldDropTables() {
        var db = helper.createDatabase(TEST_DB, 0)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('test@example.com', 'Test', 'User', 'Address', 'avatar.jpg', 1000000, 1000000)")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, 100, 200, 300, 50, 250, 'Test', 1000000, 1000000)")
        
        val userCountBefore = db.query("SELECT COUNT(*) FROM users").use { it.moveToFirst(); it.getInt(0) }
        val financialCountBefore = db.query("SELECT COUNT(*) FROM financial_records").use { it.moveToFirst(); it.getInt(0) }
        
        assertEquals(1, userCountBefore, "Should have 1 user before downgrade")
        assertEquals(1, financialCountBefore, "Should have 1 financial record before downgrade")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 0, true, Migration1Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertFalse("users table should not exist", tables.contains("users"))
            assertFalse("financial_records table should not exist", tables.contains("financial_records"))
        }

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='index'").use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                val indexName = cursor.getString(0)
                if (!indexName.startsWith("sqlite_")) {
                    indexes.add(indexName)
                }
            }
            assertFalse("idx_users_email index should not exist", indexes.contains("idx_users_email"))
            assertFalse("idx_financial_user_id index should not exist", indexes.contains("idx_financial_user_id"))
            assertFalse("idx_financial_updated_at index should not exist", indexes.contains("idx_financial_updated_at"))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate1To0_shouldValidateCleanSchema() {
        var db = helper.createDatabase(TEST_DB, 0)
        
        db.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertEquals("Version 0 should have no user-defined tables", 0, tables.size)
        }
        
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 0, true, Migration1Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertEquals("After downgrade, schema should be clean", 0, tables.size)
        }

        downgradedDb.close()
    }

    @Test
    fun migrate1To0_shouldHandleEmptyDatabase() {
        var db = helper.createDatabase(TEST_DB, 0)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        val userCount = db.query("SELECT COUNT(*) FROM users").use { it.moveToFirst(); it.getInt(0) }
        val financialCount = db.query("SELECT COUNT(*) FROM financial_records").use { it.moveToFirst(); it.getInt(0) }
        
        assertEquals(0, userCount, "Should have 0 users before downgrade")
        assertEquals(0, financialCount, "Should have 0 financial records before downgrade")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 0, true, Migration1Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertFalse("users table should not exist", tables.contains("users"))
            assertFalse("financial_records table should not exist", tables.contains("financial_records"))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate1To0_shouldDropIndexesBeforeTables() {
        var db = helper.createDatabase(TEST_DB, 0)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('test@example.com', 'Test', 'User', 'Address', 'avatar.jpg', 1000000, 1000000)")
        
        val indexQuery = "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name = 'users'"
        val indexesBefore = db.query(indexQuery).use { cursor ->
            val list = mutableListOf<String>()
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0))
            }
            list
        }
        
        assertTrue("Should have indexes before downgrade", indexesBefore.any { !it.startsWith("sqlite_") })
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 0, true, Migration1Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertFalse("users table should not exist", tables.contains("users"))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate1To0_documentationNote() {
        val db = helper.createDatabase(TEST_DB, 0)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        migratedDb.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('important@example.com', 'Important', 'Data', 'Address', 'avatar.jpg', 1000000, 1000000)")
        migratedDb.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, 100, 200, 300, 50, 250, 'Important Data', 1000000, 1000000)")
        
        migratedDb.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 0, true, Migration1Down)

        downgradedDb.close()
    }
}
