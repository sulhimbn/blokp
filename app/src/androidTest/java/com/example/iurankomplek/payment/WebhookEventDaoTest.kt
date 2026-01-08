package com.example.iurankomplek.payment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class WebhookEventDaoTest {

    private lateinit var database: androidx.room.RoomDatabase
    private lateinit var webhookEventDao: WebhookEventDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            com.example.iurankomplek.data.database.AppDatabase::class.java
        ).build()
        
        webhookEventDao = database.webhookEventDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_shouldInsertWebhookEvent() = runBlocking {
        val event = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val id = webhookEventDao.insert(event)

        assertTrue(id > 0, "Insert should return positive ID")
    }

    @Test
    fun insertIgnoreConflict_shouldNotDuplicateIdempotencyKey() = runBlocking {
        val event1 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data1"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val event2 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data2"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val id1 = webhookEventDao.insertIgnoreConflict(event1)
        val id2 = webhookEventDao.insertIgnoreConflict(event2)

        assertTrue(id1 != null && id1 > 0, "First insert should succeed")
        assertTrue(id2 == null || id2 == -1L, "Second insert with same key should be ignored")
    }

    @Test
    fun getEventById_shouldReturnCorrectEvent() = runBlocking {
        val event = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val id = webhookEventDao.insert(event)
        val retrieved = webhookEventDao.getEventById(id)

        assertNotNull(retrieved, "Event should be retrieved")
        assertEquals("whk_1", retrieved.idempotencyKey)
        assertEquals("payment.success", retrieved.eventType)
    }

    @Test
    fun getEventByIdempotencyKey_shouldReturnCorrectEvent() = runBlocking {
        val event = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        webhookEventDao.insert(event)
        val retrieved = webhookEventDao.getEventByIdempotencyKey("whk_1")

        assertNotNull(retrieved, "Event should be retrieved")
        assertEquals("whk_1", retrieved.idempotencyKey)
    }

    @Test
    fun getEventByIdempotencyKey_shouldReturnNullForNonExistentKey() = runBlocking {
        val retrieved = webhookEventDao.getEventByIdempotencyKey("whk_nonexistent")
        
        assertNull(retrieved, "Should return null for non-existent key")
    }

    @Test
    fun getPendingEventsByStatus_shouldReturnPendingEvents() = runBlocking {
        val event1 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data1"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            nextRetryAt = null
        )

        val event2 = WebhookEvent(
            idempotencyKey = "whk_2",
            eventType = "payment.failed",
            payload = """{"test":"data2"}""",
            transactionId = "tx456",
            status = WebhookDeliveryStatus.FAILED
        )

        webhookEventDao.insert(event1)
        webhookEventDao.insert(event2)

        val pendingEvents = webhookEventDao.getPendingEventsByStatus(
            WebhookDeliveryStatus.PENDING,
            System.currentTimeMillis(),
            10
        )

        assertEquals(1, pendingEvents.size, "Should return one pending event")
        assertEquals("whk_1", pendingEvents[0].idempotencyKey)
    }

    @Test
    fun updateStatus_shouldUpdateEventStatus() = runBlocking {
        val event = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val id = webhookEventDao.insert(event)
        webhookEventDao.updateStatus(id, WebhookDeliveryStatus.PROCESSING)

        val retrieved = webhookEventDao.getEventById(id)
        assertEquals(WebhookDeliveryStatus.PROCESSING, retrieved?.status)
    }

    @Test
    fun updateRetryInfo_shouldUpdateRetryCountAndNextRetry() = runBlocking {
        val event = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PROCESSING,
            retryCount = 0
        )

        val id = webhookEventDao.insert(event)
        val nextRetryAt = System.currentTimeMillis() + 5000L
        
        webhookEventDao.updateRetryInfo(
            id = id,
            retryCount = 1,
            nextRetryAt = nextRetryAt,
            lastError = "Network timeout"
        )

        val retrieved = webhookEventDao.getEventById(id)
        assertEquals(1, retrieved?.retryCount)
        assertEquals(nextRetryAt, retrieved?.nextRetryAt)
        assertEquals("Network timeout", retrieved?.lastError)
    }

    @Test
    fun markAsDelivered_shouldUpdateDeliveryTimestamp() = runBlocking {
        val event = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PROCESSING
        )

        val id = webhookEventDao.insert(event)
        webhookEventDao.markAsDelivered(id)

        val retrieved = webhookEventDao.getEventById(id)
        assertEquals(WebhookDeliveryStatus.DELIVERED, retrieved?.status)
        assertNotNull(retrieved?.deliveredAt, "deliveredAt should be set")
    }

    @Test
    fun markAsFailed_shouldUpdateStatusToFailed() = runBlocking {
        val event = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PROCESSING
        )

        val id = webhookEventDao.insert(event)
        webhookEventDao.markAsFailed(id)

        val retrieved = webhookEventDao.getEventById(id)
        assertEquals(WebhookDeliveryStatus.FAILED, retrieved?.status)
    }

    @Test
    fun deleteEventsOlderThan_shouldDeleteOldEvents() = runBlocking {
        val oldEvent = WebhookEvent(
            idempotencyKey = "whk_old",
            eventType = "payment.success",
            payload = """{"test":"old"}""",
            transactionId = "tx_old",
            status = WebhookDeliveryStatus.DELIVERED,
            createdAt = System.currentTimeMillis() - (40L * 24L * 60L * 60L * 1000L)
        )

        val recentEvent = WebhookEvent(
            idempotencyKey = "whk_new",
            eventType = "payment.success",
            payload = """{"test":"new"}""",
            transactionId = "tx_new",
            status = WebhookDeliveryStatus.DELIVERED,
            createdAt = System.currentTimeMillis()
        )

        webhookEventDao.insert(oldEvent)
        webhookEventDao.insert(recentEvent)

        val cutoffTime = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
        val deletedCount = webhookEventDao.deleteEventsOlderThan(cutoffTime)

        assertEquals(1, deletedCount, "Should delete one old event")
        assertNull(webhookEventDao.getEventByIdempotencyKey("whk_old"))
        assertNotNull(webhookEventDao.getEventByIdempotencyKey("whk_new"))
    }

    @Test
    fun countByStatus_shouldReturnCorrectCount() = runBlocking {
        val event1 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data1"}""",
            status = WebhookDeliveryStatus.PENDING
        )

        val event2 = WebhookEvent(
            idempotencyKey = "whk_2",
            eventType = "payment.success",
            payload = """{"test":"data2"}""",
            status = WebhookDeliveryStatus.PENDING
        )

        val event3 = WebhookEvent(
            idempotencyKey = "whk_3",
            eventType = "payment.success",
            payload = """{"test":"data3"}""",
            status = WebhookDeliveryStatus.FAILED
        )

        webhookEventDao.insert(event1)
        webhookEventDao.insert(event2)
        webhookEventDao.insert(event3)

        val pendingCount = webhookEventDao.countByStatus(WebhookDeliveryStatus.PENDING)
        val failedCount = webhookEventDao.countByStatus(WebhookDeliveryStatus.FAILED)

        assertEquals(2, pendingCount, "Should count 2 pending events")
        assertEquals(1, failedCount, "Should count 1 failed event")
    }

    @Test
    fun getEventsByTransactionId_shouldReturnEventsForTransaction() = runBlocking {
        val event1 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data1"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val event2 = WebhookEvent(
            idempotencyKey = "whk_2",
            eventType = "payment.failed",
            payload = """{"test":"data2"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.FAILED
        )

        val event3 = WebhookEvent(
            idempotencyKey = "whk_3",
            eventType = "payment.success",
            payload = """{"test":"data3"}""",
            transactionId = "tx456",
            status = WebhookDeliveryStatus.PENDING
        )

        webhookEventDao.insert(event1)
        webhookEventDao.insert(event2)
        webhookEventDao.insert(event3)

        val events = webhookEventDao.getEventsByTransactionId("tx123").first()
        assertEquals(2, events.size, "Should return 2 events for tx123")
    }

    @Test
    fun getEventsByType_shouldReturnEventsByType() = runBlocking {
        val event1 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data1"}""",
            status = WebhookDeliveryStatus.PENDING
        )

        val event2 = WebhookEvent(
            idempotencyKey = "whk_2",
            eventType = "payment.success",
            payload = """{"test":"data2"}""",
            status = WebhookDeliveryStatus.PENDING
        )

        val event3 = WebhookEvent(
            idempotencyKey = "whk_3",
            eventType = "payment.failed",
            payload = """{"test":"data3"}""",
            status = WebhookDeliveryStatus.PENDING
        )

        webhookEventDao.insert(event1)
        webhookEventDao.insert(event2)
        webhookEventDao.insert(event3)

        val events = webhookEventDao.getEventsByType("payment.success").first()
        assertEquals(2, events.size, "Should return 2 payment.success events")
    }

    @Test
    fun insertOrUpdate_shouldUpdateExistingEvent() = runBlocking {
        val event1 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data1"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val id = webhookEventDao.insert(event1)

        val event2 = WebhookEvent(
            idempotencyKey = "whk_1",
            eventType = "payment.success",
            payload = """{"test":"data2"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.FAILED
        )

        val updatedId = webhookEventDao.insertOrUpdate(event2)

        assertEquals(id, updatedId, "Should return same ID for existing event")
        
        val retrieved = webhookEventDao.getEventById(id)
        assertEquals(WebhookDeliveryStatus.FAILED, retrieved?.status, "Status should be updated")
    }
}
