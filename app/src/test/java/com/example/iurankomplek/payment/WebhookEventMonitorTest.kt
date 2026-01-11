package com.example.iurankomplek.payment

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class WebhookEventMonitorTest {

    private lateinit var webhookEventDao: WebhookEventDao
    private lateinit var eventMonitor: WebhookEventMonitor

    @Before
    fun setup() {
        webhookEventDao = mockk(relaxed = true)
        eventMonitor = WebhookEventMonitor(webhookEventDao)
    }

    @Test
    fun `getPendingEventCount should return count from DAO`() = runTest {
        coEvery { webhookEventDao.countByStatus(WebhookDeliveryStatus.PENDING) } returns 10

        val count = eventMonitor.getPendingEventCount()

        assertEquals(10, count)
        coVerify { webhookEventDao.countByStatus(WebhookDeliveryStatus.PENDING) }
    }

    @Test
    fun `getFailedEventCount should return count from DAO`() = runTest {
        coEvery { webhookEventDao.countByStatus(WebhookDeliveryStatus.FAILED) } returns 3

        val count = eventMonitor.getFailedEventCount()

        assertEquals(3, count)
        coVerify { webhookEventDao.countByStatus(WebhookDeliveryStatus.FAILED) }
    }

    @Test
    fun `getPendingEventCount should handle zero events`() = runTest {
        coEvery { webhookEventDao.countByStatus(WebhookDeliveryStatus.PENDING) } returns 0

        val count = eventMonitor.getPendingEventCount()

        assertEquals(0, count)
    }

    @Test
    fun `getFailedEventCount should handle zero events`() = runTest {
        coEvery { webhookEventDao.countByStatus(WebhookDeliveryStatus.FAILED) } returns 0

        val count = eventMonitor.getFailedEventCount()

        assertEquals(0, count)
    }
}
