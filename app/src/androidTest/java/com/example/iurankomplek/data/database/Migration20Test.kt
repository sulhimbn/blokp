package com.example.iurankomplek.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Migration 20 Test Suite
 *
 * Tests:
 * - Transaction.amount column converted from TEXT to INTEGER (cents)
 * - Existing data correctly converted (multiply by 100)
 * - Type converter handles cents-to-BigDecimal conversion
 * - Rollback (Migration20Down) works correctly
 */
@RunWith(AndroidJUnit4::class)
class Migration20Test {
    private lateinit var helper: MigrationTestHelper
    private lateinit var db: androidx.room.RoomDatabase

    @Before
    fun setUp() {
        helper = MigrationTestHelper(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java.canonicalName,
            androidx.arch.core.executor.testing.InstantTaskExecutorRule()
        )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        helper.closeAllDatabases()
    }

    @Test
    fun migration20_should_convert_amount_column_to_integer() {
        val db19 = helper.createDatabase("test-db", 19)

        // Insert test transaction with TEXT amount (pre-migration)
        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-123', 1, '100000.50', 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test transaction', 0, 1000000, 1000000, '')")

        db19.close()

        val db20 = helper.runMigrationsAndValidate("test-db", 20, true, Migration19(), Migration20())

        // Verify amount is now INTEGER (cents)
        val cursor = db20.query("SELECT amount FROM transactions WHERE id = 'test-123'")
        cursor.moveToFirst()
        val amount = cursor.getLong(0)

        cursor.close()
        db20.close()

        assertEquals(10000050L, amount) // 100000.50 * 100 = 10000050 cents
    }

    @Test
    fun migration20_should_preserve_all_transaction_data() {
        val db19 = helper.createDatabase("test-db", 19)

        // Insert multiple test transactions
        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-1', 1, '100.00', 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test 1', 0, 1000000, 1000000, '')")
        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-2', 2, '50.50', 'IDR', 'PENDING', 'E_WALLET', 'Test 2', 0, 1000000, 1000000, '')")
        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-3', 1, '0.01', 'IDR', 'FAILED', 'VIRTUAL_ACCOUNT', 'Test 3', 0, 1000000, 1000000, '')")

        db19.close()

        val db20 = helper.runMigrationsAndValidate("test-db", 20, true, Migration19(), Migration20())

        // Verify all transactions converted correctly
        val cursor = db20.query("SELECT id, user_id, amount, currency, status FROM transactions ORDER BY id")
        val count = cursor.count

        val ids = mutableListOf<String>()
        val amounts = mutableListOf<Long>()

        while (cursor.moveToNext()) {
            ids.add(cursor.getString(0))
            amounts.add(cursor.getLong(2))
        }

        cursor.close()
        db20.close()

        assertEquals(3, count)
        assertEquals(listOf("test-1", "test-2", "test-3"), ids)
        assertEquals(listOf(10000L, 5050L, 1L), amounts) // amounts in cents
    }

    @Test
    fun migration20_should_handle_null_amount() {
        val db19 = helper.createDatabase("test-db", 19)

        // Insert transaction with NULL amount
        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-null', 1, NULL, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test', 0, 1000000, 1000000, '')")

        db19.close()

        val db20 = helper.runMigrationsAndValidate("test-db", 20, true, Migration19(), Migration20())

        // Verify NULL amount handled
        val cursor = db20.query("SELECT amount FROM transactions WHERE id = 'test-null'")
        cursor.moveToFirst()
        val amount = if (cursor.isNull(0)) null else cursor.getLong(0)

        cursor.close()
        db20.close()

        assertNotNull(amount)
        assertEquals(0L, amount) // NULL defaults to 0 cents
    }

    @Test
    fun migration20_should_recreate_indexes() {
        val db19 = helper.createDatabase("test-db", 19)

        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-index', 1, '100.00', 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test', 0, 1000000, 1000000, '')")

        db19.close()

        val db20 = helper.runMigrationsAndValidate("test-db", 20, true, Migration19(), Migration20())

        // Verify indexes exist
        val indexCursor = db20.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions' AND name LIKE 'idx_transactions_%'")
        val indexCount = indexCursor.count

        val indexNames = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }

        indexCursor.close()
        db20.close()

        assertTrue("Should have indexes", indexCount > 0)
        assertTrue("Should have user_id index", indexNames.contains("idx_transactions_user_id"))
        assertTrue("Should have status index", indexNames.contains("idx_transactions_status"))
        assertTrue("Should have user_status composite index", indexNames.contains("idx_transactions_user_status"))
    }

    @Test
    fun migration20Down_should_convert_amount_back_to_text() {
        val db19 = helper.createDatabase("test-db", 19)

        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-rollback', 1, '100.50', 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test', 0, 1000000, 1000000, '')")

        db19.close()

        val db20 = helper.runMigrationsAndValidate("test-db", 20, true, Migration19(), Migration20())
        db20.close()

        val db19Again = helper.runMigrationsAndValidate("test-db", 19, true, Migration19(), Migration20Down())

        // Verify amount converted back to TEXT (decimal)
        val cursor = db19Again.query("SELECT amount FROM transactions WHERE id = 'test-rollback'")
        cursor.moveToFirst()
        val amount = cursor.getString(0)

        cursor.close()
        db19Again.close()

        assertEquals("100.50", amount)
    }

    @Test
    fun migration20Down_should_preserve_all_transaction_data() {
        val db19 = helper.createDatabase("test-db", 19)

        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-1', 1, '100.00', 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test 1', 0, 1000000, 1000000, '')")
        db19.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-2', 2, '50.50', 'IDR', 'PENDING', 'E_WALLET', 'Test 2', 0, 1000000, 1000000, '')")

        db19.close()

        val db20 = helper.runMigrationsAndValidate("test-db", 20, true, Migration19(), Migration20())
        db20.close()

        val db19Again = helper.runMigrationsAndValidate("test-db", 19, true, Migration19(), Migration20Down())

        val cursor = db19Again.query("SELECT id, amount FROM transactions ORDER BY id")
        val count = cursor.count

        val ids = mutableListOf<String>()
        val amounts = mutableListOf<String>()

        while (cursor.moveToNext()) {
            ids.add(cursor.getString(0))
            amounts.add(cursor.getString(1))
        }

        cursor.close()
        db19Again.close()

        assertEquals(2, count)
        assertEquals(listOf("test-1", "test-2"), ids)
        assertEquals(listOf("100.00", "50.50"), amounts)
    }
}
