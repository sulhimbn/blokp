package com.example.iurankomplek.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.payment.WebhookEvent
import com.example.iurankomplek.payment.WebhookDeliveryStatus
import com.example.iurankomplek.payment.WebhookEventDao
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class Migration23Test {

    private lateinit var helper: MigrationTestHelper
    private val TEST_DB = "migration-test"
    private val MIGRATION_22_23 = Migration23()
    private val MIGRATION_23_22 = Migration23Down()

    @Before
    fun setUp() {
        helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @After
    fun tearDown() {
        helper.closeAllDatabases()
    }

    @Test
    fun migrate22To23_success() {
        // Step 1: Create database with version 22 (before migration)
        val dbV22 = helper.createDatabase(TEST_DB, 22).apply {
            // Insert test data into webhook_events table (version 22 schema)
            execSQL(
                """
                INSERT INTO webhook_events
                (id, idempotency_key, event_type, payload, transaction_id, status,
                 retry_count, max_retries, next_retry_at, delivered_at,
                 created_at, updated_at, last_error, is_deleted)
                VALUES
                (1, 'key-1', 'payment.completed', '{}', 'txn-123', 'PENDING',
                 0, 3, NULL, NULL,
                 ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, NULL, 0),
                (2, 'key-2', 'payment.completed', '{}', NULL, 'DELIVERED',
                 0, 3, NULL, ${System.currentTimeMillis()},
                 ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, NULL, 0),
                (3, 'key-3', 'payment.failed', '{}', 'txn-456', 'FAILED',
                 2, 3, NULL, NULL,
                 ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 'Timeout', 0)
                """
            )

            // Insert test transaction
            execSQL(
                """
                INSERT INTO transactions
                (id, user_id, amount, currency, status, payment_method, description,
                 is_deleted, created_at, updated_at, metadata)
                VALUES
                ('txn-123', 1, 10000, 'IDR', 'COMPLETED', 'BANK_TRANSFER',
                 'Test payment 1', 0, ${Date().time}, ${Date().time}, '{}'),
                ('txn-456', 2, 20000, 'IDR', 'FAILED', 'E_WALLET',
                 'Test payment 2', 0, ${Date().time}, ${Date().time}, '{}')
                """
            )
        }

        // Step 2: Migrate to version 23
        dbV22.close()
        val dbV23 = helper.runMigrationsAndValidate(TEST_DB, 23, true, MIGRATION_22_23)

        // Step 3: Verify data integrity after migration
        dbV23.query("SELECT COUNT(*) FROM webhook_events").use { cursor ->
            Assert.assertTrue("All webhook events should be preserved", cursor.moveToFirst())
            Assert.assertEquals("Should have 3 webhook events", 3, cursor.getInt(0))
        }

        dbV23.query("SELECT transaction_id FROM webhook_events WHERE id = 1").use { cursor ->
            Assert.assertTrue("Webhook event 1 should exist", cursor.moveToFirst())
            Assert.assertEquals("transaction_id should be preserved", "txn-123", cursor.getString(0))
        }

        dbV23.query("SELECT transaction_id FROM webhook_events WHERE id = 2").use { cursor ->
            Assert.assertTrue("Webhook event 2 should exist", cursor.moveToFirst())
            Assert.assertNull("NULL transaction_id should be preserved", cursor.getString(0))
        }

        dbV23.query("SELECT transaction_id FROM webhook_events WHERE id = 3").use { cursor ->
            Assert.assertTrue("Webhook event 3 should exist", cursor.moveToFirst())
            Assert.assertEquals("transaction_id should be preserved", "txn-456", cursor.getString(0))
        }

        dbV23.close()
    }

    @Test
    fun foreignKeyConstraint_insertInvalidTransactionId_fails() {
        // Create database at version 23
        val dbV23 = helper.createDatabase(TEST_DB, 23).apply {
            // Insert a valid transaction first
            execSQL(
                """
                INSERT INTO transactions
                (id, user_id, amount, currency, status, payment_method, description,
                 is_deleted, created_at, updated_at, metadata)
                VALUES
                ('txn-valid', 1, 10000, 'IDR', 'COMPLETED', 'BANK_TRANSFER',
                 'Valid transaction', 0, ${Date().time}, ${Date().time}, '{}')
                """
            )

            // Insert webhook event with valid transaction_id (should succeed)
            execSQL(
                """
                INSERT INTO webhook_events
                (id, idempotency_key, event_type, payload, transaction_id, status,
                 retry_count, max_retries, next_retry_at, delivered_at,
                 created_at, updated_at, last_error, is_deleted)
                VALUES
                (1, 'key-valid', 'payment.completed', '{}', 'txn-valid', 'PENDING',
                 0, 3, NULL, NULL,
                 ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, NULL, 0)
                """
            )
        }

        dbV23.query("SELECT COUNT(*) FROM webhook_events WHERE transaction_id = 'txn-valid'").use { cursor ->
            Assert.assertTrue("Webhook event with valid transaction should exist", cursor.moveToFirst())
            Assert.assertEquals("Should have 1 webhook event", 1, cursor.getInt(0))
        }

        // Try to insert webhook event with invalid transaction_id (should fail FK constraint)
        var fkViolationCaught = false
        try {
            dbV23.execSQL(
                """
                INSERT INTO webhook_events
                (id, idempotency_key, event_type, payload, transaction_id, status,
                 retry_count, max_retries, next_retry_at, delivered_at,
                 created_at, updated_at, last_error, is_deleted)
                VALUES
                (2, 'key-invalid', 'payment.completed', '{}', 'txn-invalid', 'PENDING',
                 0, 3, NULL, NULL,
                 ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, NULL, 0)
                """
            )
        } catch (e: Exception) {
            fkViolationCaught = true
        }

        Assert.assertTrue("FK violation should be caught", fkViolationCaught)

        dbV23.query("SELECT COUNT(*) FROM webhook_events").use { cursor ->
            Assert.assertTrue("Query should succeed", cursor.moveToFirst())
            Assert.assertEquals("Should still have only 1 webhook event (invalid insert failed)", 1, cursor.getInt(0))
        }

        dbV23.close()
    }

    @Test
    fun foreignKeyConstraint_deleteTransaction_setsNull() {
        // Create database at version 23
        val dbV23 = helper.createDatabase(TEST_DB, 23).apply {
            // Insert test transaction
            execSQL(
                """
                INSERT INTO transactions
                (id, user_id, amount, currency, status, payment_method, description,
                 is_deleted, created_at, updated_at, metadata)
                VALUES
                ('txn-to-delete', 1, 10000, 'IDR', 'COMPLETED', 'BANK_TRANSFER',
                 'Transaction to delete', 0, ${Date().time}, ${Date().time}, '{}')
                """
            )

            // Insert webhook event referencing the transaction
            execSQL(
                """
                INSERT INTO webhook_events
                (id, idempotency_key, event_type, payload, transaction_id, status,
                 retry_count, max_retries, next_retry_at, delivered_at,
                 created_at, updated_at, last_error, is_deleted)
                VALUES
                (1, 'key-test', 'payment.completed', '{}', 'txn-to-delete', 'DELIVERED',
                 0, 3, NULL, ${System.currentTimeMillis()},
                 ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, NULL, 0)
                """
            )
        }

        // Verify initial state
        dbV23.query("SELECT transaction_id FROM webhook_events WHERE id = 1").use { cursor ->
            Assert.assertTrue("Webhook event should exist", cursor.moveToFirst())
            Assert.assertEquals("transaction_id should be set initially", "txn-to-delete", cursor.getString(0))
        }

        // Delete the transaction (should set webhook event's transaction_id to NULL)
        dbV23.execSQL("DELETE FROM transactions WHERE id = 'txn-to-delete'")

        // Verify ON DELETE SET NULL behavior
        dbV23.query("SELECT transaction_id FROM webhook_events WHERE id = 1").use { cursor ->
            Assert.assertTrue("Webhook event should still exist", cursor.moveToFirst())
            Assert.assertNull("transaction_id should be set to NULL after transaction delete", cursor.getString(0))
        }

        dbV23.close()
    }

    @Test
    fun migrate23To22_success() {
        // Create database at version 23 with FK constraint
        val dbV23 = helper.createDatabase(TEST_DB, 23).apply {
            // Insert test data
            execSQL(
                """
                INSERT INTO webhook_events
                (id, idempotency_key, event_type, payload, transaction_id, status,
                 retry_count, max_retries, next_retry_at, delivered_at,
                 created_at, updated_at, last_error, is_deleted)
                VALUES
                (1, 'key-1', 'payment.completed', '{}', 'txn-123', 'PENDING',
                 0, 3, NULL, NULL,
                 ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, NULL, 0)
                """
            )
        }

        // Migrate back to version 22
        dbV23.close()
        val dbV22 = helper.runMigrationsAndValidate(TEST_DB, 22, false, MIGRATION_23_22)

        // Verify data integrity after rollback
        dbV22.query("SELECT COUNT(*) FROM webhook_events").use { cursor ->
            Assert.assertTrue("All webhook events should be preserved", cursor.moveToFirst())
            Assert.assertEquals("Should have 1 webhook event", 1, cursor.getInt(0))
        }

        dbV22.query("SELECT transaction_id FROM webhook_events WHERE id = 1").use { cursor ->
            Assert.assertTrue("Webhook event should exist", cursor.moveToFirst())
            Assert.assertEquals("transaction_id should be preserved", "txn-123", cursor.getString(0))
        }

        dbV22.close()
    }

    @Test
    fun indexesCreated_correctly() {
        // Create database at version 23
        val dbV23 = helper.createDatabase(TEST_DB, 23)

        // Get list of indexes on webhook_events table
        val indexes = mutableListOf<String>()
        dbV23.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'").use { cursor ->
            while (cursor.moveToNext()) {
                indexes.add(cursor.getString(0))
            }
        }

        // Verify all expected indexes exist
        Assert.assertTrue("index_webhook_events_idempotency_key should exist", indexes.contains("index_webhook_events_idempotency_key"))
        Assert.assertTrue("index_webhook_events_transaction_id should exist", indexes.contains("index_webhook_events_transaction_id"))
        Assert.assertTrue("index_webhook_events_status should exist", indexes.contains("index_webhook_events_status"))
        Assert.assertTrue("index_webhook_events_event_type should exist", indexes.contains("index_webhook_events_event_type"))
        Assert.assertTrue("index_webhook_events_status_next_retry_at should exist", indexes.contains("index_webhook_events_status_next_retry_at"))
        Assert.assertTrue("idx_webhook_events_active should exist", indexes.contains("idx_webhook_events_active"))
        Assert.assertTrue("idx_webhook_events_status_retry_active should exist", indexes.contains("idx_webhook_events_status_retry_active"))
        Assert.assertTrue("idx_webhook_events_status_created_active should exist", indexes.contains("idx_webhook_events_status_created_active"))
        Assert.assertTrue("idx_webhook_events_transaction_created_active should exist", indexes.contains("idx_webhook_events_transaction_created_active"))
        Assert.assertTrue("idx_webhook_events_type_created_active should exist", indexes.contains("idx_webhook_events_type_created_active"))
        Assert.assertTrue("idx_webhook_events_status_delivered_active should exist", indexes.contains("idx_webhook_events_status_delivered_active"))
        Assert.assertTrue("idx_webhook_events_status_failed_active should exist", indexes.contains("idx_webhook_events_status_failed_active"))
        Assert.assertTrue("idx_webhook_events_idempotency_key_active should exist", indexes.contains("idx_webhook_events_idempotency_key_active"))

        dbV23.close()
    }
}
