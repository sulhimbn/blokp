package com.example.iurankomplek.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.payment.WebhookDeliveryStatus
import com.example.iurankomplek.payment.WebhookEvent
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration10Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        ApplicationProvider.getApplicationContext<Context>(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun setUp() {
        helper.createDatabase(TEST_DB, 9).apply {
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
    fun migrate9To10_createsTransactionIdIndex() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_events_transaction_id'").use { cursor ->
            assertTrue("Index idx_webhook_events_transaction_id should exist", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_indexWorksCorrectly() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        db.query("SELECT * FROM webhook_events WHERE transaction_id = 'txn_test_001'").use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_preservesExistingData() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

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
    fun migrate9To10_handlesEmptyDatabase() {
        helper.createDatabase(TEST_DB, 9).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_events_transaction_id'").use { cursor ->
            assertTrue("Index should exist even on empty database", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_preservesExistingIndexes() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        val indexNames = mutableListOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'").use { cursor ->
            while (cursor.moveToNext()) {
                indexNames.add(cursor.getString(0))
            }
        }

        assertTrue("idempotency_key index should exist", indexNames.contains("index_webhook_events_idempotency_key"))
        assertTrue("status index should exist", indexNames.contains("index_webhook_events_status"))
        assertTrue("event_type index should exist", indexNames.contains("index_webhook_events_event_type"))
        assertTrue("status, next_retry_at composite index should exist", indexNames.contains("index_webhook_events_status_next_retry_at"))
        assertTrue("transaction_id index should exist", indexNames.contains("idx_webhook_events_transaction_id"))
    }

    @Test
    @Throws(IOException::class)
    fun migrate10To9_dropsTransactionIdIndex() {
        helper.createDatabase(TEST_DB, 10).apply {
            execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('test2@example.com', 'Test2', 'User2', 'Test Address 2', 'avatar2.png', 0, strftime('%s', 'now'), strftime('%s', 'now'))")
            execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, transaction_id, status, retry_count, max_retries, next_retry_at, delivered_at, created_at, updated_at, last_error) VALUES ('idempotency_002', 'PAYMENT_FAILED', '{\"test\":\"payload2\"}', 'txn_test_002', 'FAILED', 1, 3, NULL, NULL, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, 'Test error')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration10Down)

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_webhook_events_transaction_id'").use { cursor ->
            assertTrue("Index idx_webhook_events_transaction_id should not exist after down migration", cursor.count == 0)
        }

        db.query("SELECT * FROM users WHERE email = 'test2@example.com'").use { cursor ->
            assertTrue("Data should be preserved after down migration", cursor.count > 0)
        }

        db.query("SELECT * FROM webhook_events WHERE idempotency_key = 'idempotency_002'").use { cursor ->
            assertTrue("Webhook events should be preserved after down migration", cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_indexPerformanceTest() {
        helper.createDatabase(TEST_DB, 9).apply {
            for (i in 1..100) {
                execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, transaction_id, status, retry_count, max_retries, next_retry_at, delivered_at, created_at, updated_at, last_error) VALUES ('idempotency_$i', 'TEST_EVENT', '{\"test\":\"payload$i\"}', 'txn_$i', 'PENDING', 0, 3, NULL, NULL, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, NULL)")
            }
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        val startTime = System.currentTimeMillis()
        db.query("SELECT * FROM webhook_events WHERE transaction_id = 'txn_50'").use { cursor ->
            cursor.count
        }
        val endTime = System.currentTimeMillis()

        val queryTime = endTime - startTime

        assertTrue("Query with index should be fast", queryTime < 100)
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_supportsInsertAfterMigration() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, transaction_id, status, retry_count, max_retries, next_retry_at, delivered_at, created_at, updated_at, last_error) VALUES ('new_idempotency', 'NEW_EVENT', '{\"test\":\"new_payload\"}', 'new_txn_001', 'PENDING', 0, 3, NULL, NULL, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, NULL)")

        db.query("SELECT * FROM webhook_events WHERE transaction_id = 'new_txn_001'").use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_supportsUpdateAfterMigration() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        db.execSQL("UPDATE webhook_events SET status = 'DELIVERED', delivered_at = strftime('%s', 'now') * 1000 WHERE transaction_id = 'txn_test_001'")

        db.query("SELECT * FROM webhook_events WHERE transaction_id = 'txn_test_001' AND status = 'DELIVERED'").use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10_supportsDeleteAfterMigration() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, Migration10)

        db.execSQL("INSERT INTO webhook_events (idempotency_key, event_type, payload, transaction_id, status, retry_count, max_retries, next_retry_at, delivered_at, created_at, updated_at, last_error) VALUES ('delete_test', 'DELETE_TEST', '{\"test\":\"payload\"}', 'delete_txn', 'PENDING', 0, 3, NULL, NULL, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, NULL)")

        db.execSQL("DELETE FROM webhook_events WHERE transaction_id = 'delete_txn'")

        db.query("SELECT * FROM webhook_events WHERE transaction_id = 'delete_txn'").use { cursor ->
            assertEquals(0, cursor.count)
        }
    }
}
