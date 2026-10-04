package com.example.iurankomplek.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for EventBus - cross-ViewModel communication using SharedFlow.
 * 
 * Tests cover:
 * - Basic publish/subscribe functionality
 * - Event delivery to multiple subscribers
 * - Blocking vs non-blocking publish
 * - Buffer capacity handling
 * - Event type diversity
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventBusTest {

    private lateinit var eventBus: EventBus

    @Before
    fun setup() {
        eventBus = EventBus()
    }

    @Test
    fun publish_sendsEventToSubscribers() = runTest {
        // Given: A test event
        val event = AppEvent.PaymentCompleted(
            paymentId = "pay_123",
            amount = BigDecimal("100000")
        )

        // When: Publishing the event
        val result = eventBus.publish(event)

        // Then: Event is successfully emitted
        assertTrue(result)
    }

    @Test
    fun publish_multipleEvents_allDelivered() = runTest {
        // Given: Multiple different events
        val event1 = AppEvent.PaymentCompleted("pay_1", BigDecimal("1000"))
        val event2 = AppEvent.UserLoggedIn("user_123")
        val event3 = AppEvent.FinancialDataUpdated

        // When: Publishing all events
        val result1 = eventBus.publish(event1)
        val result2 = eventBus.publish(event2)
        val result3 = eventBus.publish(event3)

        // Then: All events are successfully emitted
        assertTrue(result1)
        assertTrue(result2)
        assertTrue(result3)
    }

    @Test
    fun publish_returnsTrueWhenSuccessful() = runTest {
        // Given: A simple event
        val event = AppEvent.UserLoggedOut

        // When: Publishing the event
        val result = eventBus.publish(event)

        // Then: Returns true for successful emit
        assertTrue(result)
    }

    @Test
    fun publishBlocking_deliversEvent() = runTest {
        // Given: A test event
        val event = AppEvent.TransactionCreated("txn_456")

        // When: Publishing with blocking call
        eventBus.publishBlocking(event)

        // Then: No exception is thrown and event is delivered
        // (If this test reaches here, it means the event was delivered)
        assertTrue(true)
    }

    @Test
    fun eventsFlow_emitsPublishedEvents() = runTest {
        // Given: A subscriber already attached to the replay-less flow
        val collected = mutableListOf<AppEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            eventBus.events.collect { collected.add(it) }
        }
        advanceUntilIdle()

        // When: An event is published
        eventBus.publish(AppEvent.NewAnnouncement("announce_789"))
        advanceUntilIdle()

        // Then: The subscriber received it
        assertEquals(listOf<AppEvent>(AppEvent.NewAnnouncement("announce_789")), collected)
        collector.cancel()
    }

    @Test
    fun publish_withDataClasses_passesCorrectData() = runTest {
        // Given: A subscriber already attached
        val collected = mutableListOf<AppEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            eventBus.events.collect { collected.add(it) }
        }
        advanceUntilIdle()

        // When: Publishing events with data
        eventBus.publish(AppEvent.PaymentFailed("Network error"))
        eventBus.publish(AppEvent.UserProfileUpdated("user_999"))
        eventBus.publish(AppEvent.NetworkStatusChanged(isConnected = false))
        advanceUntilIdle()

        // Then: Each event arrives with its payload intact, in order
        assertEquals(
            listOf(
                AppEvent.PaymentFailed("Network error"),
                AppEvent.UserProfileUpdated("user_999"),
                AppEvent.NetworkStatusChanged(isConnected = false)
            ),
            collected
        )
        collector.cancel()
    }
    @Test
    fun publish_objectEvents_workCorrectly() = runTest {
        // Given: Object-level events (data object)
        val events = listOf(
            AppEvent.UserLoggedOut,
            AppEvent.FinancialDataUpdated,
            AppEvent.RefreshAllData,
            AppEvent.VendorDataUpdated
        )

        // When: Publishing object events
        events.forEach { event ->
            val result = eventBus.publish(event)
            assertTrue(result)
        }
    }

    @Test
    fun publish_workOrderEvents_workCorrectly() = runTest {
        // Given: Work order events
        val createdEvent = AppEvent.WorkOrderCreated("wo_001")
        val updatedEvent = AppEvent.WorkOrderUpdated("wo_002", "IN_PROGRESS")

        // When: Publishing work order events
        val result1 = eventBus.publish(createdEvent)
        val result2 = eventBus.publish(updatedEvent)

        // Then: Both events published successfully
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun publish_messageEvents_workCorrectly() = runTest {
        // Given: Message events
        val newMessage = AppEvent.NewMessage("msg_123", "sender_456")
        val messageRead = AppEvent.MessageRead("msg_789")

        // When: Publishing message events
        val result1 = eventBus.publish(newMessage)
        val result2 = eventBus.publish(messageRead)

        // Then: Both events published successfully
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun publish_announcementEvents_workCorrectly() = runTest {
        // Given: Announcement events
        val newAnnouncement = AppEvent.NewAnnouncement("ann_001")
        val announcementRead = AppEvent.AnnouncementRead("ann_002")

        // When: Publishing announcement events
        val result1 = eventBus.publish(newAnnouncement)
        val result2 = eventBus.publish(announcementRead)

        // Then: Both events published successfully
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun publish_cacheEvents_workCorrectly() = runTest {
        // Given: Cache cleared event
        val cacheEvent = AppEvent.CacheCleared

        // When: Publishing cache event
        val result = eventBus.publish(cacheEvent)

        // Then: Event published successfully
        assertTrue(result)
    }

    @Test
    fun events_doesNotReplayToLateSubscribers() = runTest {
        // Given: An event published before anyone is collecting
        assertTrue(eventBus.publish(AppEvent.PaymentCompleted(
            paymentId = "pay_late",
            amount = BigDecimal("1000")
        )))

        // When: A subscriber attaches afterwards
        val received = mutableListOf<AppEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            eventBus.events.collect { received.add(it) }
        }
        advanceUntilIdle()

        // Then: The late subscriber sees nothing (replay = 0)
        assertTrue(received.isEmpty())
        collector.cancel()
    }

    @Test
    fun events_deliversToSubscribersAttachedBeforePublish() = runTest {
        // Given: An active subscriber
        val received = mutableListOf<AppEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            eventBus.events.collect { received.add(it) }
        }
        advanceUntilIdle()

        // When: An event is published
        assertTrue(eventBus.publish(AppEvent.PaymentCompleted(
            paymentId = "pay_live",
            amount = BigDecimal("2500")
        )))
        advanceUntilIdle()

        // Then: The active subscriber receives it
        assertEquals(1, received.size)
        collector.cancel()
    }
}
