package com.example.iurankomplek.payment

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Channel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class WebhookEventCleanerTest {

    private lateinit var webhookEventDao: WebhookEventDao
    private lateinit var eventChannel: Channel<Long>
    private lateinit var eventCleaner: WebhookEventCleaner

    @Before
    fun setup() {
        webhookEventDao = mockk(relaxed = true)
        eventChannel = Channel<Long>(capacity = Channel.UNLIMITED)
        eventCleaner = WebhookEventCleaner(webhookEventDao, eventChannel)
    }

    @Test
    fun `retryFailedEvents should retry failed events`() = runTest {
        val events = listOf(
            WebhookEvent(
                id = 1L,
                idempotencyKey = "whk_1",
                eventType = "payment.success",
                payload = "{}",
                status = WebhookDeliveryStatus.FAILED,
                retryCount = 5
            ),
            WebhookEvent(
                id = 2L,
                idempotencyKey = "whk_2",
                eventType = "payment.failed",
                payload = "{}",
                status = WebhookDeliveryStatus.FAILED,
                retryCount = 3
            )
        )

        coEvery {
            webhookEventDao.getPendingEventsByStatus(
                WebhookDeliveryStatus.FAILED,
                any<Long>(),
                50
            )
        } returns events
        coEvery { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) } just Runs

        val count = eventCleaner.retryFailedEvents()

        assertEquals(2, count)
        coVerify(exactly = 2) { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) }
    }

    @Test
    fun `retryFailedEvents should handle empty list`() = runTest {
        coEvery {
            webhookEventDao.getPendingEventsByStatus(
                WebhookDeliveryStatus.FAILED,
                any<Long>(),
                50
            )
        } returns emptyList()

        val count = eventCleaner.retryFailedEvents()

        assertEquals(0, count)
        coVerify(exactly = 0) { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) }
    }

    @Test
    fun `retryFailedEvents should respect limit parameter`() = runTest {
        val events = (1L..10L).map { id ->
            WebhookEvent(
                id = id,
                idempotencyKey = "whk_$id",
                eventType = "payment.success",
                payload = "{}",
                status = WebhookDeliveryStatus.FAILED,
                retryCount = 5
            )
        }

        coEvery {
            webhookEventDao.getPendingEventsByStatus(
                WebhookDeliveryStatus.FAILED,
                any<Long>(),
                5
            )
        } returns events.take(5)
        coEvery { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) } just Runs

        val count = eventCleaner.retryFailedEvents(5)

        assertEquals(5, count)
        coVerify(exactly = 5) { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) }
    }

    @Test
    fun `cleanupOldEvents should delete old events`() = runTest {
        coEvery { webhookEventDao.deleteEventsOlderThan(any<Long>()) } returns 5

        val count = eventCleaner.cleanupOldEvents()

        assertEquals(5, count)
        coVerify { webhookEventDao.deleteEventsOlderThan(any<Long>()) }
    }

    @Test
    fun `cleanupOldEvents should handle zero deletions`() = runTest {
        coEvery { webhookEventDao.deleteEventsOlderThan(any<Long>()) } returns 0

        val count = eventCleaner.cleanupOldEvents()

        assertEquals(0, count)
    }

    @Test
    fun `retryFailedEvents should send events to channel`() = runTest {
        val events = listOf(
            WebhookEvent(
                id = 1L,
                idempotencyKey = "whk_1",
                eventType = "payment.success",
                payload = "{}",
                status = WebhookDeliveryStatus.FAILED,
                retryCount = 5
            )
        )

        coEvery {
            webhookEventDao.getPendingEventsByStatus(
                WebhookDeliveryStatus.FAILED,
                any<Long>(),
                50
            )
        } returns events
        coEvery { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) } just Runs

        eventCleaner.retryFailedEvents()

        val eventId = eventChannel.receive()
        assertEquals(1L, eventId)
    }
}
