package com.example.iurankomplek.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * EventBus uses a SharedFlow with replay=0, so a subscriber only sees events published
 * after it started collecting. Every delivery test therefore registers a collector first
 * and asserts on what it received.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventBusTest {

    private lateinit var eventBus: EventBus

    @Before
    fun setup() {
        eventBus = EventBus()
    }

    private fun collectInto(scope: TestScope, sink: MutableList<AppEvent>) {
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
            eventBus.events.collect { sink.add(it) }
        }
        scope.runCurrent()
    }

    @Test
    fun `publish reports success`() = runTest {
        assertTrue(eventBus.publish(AppEvent.UserLoggedOut))
    }

    @Test
    fun `publishBlocking emits to an active subscriber`() = runTest {
        val received = mutableListOf<AppEvent>()
        collectInto(this, received)

        eventBus.publishBlocking(AppEvent.UserLoggedIn("user_123"))
        runCurrent()

        assertEquals(listOf(AppEvent.UserLoggedIn("user_123")), received)
    }

    @Test
    fun `publish delivers the event to an active subscriber`() = runTest {
        val received = mutableListOf<AppEvent>()
        collectInto(this, received)

        eventBus.publish(AppEvent.PaymentCompleted("pay_123", BigDecimal("100000")))
        runCurrent()

        assertEquals(listOf(AppEvent.PaymentCompleted("pay_123", BigDecimal("100000"))), received)
    }

    @Test
    fun `a subscriber receives every published event in order`() = runTest {
        val received = mutableListOf<AppEvent>()
        collectInto(this, received)

        eventBus.publish(AppEvent.UserLoggedIn("user_1"))
        eventBus.publish(AppEvent.FinancialDataUpdated)
        eventBus.publish(AppEvent.NewAnnouncement("ann_1"))
        runCurrent()

        assertEquals(
            listOf(
                AppEvent.UserLoggedIn("user_1"),
                AppEvent.FinancialDataUpdated,
                AppEvent.NewAnnouncement("ann_1")
            ),
            received
        )
    }

    @Test
    fun `multiple subscribers each receive the same event`() = runTest {
        val first = mutableListOf<AppEvent>()
        val second = mutableListOf<AppEvent>()
        collectInto(this, first)
        collectInto(this, second)

        eventBus.publish(AppEvent.CacheCleared)
        runCurrent()

        assertEquals(listOf(AppEvent.CacheCleared), first)
        assertEquals(listOf(AppEvent.CacheCleared), second)
    }

    @Test
    fun `the replay cache stays empty so late subscribers miss earlier publishes`() = runTest {
        eventBus.publish(AppEvent.CacheCleared)

        assertTrue(eventBus.events.replayCache.isEmpty())
    }

    @Test
    fun `every AppEvent variant can be published`() = runTest {
        val events = listOf(
            AppEvent.PaymentCompleted("pay_1", BigDecimal("1000")),
            AppEvent.PaymentFailed("Network error"),
            AppEvent.TransactionCreated("txn_1"),
            AppEvent.TransactionUpdated("txn_1"),
            AppEvent.UserLoggedIn("user_1"),
            AppEvent.UserLoggedOut,
            AppEvent.UserProfileUpdated("user_1"),
            AppEvent.FinancialDataUpdated,
            AppEvent.NetworkStatusChanged(isConnected = false),
            AppEvent.NewAnnouncement("ann_1"),
            AppEvent.AnnouncementRead("ann_1"),
            AppEvent.NewMessage("msg_1", "sender_1"),
            AppEvent.MessageRead("msg_1"),
            AppEvent.WorkOrderCreated("wo_1"),
            AppEvent.WorkOrderUpdated("wo_1", "IN_PROGRESS"),
            AppEvent.VendorDataUpdated,
            AppEvent.CacheCleared
        )

        val received = mutableListOf<AppEvent>()
        collectInto(this, received)
        events.forEach { eventBus.publish(it) }
        runCurrent()

        assertEquals(events, received)
    }

    @Test
    fun `bursts larger than one are all buffered`() = runTest {
        val burst = (1..64).map { AppEvent.NewMessage("msg_$it", "sender") }
        val received = mutableListOf<AppEvent>()
        collectInto(this, received)

        burst.forEach { eventBus.publish(it) }
        runCurrent()

        assertEquals(burst, received)
    }

    @Test
    fun `a subscriber that cancels stops receiving`() = runTest {
        val collected = mutableListOf<AppEvent>()
        collectInto(this, collected)

        eventBus.publish(AppEvent.NewMessage("msg_1", "sender_1"))
        runCurrent()
        backgroundScope.coroutineContext[kotlinx.coroutines.Job]!!.children.first().cancel()
        runCurrent()
        eventBus.publish(AppEvent.NewMessage("msg_2", "sender_1"))
        runCurrent()

        assertEquals(1, collected.size)
        assertEquals(AppEvent.NewMessage("msg_1", "sender_1"), collected[0])
    }

    @Test
    fun `publish does not throw when there are no subscribers`() = runTest {
        assertTrue(eventBus.publish(AppEvent.VendorDataUpdated))
        assertTrue(eventBus.events.replayCache.isEmpty())
    }
}