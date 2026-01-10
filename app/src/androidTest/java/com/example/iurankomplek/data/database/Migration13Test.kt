package com.example.iurankomplek.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.util.Date
import org.junit.Assert.*

/**
 * Test suite for Migration 13: Add CHECK Constraints to Transactions Table
 *
 * Tests verify:
 * 1. Migration completes successfully
 * 2. Valid data is preserved through migration
 * 3. CHECK constraints are enforced (invalid data rejected)
 * 4. Indexes are recreated correctly
 * 5. Down migration is reversible
 */
@RunWith(AndroidJUnit4::class)
class Migration13Test {

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
    fun migrateFrom12To13_validDataPreserved() {
        // Given: Database with valid transaction data
        database = migrationTestHelper.createDatabase(TEST_DB, 12).apply {
            execSQL(
                """
                INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
                VALUES (
                    'txn_123', 1, 50000.00, 'IDR', 'PENDING', 'BANK_TRANSFER', 'Test transaction', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
                )
                """.trimIndent()
            )

            execSQL(
                """
                INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
                VALUES (
                    'txn_456', 2, 150000.50, 'IDR', 'COMPLETED', 'E_WALLET', 'Another test', 0, strftime('%s', 'now') - 3600, strftime('%s', 'now'), 'key=value'
                )
                """.trimIndent()
            )
        }

        // When: Migrate from version 12 to 13
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // Then: Valid data should be preserved
        val cursor = database.query("SELECT * FROM transactions ORDER BY id")
        cursor.moveToFirst()

        // Verify first transaction
        assertEquals("txn_123", cursor.getString(cursor.getColumnIndexOrThrow("id")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("user_id")))
        assertEquals(50000.00, cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), 0.01)
        assertEquals("IDR", cursor.getString(cursor.getColumnIndexOrThrow("currency")))
        assertEquals("PENDING", cursor.getString(cursor.getColumnIndexOrThrow("status")))
        assertEquals("BANK_TRANSFER", cursor.getString(cursor.getColumnIndexOrThrow("payment_method")))
        assertEquals("Test transaction", cursor.getString(cursor.getColumnIndexOrThrow("description")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")))

        cursor.moveToNext()

        // Verify second transaction
        assertEquals("txn_456", cursor.getString(cursor.getColumnIndexOrThrow("id")))
        assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("user_id")))
        assertEquals(150000.50, cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), 0.01)
        assertEquals("IDR", cursor.getString(cursor.getColumnIndexOrThrow("currency")))
        assertEquals("COMPLETED", cursor.getString(cursor.getColumnIndexOrThrow("status")))
        assertEquals("E_WALLET", cursor.getString(cursor.getColumnIndexOrThrow("payment_method")))
        assertEquals("Another test", cursor.getString(cursor.getColumnIndexOrThrow("description")))
        assertEquals("key=value", cursor.getString(cursor.getColumnIndexOrThrow("metadata")))

        cursor.close()
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_amountMustBePositive() {
        // Given: Database migrated to version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // When: Try to insert transaction with negative amount
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
            VALUES (
                'txn_negative', 1, -1000.00, 'IDR', 'PENDING', 'BANK_TRANSFER', 'Negative amount', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_amountMustNotExceedMaximum() {
        // Given: Database migrated to version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // When: Try to insert transaction exceeding maximum amount
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
            VALUES (
                'txn_overflow', 1, 1000000000.00, 'IDR', 'PENDING', 'BANK_TRANSFER', 'Overflow amount', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_statusMustBeValidEnum() {
        // Given: Database migrated to version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // When: Try to insert transaction with invalid status
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
            VALUES (
                'txn_invalid_status', 1, 50000.00, 'IDR', 'INVALID_STATUS', 'BANK_TRANSFER', 'Invalid status', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_paymentMethodMustBeValidEnum() {
        // Given: Database migrated to version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // When: Try to insert transaction with invalid payment method
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
            VALUES (
                'txn_invalid_method', 1, 50000.00, 'IDR', 'PENDING', 'INVALID_METHOD', 'Invalid method', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_descriptionCannotBeEmpty() {
        // Given: Database migrated to version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // When: Try to insert transaction with empty description
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
            VALUES (
                'txn_empty_desc', 1, 50000.00, 'IDR', 'PENDING', 'BANK_TRANSFER', '', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
            )
            """.trimIndent()
        )
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun checkConstraint_currencyLengthLimit() {
        // Given: Database migrated to version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // When: Try to insert transaction with currency code longer than 3 characters
        // Then: Should throw SQLiteConstraintException
        database.execSQL(
            """
            INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
            VALUES (
                'txn_long_currency', 1, 50000.00, 'INVALID', 'PENDING', 'BANK_TRANSFER', 'Long currency code', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
            )
            """.trimIndent()
        )
    }

    @Test
    fun allIndexesRecreatedSuccessfully() {
        // Given: Database migrated to version 13
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // When: Query index list
        val cursor = database.query(
            """
            SELECT name FROM sqlite_master
            WHERE type = 'index' AND tbl_name = 'transactions'
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
            "idx_transactions_user_updated",
            "idx_transactions_user_id",
            "idx_transactions_status",
            "idx_transactions_user_status",
            "idx_transactions_created_at",
            "idx_transactions_updated_at",
            "idx_transactions_user_active",
            "idx_transactions_status_active",
            "idx_transactions_user_status_active",
            "idx_transactions_created_at_active",
            "idx_transactions_status_deleted"
        )

        for (expectedIndex in expectedIndexes) {
            assertTrue("Index $expectedIndex not found", indexes.contains(expectedIndex))
        }
    }

    @Test
    fun migrateFrom13To12_reversibleDownMigration() {
        // Given: Database at version 13 with CHECK constraints
        database = migrationTestHelper.createDatabase(TEST_DB, 12)
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // Insert some test data at version 13
        database.execSQL(
            """
            INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata)
            VALUES (
                'txn_test', 1, 75000.00, 'IDR', 'COMPLETED', 'CREDIT_CARD', 'Test for down migration', 0, strftime('%s', 'now'), strftime('%s', 'now'), ''
            )
            """.trimIndent()
        )

        // When: Migrate down from version 13 to 12
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            12,
            true,
            Migration13Down
        )

        // Then: Data should be preserved
        val cursor = database.query("SELECT * FROM transactions WHERE id = 'txn_test'")
        cursor.moveToFirst()
        assertEquals("txn_test", cursor.getString(cursor.getColumnIndexOrThrow("id")))
        assertEquals(75000.00, cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), 0.01)
        cursor.close()
    }

    @Test
    fun emptyDatabaseMigrationSucceeds() {
        // Given: Empty database at version 12
        database = migrationTestHelper.createDatabase(TEST_DB, 12)

        // When: Migrate to version 13
        database = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            Migration13()
        )

        // Then: Migration should succeed without errors
        val cursor = database.query("SELECT COUNT(*) FROM transactions")
        cursor.moveToFirst()
        assertEquals(0, cursor.getInt(0))
        cursor.close()
    }

    companion object {
        private const val TEST_DB = "migration13_test"
    }
}
