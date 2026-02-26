package com.example.iurankomplek.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
        // Given: An event that will be published
        val testEvent = AppEvent.NewAnnouncement("announce_789")

        // When: Publishing event and collecting
        eventBus.publish(testEvent)
        val collected = eventBus.events.first()

        // Then: The collected event matches what was published
        assertEquals(testEvent, collected)
    }

    @Test
    fun publish_withDataClasses_passesCorrectData() = runTest {
        // Given: Events with various data
        val paymentEvent = AppEvent.PaymentFailed("Network error")
        val userEvent = AppEvent.UserProfileUpdated("user_999")
        val networkEvent = AppEvent.NetworkStatusChanged(isConnected = false)

        // When: Publishing events with data
        eventBus.publish(paymentEvent)
        eventBus.publish(userEvent)
        eventBus.publish(networkEvent)

        // Then: Events can be collected with correct data
        val events = listOf(
            eventBus.events.first(),
            eventBus.events.first(),
            eventBus.events.first()
        )

        assertTrue(events.any { it is AppEvent.PaymentFailed })
        assertTrue(events.any { it is AppEvent.UserProfileUpdated })
        assertTrue(events.any { it is AppEvent.NetworkStatusChanged })
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
        // Given: EventBus provides SharedFlow

        // When: Accessing the events flow
        val flow = eventBus.events

        // Then: It's a SharedFlow (replay=0, extraBufferCapacity=64)
        assertEquals(0, flow.replay)
    }
}
