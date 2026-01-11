package com.example.iurankomplek.data.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration2Test {

    private lateinit var database: androidx.room.RoomDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val TEST_DB = "migration-test"

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun migrate1To2_shouldCreateWebhookEventsTable() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            com.example.iurankomplek.data.database.AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        migratedDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertTrue(tables.contains("webhook_events"), "webhook_events table should exist")
        }

        migratedDb.close()
    }

    @Test
    fun migrate1To2_shouldCreateCorrectIndexes() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            com.example.iurankomplek.data.database.AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='webhook_events'"
        ).use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexes.add(cursor.getString(0))
            }
            assertTrue(
                indexes.any { it.contains("idempotency_key") },
                "Index on idempotency_key should exist"
            )
            assertTrue(
                indexes.any { it.contains("status") },
                "Index on status should exist"
            )
            assertTrue(
                indexes.any { it.contains("event_type") },
                "Index on event_type should exist"
            )
        }

        migratedDb.close()
    }

    @Test
    fun migrate1To2_shouldHaveCorrectSchema() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            com.example.iurankomplek.data.database.AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        migratedDb.query("PRAGMA table_info(webhook_events)").use { cursor ->
            val columns = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(1))
            }

            val expectedColumns = listOf(
                "id",
                "idempotency_key",
                "event_type",
                "payload",
                "transaction_id",
                "status",
                "retry_count",
                "max_retries",
                "next_retry_at",
                "delivered_at",
                "created_at",
                "updated_at",
                "last_error"
            )

            expectedColumns.forEach { column ->
                assertTrue(columns.contains(column), "Column $column should exist")
            }
        }

        migratedDb.close()
    }

    @Test
    fun migratedDatabase_shouldAllowWebhookEventOperations() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            com.example.iurankomplek.data.database.AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1(), Migration2())

        val database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            com.example.iurankomplek.data.database.AppDatabase::class.java,
            TEST_DB
        ).addMigrations(Migration1(), Migration2()).build()

        val webhookEventDao = database.webhookEventDao()

        val event = com.example.iurankomplek.payment.WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = com.example.iurankomplek.payment.WebhookDeliveryStatus.PENDING
        )

        val id = runBlocking {
            webhookEventDao.insert(event)
        }

        assertTrue(id > 0, "Insert should succeed")

        val retrieved = runBlocking {
            webhookEventDao.getEventById(id)
        }

        assertTrue(retrieved != null, "Event should be retrievable")
        assertEquals("whk_1", retrieved?.idempotencyKey)

        database.close()
    }

    private fun runBlocking(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking {
            block()
        }
    }
}
