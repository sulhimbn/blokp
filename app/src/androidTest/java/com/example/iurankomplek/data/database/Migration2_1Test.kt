package com.example.iurankomplek.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.iurankomplek.data.database.TransactionDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2_1Test {

    private val TEST_DB = "migration-test-down"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TransactionDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun setUp() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, created_at, updated_at, metadata) VALUES " +
                    "('tx1', 123, 100000.00, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test payment', strftime('%s', 'now'), strftime('%s', 'now'), 'key=value')")
        }
    }

    @Test
    fun migrate2To1_dataPreserved() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 1, false, Migration2_1())

        db.query("SELECT id, userId, amount, currency FROM transactions WHERE id = 'tx1'").use { cursor ->
            assertTrue("Should have data", cursor.moveToFirst())
            assertEquals("tx1", cursor.getString(0))
            assertEquals("123", cursor.getString(1))
            assertEquals(100000.00, cursor.getDouble(2), 0.001)
            assertEquals("IDR", cursor.getString(3))
        }
    }

    @Test
    fun migrate2To1_indexesRemoved() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 1, false, Migration2_1())

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'").use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexes.add(cursor.getString(0))
            }
            assertTrue("user_id index should not exist", !indexes.any { it.contains("user_id") })
            assertTrue("status index should not exist", !indexes.any { it.contains("status") })
        }
    }

    @Test
    fun migrate2To1_foreignKeyRemoved() {
        val db = helper.runMigrationsAndValidate(TEST_DB, 1, false, Migration2_1())

        db.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='transactions'").use { cursor ->
            assertTrue("Should have table definition", cursor.moveToFirst())
            val sql = cursor.getString(0)
            assertTrue("Should not have FOREIGN KEY constraint", !sql.contains("FOREIGN KEY"))
        }
    }
}
