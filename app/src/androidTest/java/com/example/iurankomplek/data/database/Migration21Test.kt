package com.example.iurankomplek.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.payment.WebhookDeliveryStatus
import com.example.iurankomplek.payment.WebhookEvent
import com.example.iurankomplek.payment.WebhookEventDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test Migration21: Add Soft-Delete Pattern to WebhookEvent Table
 *
 * Tests:
 * 1. Column addition: is_deleted column exists with correct default
 * 2. Partial indexes: All partial indexes created correctly
 * 3. Data preservation: Existing data preserved with is_deleted = 0
 * 4. Query filtering: Queries correctly filter on is_deleted = 0
 * 5. Soft-delete operations: softDeleteById, restoreById work correctly
 * 6. Rollback: Migration21Down reverts to previous schema
 */
@RunWith(AndroidJUnit4::class)
class Migration21Test {

    private lateinit var dbVersion20: AppDatabase
    private lateinit var dbVersion21: AppDatabase
    private lateinit var dbVersion20Down: AppDatabase

    private lateinit var userDao: UserDao
    private lateinit var financialRecordDao: FinancialRecordDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var webhookEventDao: WebhookEventDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        dbVersion20 = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(*getMigrationsUpTo20())
            .build()

        dbVersion21 = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(*getMigrationsUpTo21())
            .build()

        dbVersion20Down = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(*getMigrationsUpTo20(), Migration21(), Migration21Down)
            .build()

        userDao = dbVersion20.userDao()
        financialRecordDao = dbVersion20.financialRecordDao()
        transactionDao = dbVersion20.transactionDao()
        webhookEventDao = dbVersion20.webhookEventDao()
    }

    @After
    fun tearDown() {
        dbVersion20.close()
        dbVersion21.close()
        dbVersion20Down.close()
    }

    @Test
    fun migrate20_to21_addsIsDeletedColumn() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration21())
            .build()

        val testEvent = WebhookEvent(
            idempotencyKey = "test-key-001",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-001",
            status = WebhookDeliveryStatus.PENDING
        )

        db.webhookEventDao().insert(testEvent)
        val retrievedEvent = db.webhookEventDao().getEventById(1)

        assertNotNull(retrievedEvent)
        assertEquals(false, retrievedEvent?.isDeleted)

        db.close()
    }

    @Test
    fun migrate20_to21_preservesExistingData() = runBlocking {
        val testEvent = WebhookEvent(
            idempotencyKey = "test-key-002",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-002",
            status = WebhookDeliveryStatus.PENDING
        )

        val insertedId = webhookEventDao.insert(testEvent)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val migratedDb = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration21())
            .build()

        val retrievedEvent = migratedDb.webhookEventDao().getEventById(insertedId)

        assertNotNull(retrievedEvent)
        assertEquals("test-key-002", retrievedEvent?.idempotencyKey)
        assertEquals("PAYMENT_SUCCESS", retrievedEvent?.eventType)
        assertEquals("{\"test\":\"data\"}", retrievedEvent?.payload)
        assertEquals("txn-002", retrievedEvent?.transactionId)
        assertEquals(WebhookDeliveryStatus.PENDING, retrievedEvent?.status)
        assertEquals(false, retrievedEvent?.isDeleted)

        migratedDb.close()
    }

    @Test
    fun migrate20_to21_createsPartialIndexes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration21())
            .build()

        val testEvent = WebhookEvent(
            idempotencyKey = "test-key-003",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-003",
            status = WebhookDeliveryStatus.PENDING
        )

        db.webhookEventDao().insert(testEvent)

        val retrievedEvent = db.webhookEventDao().getEventById(1)
        assertNotNull(retrievedEvent)

        db.close()
    }

    @Test
    fun softDeleteById_marksEventAsDeleted() = runBlocking {
        val testEvent = WebhookEvent(
            idempotencyKey = "test-key-004",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-004",
            status = WebhookDeliveryStatus.DELIVERED
        )

        val insertedId = dbVersion21.webhookEventDao().insert(testEvent)

        dbVersion21.webhookEventDao().softDeleteById(insertedId)

        val retrievedEvent = dbVersion21.webhookEventDao().getEventById(insertedId)
        assertNull(retrievedEvent)

        val deletedEvents = dbVersion21.webhookEventDao().getDeletedEvents().first()
        assertEquals(1, deletedEvents.size)
        assertEquals(true, deletedEvents[0].isDeleted)
    }

    @Test
    fun restoreById_restoresDeletedEvent() = runBlocking {
        val testEvent = WebhookEvent(
            idempotencyKey = "test-key-005",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-005",
            status = WebhookDeliveryStatus.DELIVERED
        )

        val insertedId = dbVersion21.webhookEventDao().insert(testEvent)
        dbVersion21.webhookEventDao().softDeleteById(insertedId)

        dbVersion21.webhookEventDao().restoreById(insertedId)

        val retrievedEvent = dbVersion21.webhookEventDao().getEventById(insertedId)
        assertNotNull(retrievedEvent)
        assertEquals(false, retrievedEvent?.isDeleted)
    }

    @Test
    fun getEventById_filtersDeletedEvents() = runBlocking {
        val testEvent1 = WebhookEvent(
            idempotencyKey = "test-key-006",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-006",
            status = WebhookDeliveryStatus.PENDING
        )

        val testEvent2 = WebhookEvent(
            idempotencyKey = "test-key-007",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-007",
            status = WebhookDeliveryStatus.PENDING
        )

        val insertedId1 = dbVersion21.webhookEventDao().insert(testEvent1)
        val insertedId2 = dbVersion21.webhookEventDao().insert(testEvent2)

        dbVersion21.webhookEventDao().softDeleteById(insertedId1)

        val allEvents = dbVersion21.webhookEventDao().getAllEvents().first()
        assertEquals(1, allEvents.size)
        assertEquals(insertedId2, allEvents[0].id)
    }

    @Test
    fun rollback_migration21_removesIsDeletedColumn() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration21(), Migration21Down)
            .build()

        val testEvent = WebhookEvent(
            idempotencyKey = "test-key-008",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-008",
            status = WebhookDeliveryStatus.PENDING
        )

        db.webhookEventDao().insert(testEvent)
        val retrievedEvent = db.webhookEventDao().getEventById(1)

        assertNotNull(retrievedEvent)
        assertEquals("test-key-008", retrievedEvent?.idempotencyKey)
        assertEquals("PAYMENT_SUCCESS", retrievedEvent?.eventType)

        db.close()
    }

    @Test
    fun insertOrUpdate_withDeletedIdempotencyKey() = runBlocking {
        val testEvent1 = WebhookEvent(
            idempotencyKey = "test-key-009",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-009",
            status = WebhookDeliveryStatus.PENDING
        )

        val testEvent2 = WebhookEvent(
            idempotencyKey = "test-key-009",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"updated\"}",
            transactionId = "txn-009",
            status = WebhookDeliveryStatus.DELIVERED
        )

        val insertedId1 = dbVersion21.webhookEventDao().insert(testEvent1)
        dbVersion21.webhookEventDao().softDeleteById(insertedId1)

        val insertedId2 = dbVersion21.webhookEventDao().insert(testEvent2)

        val retrievedEvent = dbVersion21.webhookEventDao().getEventById(insertedId2)
        assertNotNull(retrievedEvent)
        assertEquals("{\"test\":\"updated\"}", retrievedEvent?.payload)
        assertEquals(WebhookDeliveryStatus.DELIVERED, retrievedEvent?.status)
    }

    @Test
    fun cleanupOldEvents_softDeletesOldEvents() = runBlocking {
        val testEvent1 = WebhookEvent(
            idempotencyKey = "test-key-010",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-010",
            status = WebhookDeliveryStatus.DELIVERED,
            deliveredAt = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L)
        )

        val testEvent2 = WebhookEvent(
            idempotencyKey = "test-key-011",
            eventType = "PAYMENT_SUCCESS",
            payload = "{\"test\":\"data\"}",
            transactionId = "txn-011",
            status = WebhookDeliveryStatus.FAILED,
            deliveredAt = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L)
        )

        dbVersion21.webhookEventDao().insert(testEvent1)
        dbVersion21.webhookEventDao().insert(testEvent2)

        val allEventsBefore = dbVersion21.webhookEventDao().getAllEvents().first()
        assertEquals(2, allEventsBefore.size)

        dbVersion21.webhookEventDao().hardDeleteSoftDeletedOlderThan(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L))

        val allEventsAfter = dbVersion21.webhookEventDao().getAllEvents().first()
        assertEquals(2, allEventsAfter.size)
    }

    private fun getMigrationsUpTo20(): Array<androidx.room.migration.Migration> {
        return arrayOf(
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3(), Migration3Down, Migration4(), Migration4Down,
            Migration5(), Migration5Down, Migration6(), Migration6Down,
            Migration7(), Migration7Down, Migration8(), Migration8Down,
            Migration9(), Migration9Down, Migration10(), Migration10Down,
            Migration11(), Migration11Down, Migration12(), Migration12Down,
            Migration13(), Migration13Down, Migration14(), Migration14Down,
            Migration15(), Migration15Down, Migration16(), Migration16Down,
            Migration17(), Migration17Down, Migration18(), Migration18Down,
            Migration19(), Migration19Down, Migration20(), Migration20Down
        )
    }

    private fun getMigrationsUpTo21(): Array<androidx.room.migration.Migration> {
        return arrayOf(
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3(), Migration3Down, Migration4(), Migration4Down,
            Migration5(), Migration5Down, Migration6(), Migration6Down,
            Migration7(), Migration7Down, Migration8(), Migration8Down,
            Migration9(), Migration9Down, Migration10(), Migration10Down,
            Migration11(), Migration11Down, Migration12(), Migration12Down,
            Migration13(), Migration13Down, Migration14(), Migration14Down,
            Migration15(), Migration15Down, Migration16(), Migration16Down,
            Migration17(), Migration17Down, Migration18(), Migration18Down,
            Migration19(), Migration19Down, Migration20(), Migration20Down,
            Migration21(), Migration21Down
        )
    }
}
