package com.example.iurankomplek.data.database

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration12Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        ApplicationProvider.getApplicationContext<Context>(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun setUp() {
        helper.createDatabase(TEST_DB, 11).apply {
            execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('test@example.com', 'Test', 'User', 'Test Address', 'avatar.png', 0, strftime('%s', 'now'), strftime('%s', 'now'))")
            execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, 1000, 12000, 3000, 500, 15500, 'Test Pemanfaatan', 0, strftime('%s', 'now'), strftime('%s', 'now'))")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('txn_test_001', 1, 100000, 'IDR', 'PENDING', 'GOPAY', 'Test Transaction', 0, strftime('%s', 'now'), strftime('%s', 'now'), '')")
            execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, transaction_id, status, retry_count, max_retries, next_retry_at, delivered_at, created_at, updated_at, last_error) VALUES ('idempotency_001', 'PAYMENT_SUCCESS', '{\"test\":\"payload\"}', 'txn_test_001', 'PENDING', 0, 3, NULL, NULL, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, NULL)")
            close()
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        helper.closeAllDatabases()
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_createsFinancialUpdatedDescIndex() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_financial_updated_desc_active'").use { cursor ->
            assertTrue("Index idx_financial_updated_desc_active should exist", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_createsWebhookStatusCreatedIndex() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_status_created'").use { cursor ->
            assertTrue("Index idx_webhook_status_created should exist", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_createsWebhookTransactionCreatedIndex() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_transaction_created'").use { cursor ->
            assertTrue("Index idx_webhook_transaction_created should exist", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_createsWebhookTypeCreatedIndex() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_type_created'").use { cursor ->
            assertTrue("Index idx_webhook_type_created should exist", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_indexesWorkCorrectly() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.query("SELECT * FROM financial_records WHERE is_deleted = 0 ORDER BY updated_at DESC").use { cursor ->
            assertTrue("Query should return results", cursor.count > 0)
        }

        db.query("SELECT * FROM webhook_events WHERE status = 'PENDING' ORDER BY created_at ASC").use { cursor ->
            assertTrue("Query should return results", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_preservesExistingData() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.query("SELECT * FROM users").use { cursor ->
            assertTrue("Users should exist", cursor.count > 0)
        }

        db.query("SELECT * FROM financial_records").use { cursor ->
            assertTrue("Financial records should exist", cursor.count > 0)
        }

        db.query("SELECT * FROM transactions").use { cursor ->
            assertTrue("Transactions should exist", cursor.count > 0)
        }

        db.query("SELECT * FROM webhook_events").use { cursor ->
            assertTrue("Webhook events should exist", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_handlesEmptyDatabase() {
        helper.createDatabase(TEST_DB, 11).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_financial_updated_desc_active'").use { cursor ->
            assertTrue("Index should exist even on empty database", cursor.count > 0)
        }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_status_created'").use { cursor ->
            assertTrue("Index should exist even on empty database", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_preservesExistingIndexes() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        val indexNames = mutableListOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'").use { cursor ->
            while (cursor.moveToNext()) {
                indexNames.add(cursor.getString(0))
            }
        }

        assertTrue("Migration 11 indexes should still exist", indexNames.any { it.contains("idx_financial_user_updated") })
        assertTrue("Migration 11 indexes should still exist", indexNames.any { it.contains("idx_financial_id_active") })
    }

    @Test
    @Throws(IOException::class)
    fun migrate12To11_dropsAllNewIndexes() {
        helper.createDatabase(TEST_DB, 12).apply {
            execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('test2@example.com', 'Test2', 'User2', 'Test Address 2', 'avatar2.png', 0, strftime('%s', 'now'), strftime('%s', 'now'))")
            execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (2, 2000, 24000, 6000, 1000, 31000, 'Test Pemanfaatan 2', 0, strftime('%s', 'now'), strftime('%s', 'now'))")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migration12Down)

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_financial_updated_desc_active'").use { cursor ->
            assertTrue("Index idx_financial_updated_desc_active should not exist after down migration", cursor.count == 0)
        }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_status_created'").use { cursor ->
            assertTrue("Index idx_webhook_status_created should not exist after down migration", cursor.count == 0)
        }

        db.query("SELECT * FROM users WHERE email = 'test2@example.com'").use { cursor ->
            assertTrue("Data should be preserved after down migration", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_indexPerformanceTest() {
        helper.createDatabase(TEST_DB, 11).apply {
            for (i in 1..100) {
                execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, $i * 100, ${i * 1000}, ${i * 500}, ${i * 200}, ${i * 1700}, 'Pemanfaatan $i', 0, strftime('%s', 'now') - $i, strftime('%s', 'now') - $i)")
            }
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        val startTime = System.currentTimeMillis()
        db.query("SELECT * FROM financial_records WHERE is_deleted = 0 ORDER BY updated_at DESC LIMIT 50").use { cursor ->
            cursor.count
        }
        val endTime = System.currentTimeMillis()

        val queryTime = endTime - startTime

        assertTrue("Query with index should be fast", queryTime < 100)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_supportsInsertAfterMigration() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, 2000, 24000, 6000, 1000, 31000, 'New Pemanfaatan', 0, strftime('%s', 'now'), strftime('%s', 'now'))")

        db.query("SELECT * FROM financial_records WHERE pemanfaatan_iuran = 'New Pemanfaatan'").use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_supportsUpdateAfterMigration() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.execSQL("UPDATE financial_records SET total_iuran_rekap = 20000 WHERE user_id = 1")

        db.query("SELECT * FROM financial_records WHERE user_id = 1 AND total_iuran_rekap = 20000").use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_supportsDeleteAfterMigration() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, transaction_id, status, retry_count, max_retries, next_retry_at, delivered_at, created_at, updated_at, last_error) VALUES ('delete_test', 'DELETE_TEST', '{\"test\":\"payload\"}', 'delete_txn', 'PENDING', 0, 3, NULL, NULL, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, NULL)")

        db.execSQL("DELETE FROM webhook_events WHERE idempotency_key = 'delete_test'")

        db.query("SELECT * FROM webhook_events WHERE idempotency_key = 'delete_test'").use { cursor ->
            assertEquals(0, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12_partialIndexesFilterDeletedRecords() {
        helper.createDatabase(TEST_DB, 11).apply {
            execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('deleted@example.com', 'Deleted', 'User', 'Deleted Address', 'deleted.png', 1, strftime('%s', 'now'), strftime('%s', 'now'))")
            execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, 1000, 12000, 3000, 500, 15500, 'Deleted Record', 1, strftime('%s', 'now'), strftime('%s', 'now'))")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migration12())

        val activeCount = mutableListOf<Int>()
        db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            activeCount.add(cursor.getInt(0))
        }

        assertTrue("Active records count should be correct", activeCount[0] == 1)
    }
}
