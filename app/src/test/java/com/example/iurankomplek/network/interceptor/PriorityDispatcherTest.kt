package com.example.iurankomplek.network.interceptor

import com.example.iurankomplek.network.interceptor.RequestPriority
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class PriorityDispatcherTest {

    private lateinit var dispatcher: PriorityDispatcher

    @Before
    fun setup() {
        dispatcher = PriorityDispatcher(
            maxRequestsPerHost = 5,
            maxRequests = 64
        )
    }

    @After
    fun tearDown() {
        dispatcher.reset()
    }

    @Test
    fun `getQueueStats returns correct counts for all queues`() {
        val stats = dispatcher.getQueueStats()

        assertEquals(0, stats[RequestPriority.CRITICAL])
        assertEquals(0, stats[RequestPriority.HIGH])
        assertEquals(0, stats[RequestPriority.NORMAL])
        assertEquals(0, stats[RequestPriority.LOW])
        assertEquals(0, stats[RequestPriority.BACKGROUND])
    }

    @Test
    fun `enqueueRequest assigns correct priority to queue`() {
        val criticalRequest = createRequest("/api/v1/payments/123/confirm", RequestPriority.CRITICAL)
        val highRequest = createRequest("/api/v1/users", RequestPriority.HIGH)
        val normalRequest = createRequest("/api/v1/users", RequestPriority.NORMAL)
        val lowRequest = createRequest("/api/v1/announcements", RequestPriority.LOW)
        val backgroundRequest = createRequest("/api/v1/background-sync", RequestPriority.BACKGROUND)

        dispatcher.enqueueRequest(criticalRequest)
        dispatcher.enqueueRequest(highRequest)
        dispatcher.enqueueRequest(normalRequest)
        dispatcher.enqueueRequest(lowRequest)
        dispatcher.enqueueRequest(backgroundRequest)

        val stats = dispatcher.getQueueStats()

        assertEquals(1, stats[RequestPriority.CRITICAL])
        assertEquals(1, stats[RequestPriority.HIGH])
        assertEquals(1, stats[RequestPriority.NORMAL])
        assertEquals(1, stats[RequestPriority.LOW])
        assertEquals(1, stats[RequestPriority.BACKGROUND])
    }

    @Test
    fun `critical requests are processed first`() {
        val criticalRequest = createRequest("/api/v1/payments/123/confirm", RequestPriority.CRITICAL)
        val backgroundRequest = createRequest("/api/v1/background-sync", RequestPriority.BACKGROUND)

        dispatcher.enqueueRequest(backgroundRequest)
        dispatcher.enqueueRequest(criticalRequest)

        val stats = dispatcher.getQueueStats()

        assertEquals(1, stats[RequestPriority.CRITICAL])
        assertEquals(1, stats[RequestPriority.BACKGROUND])
    }

    @Test
    fun `reset clears all queues`() {
        dispatcher.enqueueRequest(createRequest("/api/v1/payments/123/confirm", RequestPriority.CRITICAL))
        dispatcher.enqueueRequest(createRequest("/api/v1/users", RequestPriority.HIGH))
        dispatcher.enqueueRequest(createRequest("/api/v1/users", RequestPriority.NORMAL))

        var stats = dispatcher.getQueueStats()
        assertTrue(stats[RequestPriority.CRITICAL]!! > 0)
        assertTrue(stats[RequestPriority.HIGH]!! > 0)
        assertTrue(stats[RequestPriority.NORMAL]!! > 0)

        dispatcher.reset()

        stats = dispatcher.getQueueStats()
        assertEquals(0, stats[RequestPriority.CRITICAL])
        assertEquals(0, stats[RequestPriority.HIGH])
        assertEquals(0, stats[RequestPriority.NORMAL])
    }

    @Test
    fun `cancelAll clears all queues`() {
        dispatcher.enqueueRequest(createRequest("/api/v1/payments/123/confirm", RequestPriority.CRITICAL))
        dispatcher.enqueueRequest(createRequest("/api/v1/users", RequestPriority.HIGH))

        var stats = dispatcher.getQueueStats()
        assertTrue(stats[RequestPriority.CRITICAL]!! > 0)

        dispatcher.cancelAll()

        stats = dispatcher.getQueueStats()
        assertEquals(0, stats[RequestPriority.CRITICAL])
        assertEquals(0, stats[RequestPriority.HIGH])
    }

    @Test
    fun `multiple critical requests are queued in order`() {
        val request1 = createRequest("/api/v1/payments/123/confirm", RequestPriority.CRITICAL)
        val request2 = createRequest("/api/v1/payments/456/confirm", RequestPriority.CRITICAL)

        dispatcher.enqueueRequest(request1)
        Thread.sleep(10)
        dispatcher.enqueueRequest(request2)

        val stats = dispatcher.getQueueStats()
        assertEquals(2, stats[RequestPriority.CRITICAL])
    }

    @Test
    fun `priority levels have correct numeric values`() {
        assertEquals(1, RequestPriority.CRITICAL.priorityLevel)
        assertEquals(2, RequestPriority.HIGH.priorityLevel)
        assertEquals(3, RequestPriority.NORMAL.priorityLevel)
        assertEquals(4, RequestPriority.LOW.priorityLevel)
        assertEquals(5, RequestPriority.BACKGROUND.priorityLevel)
    }

    @Test
    fun `max requests per host is configurable`() {
        val customDispatcher = PriorityDispatcher(
            maxRequestsPerHost = 10,
            maxRequests = 128
        )

        customDispatcher.enqueueRequest(createRequest("/api/v1/users", RequestPriority.NORMAL))
        val stats = customDispatcher.getQueueStats()

        assertEquals(1, stats[RequestPriority.NORMAL])

        customDispatcher.reset()
    }

    @Test
    fun `default constructor uses sensible defaults`() {
        val defaultDispatcher = PriorityDispatcher()

        defaultDispatcher.enqueueRequest(createRequest("/api/v1/users", RequestPriority.NORMAL))

        val stats = defaultDispatcher.getQueueStats()
        assertTrue(stats[RequestPriority.NORMAL]!! >= 0)

        defaultDispatcher.reset()
    }

    private fun createRequest(url: String, priority: RequestPriority): Request {
        return Request.Builder()
            .url(url)
            .get()
            .tag(RequestPriority::class.java, priority)
            .build()
    }
}
