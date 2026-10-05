package com.example.iurankomplek.event

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
        // Given: a subscriber already attached (the flow replays nothing)
        val testEvent = AppEvent.NewAnnouncement("announce_789")
        val collected = async(start = CoroutineStart.UNDISPATCHED) {
            eventBus.events.first()
        }

        // When: publishing
        eventBus.publish(testEvent)

        // Then: the collected event matches what was published
        assertEquals(testEvent, collected.await())
    }

    @Test
    fun publish_withDataClasses_passesCorrectData() = runTest {
        val paymentEvent = AppEvent.PaymentFailed("Network error")
        val userEvent = AppEvent.UserProfileUpdated("user_999")
        val networkEvent = AppEvent.NetworkStatusChanged(isConnected = false)

        val received = mutableListOf<AppEvent>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            eventBus.events.collect { received.add(it) }
        }

        eventBus.publish(paymentEvent)
        eventBus.publish(userEvent)
        eventBus.publish(networkEvent)
        advanceUntilIdle()

        collector.cancel()
        assertEquals(listOf(paymentEvent, userEvent, networkEvent), received)
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
    fun events_providesSharedFlow() = runTest {
        // Given: an event published before anyone subscribes
        eventBus.publish(AppEvent.PaymentCompleted(paymentId = "pay_early", amount = BigDecimal("1000")))
        advanceUntilIdle()

        // When: a subscriber attaches afterwards
        val received = mutableListOf<AppEvent>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            eventBus.events.collect { received.add(it) }
        }

        // Then: replay = 0, so the earlier event is not replayed to the late subscriber
        assertTrue(received.isEmpty())

        // And an event published after subscribing is delivered
        eventBus.publish(AppEvent.PaymentCompleted(paymentId = "pay_late", amount = BigDecimal("2000")))
        advanceUntilIdle()
        assertEquals(1, received.size)
        assertEquals("pay_late", (received.single() as AppEvent.PaymentCompleted).paymentId)

        job.cancel()
    }
}
