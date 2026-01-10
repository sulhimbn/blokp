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

    @Test
    fun `migration5 should add is_deleted columns to all tables`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration1Down, Migration2)
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)

        val userCursor = db.query("SELECT is_deleted FROM users WHERE id = 1")
        userCursor.moveToFirst()
        val userIsDeleted = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT is_deleted FROM financial_records WHERE id = 1")
        financialCursor.moveToFirst()
        val financialIsDeleted = financialCursor.getInt(0)
        financialCursor.close()

        assertEquals("User is_deleted should default to 0", 0, userIsDeleted)
        assertEquals("Financial record is_deleted should default to 0", 0, financialIsDeleted)
    }

    @Test
    fun `migration5 should create is_deleted indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)

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

        val transactionsIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val transactionIndexNames = mutableListOf<String>()
        while (transactionsIndexesCursor.moveToNext()) {
            transactionIndexNames.add(transactionsIndexesCursor.getString(0))
        }
        transactionsIndexesCursor.close()

        assertTrue("Should have idx_users_not_deleted index", userIndexNames.contains("idx_users_not_deleted"))
        assertTrue("Should have idx_financial_not_deleted index", financialIndexNames.contains("idx_financial_not_deleted"))
        assertTrue("Should have idx_transactions_not_deleted index", transactionIndexNames.contains("idx_transactions_not_deleted"))
    }

    @Test
    fun `migration5 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration1Down, Migration2)
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)

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
    fun `migration5Down should drop is_deleted columns and indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)

        Migration5Down.migrate(db)

        try {
            db.query("SELECT is_deleted FROM users LIMIT 1")
            fail("is_deleted column should not exist in users table")
        } catch (e: Exception) {
            assertTrue("Should fail with no such column error", e.message?.contains("no such column") == true)
        }

        try {
            db.query("SELECT is_deleted FROM financial_records LIMIT 1")
            fail("is_deleted column should not exist in financial_records table")
        } catch (e: Exception) {
            assertTrue("Should fail with no such column error", e.message?.contains("no such column") == true)
        }

        try {
            db.query("SELECT is_deleted FROM transactions LIMIT 1")
            fail("is_deleted column should not exist in transactions table")
        } catch (e: Exception) {
            assertTrue("Should fail with no such column error", e.message?.contains("no such column") == true)
        }

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

        val transactionsIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val transactionIndexNames = mutableListOf<String>()
        while (transactionsIndexesCursor.moveToNext()) {
            transactionIndexNames.add(transactionsIndexesCursor.getString(0))
        }
        transactionsIndexesCursor.close()

        assertFalse("idx_users_not_deleted should be dropped", userIndexNames.contains("idx_users_not_deleted"))
        assertFalse("idx_financial_not_deleted should be dropped", financialIndexNames.contains("idx_financial_not_deleted"))
        assertFalse("idx_transactions_not_deleted should be dropped", transactionIndexNames.contains("idx_transactions_not_deleted"))
    }

    @Test
    fun `migration5Down should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration1Down, Migration2)
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration1(), Migration1Down, Migration2, Migration3)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)

        Migration5Down.migrate(db)

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
    fun `migration1_2_3_4_5_migrations_in_sequence_should_work`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should exist", 1, userCount)
        assertEquals("Active financial records should exist", 1, financialCount)
        assertEquals("Webhook events should exist", 1, webhookCount)
    }

    @Test
    fun `migration4 should create composite index on financial_records`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4)

        val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        val indexNames = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            indexNames.add(indexesCursor.getString(0))
        }
        indexesCursor.close()

        assertTrue("Should have idx_financial_user_rekap index", indexNames.contains("idx_financial_user_rekap"))
    }

    @Test
    fun `migration4 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4)

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
    fun `migration6 should create composite index on transactions table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6)

        val indexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val indexNames = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            indexNames.add(indexesCursor.getString(0))
        }
        indexesCursor.close()

        assertTrue("Should have idx_transactions_status_deleted index", indexNames.contains("idx_transactions_status_deleted"))
    }

    @Test
    fun `migration6 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5)
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6)

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should be preserved", 1, userCount)
        assertEquals("Active financial records should be preserved", 1, financialCount)
        assertEquals("Active transactions should be preserved", 1, transactionCount)
        assertEquals("Webhook events should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration7 should create partial indexes on users table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7)

        val indexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='users'")
        val partialIndexes = mutableListOf<String>()
        val fullIndexes = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            val indexName = indexesCursor.getString(0)
            val sql = indexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                partialIndexes.add(indexName)
            } else if (!sql.isNullOrEmpty() && !indexName.startsWith("sqlite_autoindex")) {
                fullIndexes.add(indexName)
            }
        }
        indexesCursor.close()

        assertTrue("Should have idx_users_active partial index", partialIndexes.contains("idx_users_active"))
        assertTrue("Should have idx_users_active_updated partial index", partialIndexes.contains("idx_users_active_updated"))
    }

    @Test
    fun `migration7 should create partial indexes on financial_records table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7)

        val indexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        val partialIndexes = mutableListOf<String>()
        while (indexesCursor.moveToNext()) {
            val indexName = indexesCursor.getString(0)
            val sql = indexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                partialIndexes.add(indexName)
            }
        }
        indexesCursor.close()

        assertTrue("Should have idx_financial_active partial index", partialIndexes.contains("idx_financial_active"))
        assertTrue("Should have idx_financial_active_user_updated partial index", partialIndexes.contains("idx_financial_active_user_updated"))
        assertTrue("Should have idx_financial_active_updated partial index", partialIndexes.contains("idx_financial_active_updated"))
    }

    @Test
    fun `migration7 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7)

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should be preserved", 1, userCount)
        assertEquals("Active financial records should be preserved", 1, financialCount)
        assertEquals("Active transactions should be preserved", 1, transactionCount)
        assertEquals("Webhook events should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration7Down should drop partial indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7)
        Migration7Down.migrate(db)

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

        assertFalse("idx_users_active should be dropped", userIndexNames.contains("idx_users_active"))
        assertFalse("idx_users_active_updated should be dropped", userIndexNames.contains("idx_users_active_updated"))
        assertFalse("idx_financial_active should be dropped", financialIndexNames.contains("idx_financial_active"))
        assertFalse("idx_financial_active_user_updated should be dropped", financialIndexNames.contains("idx_financial_active_user_updated"))
        assertFalse("idx_financial_active_updated should be dropped", financialIndexNames.contains("idx_financial_active_updated"))
    }

    @Test
    fun `migration7Down should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7)
        Migration7Down.migrate(db)

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should be preserved", 1, userCount)
        assertEquals("Active financial records should be preserved", 1, financialCount)
        assertEquals("Active transactions should be preserved", 1, transactionCount)
        assertEquals("Webhook events should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration8 should drop duplicate full indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8)

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

        assertFalse("idx_financial_records_user_id_updated_at should be dropped", financialIndexNames.contains("idx_financial_records_user_id_updated_at"))
        assertFalse("idx_financial_records_updated_at should be dropped", financialIndexNames.contains("idx_financial_records_updated_at"))
        assertFalse("idx_financial_records_id should be dropped", financialIndexNames.contains("idx_financial_records_id"))
        assertFalse("idx_financial_records_updated_at_2 should be dropped", financialIndexNames.contains("idx_financial_records_updated_at_2"))
    }

    @Test
    fun `migration8 should recreate partial indexes correctly`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8)

        val usersIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='users'")
        val userPartialIndexes = mutableListOf<String>()
        while (usersIndexesCursor.moveToNext()) {
            val indexName = usersIndexesCursor.getString(0)
            val sql = usersIndexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                userPartialIndexes.add(indexName)
            }
        }
        usersIndexesCursor.close()

        val financialIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        val financialPartialIndexes = mutableListOf<String>()
        while (financialIndexesCursor.moveToNext()) {
            val indexName = financialIndexesCursor.getString(0)
            val sql = financialIndexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                financialPartialIndexes.add(indexName)
            }
        }
        financialIndexesCursor.close()

        assertTrue("Should have idx_users_active partial index", userPartialIndexes.contains("idx_users_active"))
        assertTrue("Should have idx_users_active_updated partial index", userPartialIndexes.contains("idx_users_active_updated"))
        assertTrue("Should have idx_financial_active partial index", financialPartialIndexes.contains("idx_financial_active"))
        assertTrue("Should have idx_financial_active_user_updated partial index", financialPartialIndexes.contains("idx_financial_active_user_updated"))
        assertTrue("Should have idx_financial_active_updated partial index", financialPartialIndexes.contains("idx_financial_active_updated"))
    }

    @Test
    fun `migration8 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8)

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should be preserved", 1, userCount)
        assertEquals("Active financial records should be preserved", 1, financialCount)
        assertEquals("Active transactions should be preserved", 1, transactionCount)
        assertEquals("Webhook events should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration8Down should recreate full indexes`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8)
        Migration8Down.migrate(db)

        val usersIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='users'")
        val usersHaveFullIndexes = false
        while (usersIndexesCursor.moveToNext()) {
            val sql = usersIndexesCursor.getString(1)
            if (sql != null && !sql.contains("WHERE") && sql.contains("idx_users_active")) {
                usersHaveFullIndexes = true
                break
            }
        }
        usersIndexesCursor.close()

        val financialIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        val financialHaveFullIndexes = false
        while (financialIndexesCursor.moveToNext()) {
            val sql = financialIndexesCursor.getString(1)
            if (sql != null && !sql.contains("WHERE") && sql.contains("idx_financial")) {
                financialHaveFullIndexes = true
                break
            }
        }
        financialIndexesCursor.close()

        assertTrue("Should have full indexes on users table", usersHaveFullIndexes)
        assertTrue("Should have full indexes on financial_records table", financialHaveFullIndexes)
    }

    @Test
    fun `migration8Down should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8)
        Migration8Down.migrate(db)

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should be preserved", 1, userCount)
        assertEquals("Active financial records should be preserved", 1, financialCount)
        assertEquals("Active transactions should be preserved", 1, transactionCount)
        assertEquals("Webhook events should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration9 should drop full indexes on transactions table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8)
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8, Migration9)

        val transactionIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val indexNames = mutableListOf<String>()
        while (transactionIndexesCursor.moveToNext()) {
            indexNames.add(transactionIndexesCursor.getString(0))
        }
        transactionIndexesCursor.close()

        assertFalse("index_transactions_user_id should be dropped", indexNames.contains("index_transactions_user_id"))
        assertFalse("index_transactions_status should be dropped", indexNames.contains("index_transactions_status"))
        assertFalse("index_transactions_user_id_status should be dropped", indexNames.contains("index_transactions_user_id_status"))
        assertFalse("index_transactions_created_at should be dropped", indexNames.contains("index_transactions_created_at"))
        assertFalse("index_transactions_updated_at should be dropped", indexNames.contains("index_transactions_updated_at"))
    }

    @Test
    fun `migration9 should create partial indexes on transactions table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8, Migration9)

        val transactionIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val partialIndexes = mutableListOf<String>()
        while (transactionIndexesCursor.moveToNext()) {
            val indexName = transactionIndexesCursor.getString(0)
            val sql = transactionIndexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                partialIndexes.add(indexName)
            }
        }
        transactionIndexesCursor.close()

        assertTrue("Should have idx_transactions_user_id partial index", partialIndexes.contains("idx_transactions_user_id"))
        assertTrue("Should have idx_transactions_status partial index", partialIndexes.contains("idx_transactions_status"))
        assertTrue("Should have idx_transactions_user_status partial index", partialIndexes.contains("idx_transactions_user_status"))
        assertTrue("Should have idx_transactions_created partial index", partialIndexes.contains("idx_transactions_created"))
        assertTrue("Should have idx_transactions_updated partial index", partialIndexes.contains("idx_transactions_updated"))
    }

    @Test
    fun `migration9 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8, Migration9)

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should be preserved", 1, userCount)
        assertEquals("Active financial records should be preserved", 1, financialCount)
        assertEquals("Active transactions should be preserved", 1, transactionCount)
        assertEquals("Webhook events should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration9Down should recreate full indexes on transactions table`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8, Migration9)
        Migration9Down.migrate(db)

        val transactionIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val hasFullIndexes = false
        while (transactionIndexesCursor.moveToNext()) {
            val sql = transactionIndexesCursor.getString(1)
            if (sql != null && !sql.contains("WHERE") && sql.contains("index_transactions")) {
                hasFullIndexes = true
                break
            }
        }
        transactionIndexesCursor.close()

        assertTrue("Should have full indexes on transactions table", hasFullIndexes)
    }

    @Test
    fun `migration9Down should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")
        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8, Migration9)
        Migration9Down.migrate(db)

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should be preserved", 1, userCount)
        assertEquals("Active financial records should be preserved", 1, financialCount)
        assertEquals("Active transactions should be preserved", 1, transactionCount)
        assertEquals("Webhook events should be preserved", 1, webhookCount)
    }

    @Test
    fun `migration1_2_3_4_5_6_7_8_9_full_migration_sequence_should_work`() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration1(), Migration1Down, Migration2, Migration3, Migration4, Migration5, Migration6, Migration7, Migration8, Migration9)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, status) VALUES ('whk_1234567890_abc', 'payment.completed', '{\"test\":\"data\"}', 'PENDING')")
        db.execSQL("INSERT INTO transactions (user_id, amount, status, payment_method) VALUES (1, 10000, 'COMPLETED', 'GOPAY')")

        val userCursor = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        val financialCursor = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
        financialCursor.moveToFirst()
        val financialCount = financialCursor.getInt(0)
        financialCursor.close()

        val transactionCursor = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
        transactionCursor.moveToFirst()
        val transactionCount = transactionCursor.getInt(0)
        transactionCursor.close()

        val webhookCursor = db.query("SELECT COUNT(*) FROM webhook_events")
        webhookCursor.moveToFirst()
        val webhookCount = webhookCursor.getInt(0)
        webhookCursor.close()

        assertEquals("Active users should exist", 1, userCount)
        assertEquals("Active financial records should exist", 1, financialCount)
        assertEquals("Active transactions should exist", 1, transactionCount)
        assertEquals("Webhook events should exist", 1, webhookCount)

        val usersIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='users'")
        var userPartialIndexCount = 0
        while (usersIndexesCursor.moveToNext()) {
            val sql = usersIndexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                userPartialIndexCount++
            }
        }
        usersIndexesCursor.close()

        val financialIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'")
        var financialPartialIndexCount = 0
        while (financialIndexesCursor.moveToNext()) {
            val sql = financialIndexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                financialPartialIndexCount++
            }
        }
        financialIndexesCursor.close()

        val transactionIndexesCursor = db.query("SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        var transactionPartialIndexCount = 0
        while (transactionIndexesCursor.moveToNext()) {
            val sql = transactionIndexesCursor.getString(1)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                transactionPartialIndexCount++
            }
        }
        transactionIndexesCursor.close()

        assertTrue("Users table should have partial indexes", userPartialIndexCount > 0)
        assertTrue("Financial records table should have partial indexes", financialPartialIndexCount > 0)
        assertTrue("Transactions table should have partial indexes", transactionPartialIndexCount > 0)
    }

    // ===== Migration 11 Tests (Partial Indexes) =====

    @Test
    fun `migration11 should create partial index for users email`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg', 0)")
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted) VALUES ('deleted@example.com', 'Jane', 'Smith', '456 Oak Ave', 'https://example.com/avatar2.jpg', 1)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())

        val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='index' AND name='idx_users_email_active'")
        var partialIndexFound = false
        while (cursor.moveToNext()) {
            val sql = cursor.getString(0)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                partialIndexFound = true
            }
        }
        cursor.close()

        assertTrue("Should create partial index for users email (WHERE is_deleted = 0)", partialIndexFound)
    }

    @Test
    fun `migration11 should create partial index for users name sort`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())

        val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='index' AND name='idx_users_name_sort_active'")
        var partialIndexFound = false
        while (cursor.moveToNext()) {
            val sql = cursor.getString(0)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                partialIndexFound = true
            }
        }
        cursor.close()

        assertTrue("Should create partial index for users name sort (WHERE is_deleted = 0)", partialIndexFound)
    }

    @Test
    fun `migration11 should create partial index for financial records user and updated_at`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance', 0)")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (1, 150, 250, 350, 450, 550, 'Repair', 1)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())

        val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='index' AND name='idx_financial_user_updated_active'")
        var partialIndexFound = false
        while (cursor.moveToNext()) {
            val sql = cursor.getString(0)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                partialIndexFound = true
            }
        }
        cursor.close()

        assertTrue("Should create partial index for financial records user and updated_at (WHERE is_deleted = 0)", partialIndexFound)
    }

    @Test
    fun `migration11 should create partial index for transactions user_id`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('txn_123', 1, 1000.00, 'IDR', 'PENDING', 'CREDIT_CARD', 'Test payment', 0)")
        db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('txn_456', 1, 2000.00, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test payment 2', 1)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())

        val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='index' AND name='idx_transactions_user_active'")
        var partialIndexFound = false
        while (cursor.moveToNext()) {
            val sql = cursor.getString(0)
            if (sql != null && sql.contains("WHERE is_deleted = 0")) {
                partialIndexFound = true
            }
        }
        cursor.close()

        assertTrue("Should create partial index for transactions user_id (WHERE is_deleted = 0)", partialIndexFound)
    }

    @Test
    fun `migration11 should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg', 0)")
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted) VALUES ('deleted@example.com', 'Jane', 'Smith', '456 Oak Ave', 'https://example.com/avatar2.jpg', 1)")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance', 0)")
        db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('txn_123', 1, 1000.00, 'IDR', 'PENDING', 'CREDIT_CARD', 'Test payment', 0)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())

        val activeUserCount = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        val deletedUserCount = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 1").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        val activeFinancialCount = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        val activeTransactionCount = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        assertEquals("Active user data should be preserved", 1, activeUserCount)
        assertEquals("Deleted user data should be preserved", 1, deletedUserCount)
        assertEquals("Active financial record data should be preserved", 1, activeFinancialCount)
        assertEquals("Active transaction data should be preserved", 1, activeTransactionCount)
    }

    @Test
    fun `migration11Down should drop all partial indexes`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())
        Migration11Down.migrate(db)

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

        val transactionsIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val transactionIndexNames = mutableListOf<String>()
        while (transactionsIndexesCursor.moveToNext()) {
            transactionIndexNames.add(transactionsIndexesCursor.getString(0))
        }
        transactionsIndexesCursor.close()

        assertFalse("Should not have idx_users_email_active index", userIndexNames.contains("idx_users_email_active"))
        assertFalse("Should not have idx_users_name_sort_active index", userIndexNames.contains("idx_users_name_sort_active"))
        assertFalse("Should not have idx_users_id_active index", userIndexNames.contains("idx_users_id_active"))
        assertFalse("Should not have idx_users_updated_at_active index", userIndexNames.contains("idx_users_updated_at_active"))

        assertFalse("Should not have idx_financial_user_updated_active index", financialIndexNames.contains("idx_financial_user_updated_active"))
        assertFalse("Should not have idx_financial_id_active index", financialIndexNames.contains("idx_financial_id_active"))
        assertFalse("Should not have idx_financial_pemanfaatan_active index", financialIndexNames.contains("idx_financial_pemanfaatan_active"))

        assertFalse("Should not have idx_transactions_user_active index", transactionIndexNames.contains("idx_transactions_user_active"))
        assertFalse("Should not have idx_transactions_status_active index", transactionIndexNames.contains("idx_transactions_status_active"))
        assertFalse("Should not have idx_transactions_user_status_active index", transactionIndexNames.contains("idx_transactions_user_status_active"))
        assertFalse("Should not have idx_transactions_created_at_active index", transactionIndexNames.contains("idx_transactions_created_at_active"))
    }

    @Test
    fun `migration11Down should preserve existing data`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg', 0)")
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted) VALUES ('deleted@example.com', 'Jane', 'Smith', '456 Oak Ave', 'https://example.com/avatar2.jpg', 1)")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance', 0)")
        db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('txn_123', 1, 1000.00, 'IDR', 'PENDING', 'CREDIT_CARD', 'Test payment', 0)")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())
        Migration11Down.migrate(db)

        val activeUserCount = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        val deletedUserCount = db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 1").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        val activeFinancialCount = db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        val activeTransactionCount = db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        assertEquals("Active user data should be preserved", 1, activeUserCount)
        assertEquals("Deleted user data should be preserved", 1, deletedUserCount)
        assertEquals("Active financial record data should be preserved", 1, activeFinancialCount)
        assertEquals("Active transaction data should be preserved", 1, activeTransactionCount)
    }

    @Test
    fun `migration11Down should preserve base indexes`() {
        var db = helper.createDatabase(TEST_DB, 10)
        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar) VALUES ('test@example.com', 'John', 'Doe', '123 Main St', 'https://example.com/avatar.jpg')")
        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran) VALUES (1, 100, 200, 300, 400, 500, 'Maintenance')")
        db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description) VALUES ('txn_123', 1, 1000.00, 'IDR', 'PENDING', 'CREDIT_CARD', 'Test payment')")
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration1(), Migration1Down, Migration2, Migration2Down, Migration3, Migration3Down, Migration4, Migration4Down, Migration5, Migration5Down, Migration6, Migration6Down, Migration7, Migration7Down, Migration8, Migration8Down, Migration9, Migration9Down, Migration10, Migration10Down, Migration11())
        Migration11Down.migrate(db)

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

        val transactionsIndexesCursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'")
        val transactionIndexNames = mutableListOf<String>()
        while (transactionsIndexesCursor.moveToNext()) {
            transactionIndexNames.add(transactionsIndexesCursor.getString(0))
        }
        transactionsIndexesCursor.close()

        assertTrue("Should preserve users email index", userIndexNames.any { it.contains("email") })
        assertTrue("Should preserve users name sort index", userIndexNames.any { it.contains("last_name") && it.contains("first_name") })

        assertTrue("Should preserve financial user_updated index", financialIndexNames.any { it.contains("user_id") && it.contains("updated_at") })
        assertTrue("Should preserve financial user_rekap index", financialIndexNames.any { it.contains("user_id") && it.contains("total_iuran_rekap") })

        assertTrue("Should preserve transactions user_id index", transactionIndexNames.any { it.contains("user_id") })
        assertTrue("Should preserve transactions status index", transactionIndexNames.any { it.contains("status") })
        assertTrue("Should preserve transactions user_status index", transactionIndexNames.any { it.contains("user_id") && it.contains("status") })
        assertTrue("Should preserve transactions created_at index", transactionIndexNames.any { it.contains("created_at") })
        assertTrue("Should preserve transactions updated_at index", transactionIndexNames.any { it.contains("updated_at") })
    }
}
