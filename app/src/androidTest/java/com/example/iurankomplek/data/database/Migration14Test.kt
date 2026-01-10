package com.example.iurankomplek.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.iurankomplek.payment.WebhookDeliveryStatus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test suite for Migration 14: Add CHECK Constraints to Webhook Events Table
 *
 * Tests verify:
 * 1. Migration completes successfully
 * 2. Valid data is preserved through migration
 * 3. CHECK constraints are enforced (invalid data rejected)
 * 4. Indexes are recreated correctly
 * 5. Down migration is reversible
 */
@RunWith(AndroidJUnit4::class)
class Migration14Test {

    private lateinit var migrationTestHelper: MigrationTestHelper
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        migrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName
        )
    }

    @After
    fun tearDown() {
        database?.close()
    }

    @Test
    fun migrateFrom13To14_validDataPreserved() {
        // Given: Database with valid webhook event data
        database = migrationTestHelper.createDatabase(TEST_DB, 13).apply {
            execSQL(
                """
                INSERT INTO webhook_events (
                    idempotency_key, event_type, payload, transaction_id,
                    status, retry_count, max_retries, next_retry_at,
                    delivered_at, created_at, updated_at, last_error
                )
                VALUES (
                    'key_123', 'PAYMENT_SUCCESS', '{"amount": 50000}',
                    'txn_abc', 'DELIVERED', 1, 3,
                    strftime('%s', 'now'), strftime('%s', 'now'),
                    strftime('%s', 'now'), strftime('%s', 'now'), NULL
                )
                """.trimIndent()
            )

            execSQL(
                """
                INSERT INTO webhook_events (
                    idempotency_key, event_type, payload, transaction_id,
                    status, retry_count, max_retries, next_retry_at,
                    delivered_at, created_at, updated_at, last_error
                )
                VALUES (
                    'key_456', 'PAYMENT_FAILED', '{"error": "timeout"}',
                    'txn_def', 'FAILED', 3, 3,
                    NULL, NULL, strftime('%s', 'now'),
                    strftime('%s', 'now'), 'Connection timeout'
                )
                """.trimIndent()
            )
        }

        // When: Migrate from version 13 to 14
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // Then: Valid data should be preserved
        val cursor = database.query("SELECT * FROM webhook_events ORDER BY id")
        cursor.moveToFirst()

        // Verify first event
        assertEquals("key_123", cursor.getString(cursor.getColumnIndexOrThrow("idempotency_key")))
        assertEquals("PAYMENT_SUCCESS", cursor.getString(cursor.getColumnIndexOrThrow("event_type")))
        assertEquals("txn_abc", cursor.getString(cursor.getColumnIndexOrThrow("transaction_id")))
        assertEquals("DELIVERED", cursor.getString(cursor.getColumnIndexOrThrow("status")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("retry_count")))
        assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("max_retries")))
        assertNotNull(cursor.getLong(cursor.getColumnIndexOrThrow("delivered_at")))
        assertNull(cursor.getString(cursor.getColumnIndexOrThrow("last_error")))

        cursor.moveToNext()

        // Verify second event
        assertEquals("key_456", cursor.getString(cursor.getColumnIndexOrThrow("idempotency_key")))
        assertEquals("PAYMENT_FAILED", cursor.getString(cursor.getColumnIndexOrThrow("event_type")))
        assertEquals("txn_def", cursor.getString(cursor.getColumnIndexOrThrow("transaction_id")))
        assertEquals("FAILED", cursor.getString(cursor.getColumnIndexOrThrow("status")))
        assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("retry_count")))
        assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("max_retries")))
        assertNull(cursor.getLong(cursor.getColumnIndexOrThrow("delivered_at")))
        assertEquals("Connection timeout", cursor.getString(cursor.getColumnIndexOrThrow("last_error")))

        cursor.close()
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_idempotencyKeyCannotBeEmpty() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Try to insert webhook event with empty idempotency key
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO webhook_events (
                idempotency_key, event_type, payload, status,
                retry_count, max_retries, created_at, updated_at
            )
            VALUES (
                '', 'TEST_EVENT', '{}', 'PENDING', 0, 3,
                strftime('%s', 'now'), strftime('%s', 'now')
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_statusMustBeValidEnum() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Try to insert webhook event with invalid status
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO webhook_events (
                idempotency_key, event_type, payload, status,
                retry_count, max_retries, created_at, updated_at
            )
            VALUES (
                'key_invalid', 'TEST_EVENT', '{}', 'INVALID_STATUS', 0, 3,
                strftime('%s', 'now'), strftime('%s', 'now')
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_retryCountCannotBeNegative() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Try to insert webhook event with negative retry count
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO webhook_events (
                idempotency_key, event_type, payload, status,
                retry_count, max_retries, created_at, updated_at
            )
            VALUES (
                'key_negative', 'TEST_EVENT', '{}', 'PENDING', -1, 3,
                strftime('%s', 'now'), strftime('%s', 'now')
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_retryCountCannotExceedMax() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Try to insert webhook event with retry count > max_retries
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO webhook_events (
                idempotency_key, event_type, payload, status,
                retry_count, max_retries, created_at, updated_at
            )
            VALUES (
                'key_overflow', 'TEST_EVENT', '{}', 'PENDING', 5, 3,
                strftime('%s', 'now'), strftime('%s', 'now')
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_maxRetriesMustBePositive() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Try to insert webhook event with zero or negative max_retries
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO webhook_events (
                idempotency_key, event_type, payload, status,
                retry_count, max_retries, created_at, updated_at
            )
            VALUES (
                'key_zero_max', 'TEST_EVENT', '{}', 'PENDING', 0, 0,
                strftime('%s', 'now'), strftime('%s', 'now')
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_maxRetriesCannotExceedTen() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Try to insert webhook event with max_retries > 10
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO webhook_events (
                idempotency_key, event_type, payload, status,
                retry_count, max_retries, created_at, updated_at
            )
            VALUES (
                'key_high_max', 'TEST_EVENT', '{}', 'PENDING', 0, 15,
                strftime('%s', 'now'), strftime('%s', 'now')
            )
            """.trimIndent()
        )
    }

    @Test
    fun allIndexesRecreatedSuccessfully() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Query index list
        val cursor = database.query(
            """
            SELECT name FROM sqlite_master
            WHERE type = 'index' AND tbl_name = 'webhook_events'
            ORDER BY name
            """.trimIndent()
        )

        // Then: All expected indexes should exist
        val indexes = mutableListOf<String>()
        while (cursor.moveToNext()) {
            indexes.add(cursor.getString(0))
        }
        cursor.close()

        // Expected indexes (excluding auto-created indexes for primary key)
        val expectedIndexes = listOf(
            "index_webhook_events_idempotency_key",
            "index_webhook_events_status",
            "index_webhook_events_event_type",
            "idx_webhook_retry_queue",
            "idx_webhook_transaction_id",
            "idx_webhook_status_created",
            "idx_webhook_transaction_created",
            "idx_webhook_type_created"
        )

        for (expectedIndex in expectedIndexes) {
            assertTrue("Index $expectedIndex not found", indexes.contains(expectedIndex))
        }
    }

    @Test
    fun migrateFrom14To13_reversibleDownMigration() {
        // Given: Database at version 14 with CHECK constraints
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // Insert some test data at version 14
        database.execSQL(
            """
            INSERT INTO webhook_events (
                idempotency_key, event_type, payload, transaction_id,
                status, retry_count, max_retries, next_retry_at,
                delivered_at, created_at, updated_at, last_error
            )
            VALUES (
                'key_test', 'TEST_EVENT', '{}', 'txn_test',
                'DELIVERED', 1, 3, strftime('%s', 'now'),
                strftime('%s', 'now'), strftime('%s', 'now'),
                strftime('%s', 'now'), NULL
            )
            """.trimIndent()
        )

        // When: Migrate down from version 14 to 13
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration14Down
        )

        // Then: Data should be preserved
        val cursor = database.query("SELECT * FROM webhook_events WHERE idempotency_key = 'key_test'")
        cursor.moveToFirst()
        assertEquals("key_test", cursor.getString(cursor.getColumnIndexOrThrow("idempotency_key")))
        assertEquals("TEST_EVENT", cursor.getString(cursor.getColumnIndexOrThrow("event_type")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("retry_count")))
        cursor.close()
    }

    @Test
    fun emptyDatabaseMigrationSucceeds() {
        // Given: Empty database at version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 13)

        // When: Migrate to version 14
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // Then: Migration should succeed without errors
        val cursor = database.query("SELECT COUNT(*) FROM webhook_events")
        cursor.moveToFirst()
        assertEquals(0, cursor.getInt(0))
        cursor.close()
    }

    @Test
    fun validWebhookEventStates() {
        // Given: Database migrated to version 14
        database = migrationTestHelper.createDatabase(TEST_DB, 13)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            Migration14()
        )

        // When: Insert webhook events with all valid status values
        val validStatuses = listOf("PENDING", "PROCESSING", "DELIVERED", "FAILED", "CANCELLED")
        for ((index, status) in validStatuses.withIndex()) {
            database.execSQL(
                """
                INSERT INTO webhook_events (
                    idempotency_key, event_type, payload, status,
                    retry_count, max_retries, created_at, updated_at
                )
                VALUES (
                    'key_$index', 'TEST_EVENT', '{}', '$status', 0, 3,
                    strftime('%s', 'now'), strftime('%s', 'now')
                )
                """.trimIndent()
            )
        }

        // Then: All should be inserted successfully
        val cursor = database.query("SELECT COUNT(*) FROM webhook_events")
        cursor.moveToFirst()
        assertEquals(validStatuses.size, cursor.getInt(0))
        cursor.close()
    }

    companion object {
        private const val TEST_DB = "migration14_test"
    }
}
