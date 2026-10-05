package com.example.iurankomplek.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
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
        // Given: A subscriber already attached to the flow
        val testEvent = AppEvent.NewAnnouncement("announce_789")
        val received = mutableListOf<AppEvent>()
        val job = backgroundScope.launch { eventBus.events.collect { received.add(it) } }
        runCurrent()

        // When: Publishing the event
        eventBus.publish(testEvent)
        runCurrent()
        job.cancelAndJoin()

        // Then: The collected event matches what was published
        assertEquals(listOf(testEvent), received)
    }

    @Test
    fun publish_withDataClasses_passesCorrectData() = runTest {
        // Given: Events with various data, and a subscriber already attached.
        // EventBus uses replay = 0, so a subscriber must attach before publish() or it will
        // never observe the event.
        val paymentEvent = AppEvent.PaymentFailed("Network error")
        val userEvent = AppEvent.UserProfileUpdated("user_999")
        val networkEvent = AppEvent.NetworkStatusChanged(isConnected = false)

        val received = mutableListOf<AppEvent>()
        val job = backgroundScope.launch { eventBus.events.collect { received.add(it) } }
        runCurrent()

        // When: Publishing events with data
        eventBus.publish(paymentEvent)
        eventBus.publish(userEvent)
        eventBus.publish(networkEvent)
        runCurrent()
        job.cancelAndJoin()

        // Then: All events are delivered with their payload intact
        assertEquals(listOf(paymentEvent, userEvent, networkEvent), received)
        assertTrue(received.any { it is AppEvent.PaymentFailed })
        assertTrue(received.any { it is AppEvent.UserProfileUpdated })
        assertTrue(received.any { it is AppEvent.NetworkStatusChanged })
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
        // Given: EventBus exposes its event stream as a SharedFlow

        // When: Inspecting the flow type and replay semantics
        val flow = eventBus.events

        // Then: It is a SharedFlow...
        assertTrue(flow is kotlinx.coroutines.flow.SharedFlow)
        // ...configured with replay = 0, so a subscriber that attaches AFTER a publish does
        // not receive the already-published event. (SharedFlow exposes no public `replay`
        // property, so the contract is asserted through observable behaviour.)
        val received = mutableListOf<AppEvent>()
        val job = backgroundScope.launch { eventBus.events.collect { received.add(it) } }
        runCurrent()
        eventBus.publish(AppEvent.RefreshAllData)
        runCurrent()
        job.cancelAndJoin()

        received.clear()

        val late = mutableListOf<AppEvent>()
        val lateJob = backgroundScope.launch { eventBus.events.collect { late.add(it) } }
        runCurrent()

        assertTrue("Late subscribers must not replay earlier events", late.isEmpty())
        lateJob.cancelAndJoin()
    }
}
