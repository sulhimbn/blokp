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
class Migration2DownTest {
    private val TEST_DB = "migration-test-down-2"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate2To1_shouldDropWebhookEventsTable() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, transaction_id, status, retry_count, max_retries, created_at, updated_at) VALUES ('whk_test', 'test.event', '{\"data\":\"test\"}', 'tx123', 'PENDING', 0, 5, 1000000, 1000000)")
        
        val webhookCountBefore = db.query("SELECT COUNT(*) FROM webhook_events").use { it.moveToFirst(); it.getInt(0) }
        assertEquals(1, webhookCountBefore, "Should have 1 webhook event before downgrade")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertFalse("webhook_events table should not exist", tables.contains("webhook_events"))
            assertTrue("users table should still exist", tables.contains("users"))
            assertTrue("financial_records table should still exist", tables.contains("financial_records"))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldDropWebhookIndexes() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        val indexQuery = "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name = 'webhook_events'"
        val indexesBefore = db.query(indexQuery).use { cursor ->
            val list = mutableListOf<String>()
            while (cursor.moveToNext()) {
                if (!cursor.getString(0).startsWith("sqlite_")) {
                    list.add(cursor.getString(0))
                }
            }
            list
        }
        
        assertTrue("Should have webhook indexes before downgrade", indexesBefore.isNotEmpty())
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        val indexesAfter = downgradedDb.query(indexQuery).use { cursor ->
            val list = mutableListOf<String>()
            while (cursor.moveToNext()) {
                if (!cursor.getString(0).startsWith("sqlite_")) {
                    list.add(cursor.getString(0))
                }
            }
            list
        }
        
        assertEquals(0, indexesAfter.size, "Should have no webhook indexes after downgrade")

        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldPreserveUsersData() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('preserve@example.com', 'Test', 'User', 'Address', 'avatar.jpg', 1000000, 1000000)")
        
        val userCountBefore = db.query("SELECT COUNT(*) FROM users").use { it.moveToFirst(); it.getInt(0) }
        assertEquals(1, userCountBefore, "Should have 1 user before downgrade")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        val userCountAfter = downgradedDb.query("SELECT COUNT(*) FROM users").use { it.moveToFirst(); it.getInt(0) }
        assertEquals(1, userCountAfter, "Should still have 1 user after downgrade")
        
        downgradedDb.query("SELECT * FROM users WHERE email = 'preserve@example.com'").use { cursor ->
            assertTrue("User should still exist after downgrade", cursor.moveToFirst())
            assertEquals("preserve@example.com", cursor.getString(cursor.getColumnIndexOrThrow("email")))
            assertEquals("Test", cursor.getString(cursor.getColumnIndexOrThrow("first_name")))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldPreserveFinancialRecordsData() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('finance@example.com', 'Finance', 'User', 'Address', 'avatar.jpg', 1000000, 1000000)")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, 100, 200, 300, 50, 250, 'Finance Data', 1000000, 1000000)")
        
        val financialCountBefore = db.query("SELECT COUNT(*) FROM financial_records").use { it.moveToFirst(); it.getInt(0) }
        assertEquals(1, financialCountBefore, "Should have 1 financial record before downgrade")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        val financialCountAfter = downgradedDb.query("SELECT COUNT(*) FROM financial_records").use { it.moveToFirst(); it.getInt(0) }
        assertEquals(1, financialCountAfter, "Should still have 1 financial record after downgrade")
        
        downgradedDb.query("SELECT * FROM financial_records WHERE user_id = 1").use { cursor ->
            assertTrue("Financial record should still exist after downgrade", cursor.moveToFirst())
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("user_id")))
            assertEquals(100, cursor.getInt(cursor.getColumnIndexOrThrow("iuran_perwarga")))
            assertEquals("Finance Data", cursor.getString(cursor.getColumnIndexOrThrow("pemanfaatan_iuran")))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldHandleEmptyWebhookEvents() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        val webhookCountBefore = db.query("SELECT COUNT(*) FROM webhook_events").use { it.moveToFirst(); it.getInt(0) }
        assertEquals(0, webhookCountBefore, "Should have 0 webhook events before downgrade")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertFalse("webhook_events table should not exist", tables.contains("webhook_events"))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldPreserveUserAndFinancialIndexes() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('index@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 1000000, 1000000)")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, 100, 200, 300, 50, 250, 'Test', 1000000, 1000000)")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='index'").use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                val indexName = cursor.getString(0)
                if (!indexName.startsWith("sqlite_")) {
                    indexes.add(indexName)
                }
            }
            assertTrue("idx_users_email index should exist", indexes.contains("idx_users_email"))
            assertTrue("idx_financial_user_id index should exist", indexes.contains("idx_financial_user_id"))
            assertTrue("idx_financial_updated_at index should exist", indexes.contains("idx_financial_updated_at"))
            assertFalse("index_webhook_events_idempotency_key should not exist", indexes.any { it.contains("idempotency_key") })
        }

        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldValidateSchemaMatchesVersion1() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        downgradedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertEquals("Should have exactly 2 tables", 2, tables.size)
            assertTrue("users table should exist", tables.contains("users"))
            assertTrue("financial_records table should exist", tables.contains("financial_records"))
        }

        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldPreserveForeignKeyConstraints() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('fk@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 1000000, 1000000)")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, 100, 200, 300, 50, 250, 'Test', 1000000, 1000000)")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        var exception: Exception? = null
        try {
            downgradedDb.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (999, 100, 200, 300, 50, 250, 'Invalid', 1000000, 1000000)")
        } catch (e: Exception) {
            exception = e
        }

        assertNotNull("Foreign key constraint should still work after downgrade", exception)
        
        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldPreserveUniqueConstraints() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('unique@test.com', 'Test', 'User1', 'Address', 'avatar.jpg', 1000000, 1000000)")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        var exception: Exception? = null
        try {
            downgradedDb.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('unique@test.com', 'Test', 'User2', 'Address', 'avatar.jpg', 1000000, 1000000)")
        } catch (e: Exception) {
            exception = e
        }

        assertNotNull("Unique constraint should still work after downgrade", exception)
        
        downgradedDb.close()
    }

    @Test
    fun migrate2To1_shouldPreserveCheckConstraints() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('check@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 1000000, 1000000)")
        
        db.close()

        val downgradedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1(), Migration2(), Migration2Down)

        var exception: Exception? = null
        try {
            downgradedDb.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, -100, 200, 300, 50, 250, 'Test', 1000000, 1000000)")
        } catch (e: Exception) {
            exception = e
        }

        assertNotNull("Check constraint should still work after downgrade", exception)
        
        downgradedDb.close()
    }
}
