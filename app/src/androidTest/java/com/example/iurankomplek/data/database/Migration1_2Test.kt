package com.example.iurankomplek.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.database.TransactionDatabase
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.util.Date

@RunWith(AndroidJUnit4::class)
class Migration1_2Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TransactionDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun setUp() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO transactions (id, userId, amount, currency, status, payment_method, description, created_at, updated_at, metadata_json) VALUES " +
                    "('tx1', '123', 100000.00, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test payment', strftime('%s', 'now'), strftime('%s', 'now'), 'key=value')")
        }
    }

    @Test
    fun migrate1To2_indexesCreated() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1_2())

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'").use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexes.add(cursor.getString(0))
            }
            assertTrue("user_id index should exist", indexes.any { it.contains("user_id") })
            assertTrue("status index should exist", indexes.any { it.contains("status") })
            assertTrue("user_status composite index should exist", indexes.any { it.contains("user_status") })
            assertTrue("created_at index should exist", indexes.any { it.contains("created_at") })
            assertTrue("updated_at index should exist", indexes.any { it.contains("updated_at") })
        }
    }

    @Test
    fun migrate1To2_dataPreserved() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1_2())

        db.query("SELECT id, user_id, amount, currency FROM transactions WHERE id = 'tx1'").use { cursor ->
            assertTrue("Should have data", cursor.moveToFirst())
            assertEquals("tx1", cursor.getString(0))
            assertEquals(123L, cursor.getLong(1))
            assertEquals(100000.00, cursor.getDouble(2), 0.001)
            assertEquals("IDR", cursor.getString(3))
        }
    }

    @Test
    fun migrate1To2_foreignKeyAdded() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1_2())

        db.query("PRAGMA foreign_keys").use { cursor ->
            assertTrue("Foreign keys should be enabled", cursor.moveToFirst() && cursor.getInt(0) == 1)
        }
    }

    @Test
    fun migrate1To2_checkConstraintsAdded() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1_2())

        db.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='transactions'").use { cursor ->
            assertTrue("Should have table definition", cursor.moveToFirst())
            val sql = cursor.getString(0)
            assertTrue("Should have CHECK constraint on amount", sql.contains("CHECK(amount > 0"))
            assertTrue("Should have CHECK constraint on currency", sql.contains("CHECK(length(currency)"))
            assertTrue("Should have CHECK constraint on status", sql.contains("CHECK(status IN"))
            assertTrue("Should have CHECK constraint on payment_method", sql.contains("CHECK(payment_method IN"))
        }
    }
}
