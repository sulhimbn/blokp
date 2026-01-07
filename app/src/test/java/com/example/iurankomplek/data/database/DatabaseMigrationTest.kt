package com.example.iurankomplek.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun `migration1 should create all tables and indexes correctly`() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
            execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")

            val cursor = query("SELECT COUNT(*) FROM users")
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
            cursor.close()

            val financialCursor = query("SELECT COUNT(*) FROM financial_records")
            financialCursor.moveToFirst()
            assertEquals(1, financialCursor.getInt(0))
            financialCursor.close()
        }
    }

    @Test
    fun `migration1 should enforce email uniqueness constraint`() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")

            try {
                execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'Jane', 'Smith', '456 Oak Ave', 'https://example.com/avatar2.jpg')")
                fail("Should have thrown SQLiteConstraintException")
            } catch (e: Exception) {
                assertTrue(e.message?.contains("UNIQUE constraint failed") == true)
            }
        }
    }

    @Test
    fun `migration1 should enforce CHECK constraints on users table`() {
        helper.createDatabase(TEST_DB, 1).apply {
            var caught = false
            try {
                execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'a'.repeat(2049))")
            } catch (e: Exception) {
                caught = true
            }
            assertTrue("Should enforce max avatar length", caught)

            caught = false
            try {
                execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
                execSQL("UPDATE users SET email = '' WHERE id = 1")
            } catch (e: Exception) {
                caught = true
            }
            assertTrue("Should enforce email not blank", caught)
        }
    }

    @Test
    fun `migration1 should enforce CHECK constraints on financial_records table`() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")

            var caught = false
            try {
                execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, -1, 200, 300, 400, 500, 'Maintenance')")
            } catch (e: Exception) {
                caught = true
            }
            assertTrue("Should enforce non-negative iuran_perwarga", caught)

            caught = false
            try {
                execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, '')")
            } catch (e: Exception) {
                caught = true
            }
            assertTrue("Should enforce pemanfaatan_iuran not blank", caught)
        }
    }

    @Test
    fun `migration1 should create indexes correctly`() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='users'")
            val indexNames = mutableListOf<String>()
            while (indexesCursor.moveToNext()) {
                indexNames.add(indexesCursor.getString(0))
            }
            indexesCursor.close()

            assertTrue("Should have email index", indexNames.any { it.contains("email") })

            val financialIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
            val financialIndexNames = mutableListOf<String>()
            while (financialIndexesCursor.moveToNext()) {
                financialIndexNames.add(financialIndexesCursor.getString(0))
            }
            financialIndexesCursor.close()

            assertTrue("Should have user_id index", financialIndexNames.any { it.contains("user_id") })
            assertTrue("Should have updated_at index", financialIndexNames.any { it.contains("updated_at") })
        }
    }

    @Test
    fun `migration1Down should drop all tables and indexes`() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            Migration1Down.migrate(db)

            val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            cursor.close()

            assertFalse("users table should be dropped", tables.contains("users"))
            assertFalse("financial_records table should be dropped", tables.contains("financial_records"))

            val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index'")
            val indexes = mutableListOf<String>()
            while (indexesCursor.moveToNext()) {
                indexes.add(indexesCursor.getString(0))
            }
            indexesCursor.close()

            assertFalse("email index should be dropped", indexes.any { it.contains("email") })
            assertFalse("user_id index should be dropped", indexes.any { it.contains("user_id") })
            assertFalse("updated_at index should be dropped", indexes.any { it.contains("updated_at") })
        }
    }

    @Test
    fun `migration2 should create webhook_events table correctly`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration2)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        val cursor = db.query("SELECT COUNT(*) FROM webhook_events")
        cursor.moveToFirst()
        assertEquals(1, cursor.getInt(0))
        cursor.close()
    }

    @Test
    fun `migration2 should create indexes on webhook_events table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration2)

        val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'")
        val indexNames = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            indexNames.add(indexesCursor.getString(0))
        }
        indexesCursor.close()

        assertTrue("Should have idempotency_key index", indexNames.any { it.contains("idempotency_key") })
        assertTrue("Should have status index", indexNames.any { it.contains("status") })
        assertTrue("Should have event_type index", indexNames.any { it.contains("event_type") })
    }

    @Test
    fun `migration2 should enforce idempotency_key uniqueness`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration2)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        try {
            db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.failed', '{\"test\":\"data2\"}', 'FAILED')")
            fail("Should have thrown SQLiteConstraintException")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("UNIQUE constraint failed") == true)
        }
    }

    @Test
    fun `migration2Down should drop webhook_events table and indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration2)

        Migration2Down.migrate(db)

        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='webhook_events'")
        cursor.moveToFirst()
        val count = cursor.count
        cursor.close()

        assertEquals("webhook_events table should be dropped", 0, count)

        val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'")
        val indexes = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            indexes.add(indexesCursor.getString(0))
        }
        indexesCursor.close()

        assertFalse("idempotency_key index should be dropped", indexes.any { it.contains("idempotency_key") })
        assertFalse("status index should be dropped", indexes.any { it.contains("status") })
        assertFalse("event_type index should be dropped", indexes.any { it.contains("event_type") })
    }

    @Test
    fun `migration2 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration2)

        val cursor = db.query("SELECT COUNT(*) FROM users")
        cursor.moveToFirst()
        val userCount = cursor.getInt(0)
        cursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        assertEquals("User data should be preserved", 1, userCount)
        assertEquals("Financial record data should be preserved", 1, financialCount)
    }

    @Test
    fun `migration2Down should preserve users and financial_records data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration2)
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        Migration2Down.migrate(db)

        val cursor = db.query("SELECT COUNT(*) FROM users")
        cursor.moveToFirst()
        val userCount = cursor.getInt(0)
        cursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        assertEquals("User data should be preserved", 1, userCount)
        assertEquals("Financial record data should be preserved", 1, financialCount)
    }

    @Test
    fun `migration1_2_migrations_in_sequence should work`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration1Down, Migration2)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        val userCursor = db.query("SELECT COUNT(*) FROM users")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Users should exist", 1, userCount)
        assertEquals("Financial records should exist", 1, financialCount)
        assertEquals("Webhook events should exist", 1, webhookCount)
    }

    @Test
    fun `migration2_1_down_migration_in_sequence should work`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration1Down, Migration2)
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        Migration2Down.migrate(db)

        val userCursor = db.query("SELECT COUNT(*) FROM users")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val webhookCursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='webhook_events'")
        val webhookTableExists = webhookCursor.count > 0
        webhookCursor.close()

        assertEquals("Users should be preserved", 1, userCount)
        assertEquals("Financial records should be preserved", 1, financialCount)
        assertFalse("Webhook events table should be dropped", webhookTableExists)
    }

    @Test
    fun `migration2_0_full_down_migration_sequence should work`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration1Down, Migration2)

        Migration2Down.migrate(db)
        Migration1Down.migrate(db)

        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()

        assertFalse("users table should be dropped", tables.contains("users"))
        assertFalse("financial_records table should be dropped", tables.contains("financial_records"))
        assertFalse("webhook_events table should be dropped", tables.contains("webhook_events"))
    }

    @Test
    fun `migration1 should create foreign key constraint correctly`() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            var caught = false
            try {
                db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (999, 100, 200, 300, 400, 500, 'Maintenance')")
            } catch (e: Exception) {
                caught = true
            }
            assertTrue("Should enforce foreign key constraint", caught)
        }
    }

    @Test
    fun `migration1 should enforce cascade delete on foreign key`() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
            db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")

            var cursor = db.query("SELECT COUNT(*) FROM financial_records")
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
            cursor.close()

            db.execSQL("DELETE FROM users WHERE id = 1")

            cursor = db.query("SELECT COUNT(*) FROM financial_records")
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
            cursor.close()
        }
    }

    @Test
    fun `migration2 should set correct default values for webhook_events`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration2)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        val cursor = db.query("SELECT retry_count, max_retries FROM webhook_events WHERE idempotency_key='whk_1234567890_abc'")
        cursor.moveToFirst()
        val retryCount = cursor.getInt(0)
        val maxRetries = cursor.getInt(1)
        cursor.close()

        assertEquals("retry_count should default to 0", 0, retryCount)
        assertEquals("max_retries should default to 5", 5, maxRetries)
    }

    @Test
    fun `migration3 should create composite index on users table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)

        val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='users'")
        val indexNames = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            indexNames.add(indexesCursor.getString(0))
        }
        indexesCursor.close()

        assertTrue("Should have email index", indexNames.any { it.contains("email") })
        assertTrue("Should have idx_users_name_sort index", indexNames.contains("idx_users_name_sort"))
    }

    @Test
    fun `migration3 should create composite index on financial_records table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)

        val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        val indexNames = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            indexNames.add(indexesCursor.getString(0))
        }
        indexesCursor.close()

        assertTrue("Should have idx_financial_user_updated index", indexNames.contains("idx_financial_user_updated"))
        assertTrue("Should have updated_at index", indexNames.any { it.contains("updated_at") })
    }

    @Test
    fun `migration3 should create composite index on webhook_events table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)

        val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'")
        val indexNames = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            indexNames.add(indexesCursor.getString(0))
        }
        indexesCursor.close()

        assertTrue("Should have idempotency_key index", indexNames.any { it.contains("idempotency_key") })
        assertTrue("Should have status index", indexNames.any { it.contains("status") })
        assertTrue("Should have event_type index", indexNames.any { it.contains("event_type") })
        assertTrue("Should have idx_webhook_retry_queue index", indexNames.contains("idx_webhook_retry_queue"))
    }

    @Test
    fun `migration3 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)

        val userCursor = db.query("SELECT COUNT(*) FROM users")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("User data should be preserved", 1, userCount)
        assertEquals("Financial record data should be preserved", 1, financialCount)
        assertEquals("Webhook event data should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration3Down should drop composite indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        Migration3Down.migrate(db)

        val usersIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='users'")
        val userIndexNames = mutableListOf<String>()
        while (usersIndexesCursor.moveToNext()) {
            userIndexNames.add(usersIndexesCursor.getString(0))
        }
        usersIndexesCursor.close()

        val financialIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        val financialIndexNames = mutableListOf<String>()
        while (financialIndexesCursor.moveToNext()) {
            financialIndexNames.add(financialIndexesCursor.getString(0))
        }
        financialIndexesCursor.close()

        val webhookIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'")
        val webhookIndexNames = mutableListOf<String>()
        while (webhookIndexesCursor.moveToNext()) {
            webhookIndexNames.add(webhookIndexesCursor.getString(0))
        }
        webhookIndexesCursor.close()

        assertFalse("idx_users_name_sort should be dropped", userIndexNames.contains("idx_users_name_sort"))
        assertFalse("idx_financial_user_updated should be dropped", financialIndexNames.contains("idx_financial_user_updated"))
        assertFalse("idx_webhook_retry_queue should be dropped", webhookIndexNames.contains("idx_webhook_retry_queue"))
    }

    @Test
    fun `migration3Down should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        Migration3Down.migrate(db)

        val userCursor = db.query("SELECT COUNT(*) FROM users")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("User data should be preserved", 1, userCount)
        assertEquals("Financial record data should be preserved", 1, financialCount)
        assertEquals("Webhook event data should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration3Down should preserve base indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        Migration3Down.migrate(db)

        val usersIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='users'")
        val userIndexNames = mutableListOf<String>()
        while (usersIndexesCursor.moveToNext()) {
            userIndexNames.add(usersIndexesCursor.getString(0))
        }
        usersIndexesCursor.close()

        val financialIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        val financialIndexNames = mutableListOf<String>()
        while (financialIndexesCursor.moveToNext()) {
            financialIndexNames.add(financialIndexesCursor.getString(0))
        }
        financialIndexesCursor.close()

        val webhookIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'")
        val webhookIndexNames = mutableListOf<String>()
        while (webhookIndexesCursor.moveToNext()) {
            webhookIndexNames.add(webhookIndexesCursor.getString(0))
        }
        webhookIndexesCursor.close()

        assertTrue("email index should still exist", userIndexNames.any { it.contains("email") })
        assertTrue("user_id index should still exist", financialIndexNames.any { it.contains("user_id") })
        assertTrue("idempotency_key index should still exist", webhookIndexNames.any { it.contains("idempotency_key") })
        assertTrue("status index should still exist", webhookIndexNames.any { it.contains("status") })
        assertTrue("event_type index should still exist", webhookIndexNames.any { it.contains("event_type") })
    }

    @Test
    fun `migration1_2_3_migrations_in_sequence should work`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        val userCursor = db.query("SELECT COUNT(*) FROM users")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Users should exist", 1, userCount)
        assertEquals("Financial records should exist", 1, financialCount)
        assertEquals("Webhook events should exist", 1, webhookCount)
    }

    @Test
    fun `migration3_2_1_full_down_migration_sequence should work`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        Migration3Down.migrate(db)
        Migration2Down.migrate(db)
        Migration1Down.migrate(db)

        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()

        assertFalse("users table should be dropped", tables.contains("users"))
        assertFalse("financial_records table should be dropped", tables.contains("financial_records"))
        assertFalse("webhook_events table should be dropped", tables.contains("webhook_events"))
    }
}
