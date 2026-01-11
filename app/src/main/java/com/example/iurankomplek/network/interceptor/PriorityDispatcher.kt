package com.example.iurankomplek.network.interceptor

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.immutableListOf
import java.util.PriorityQueue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class PriorityDispatcher(
    private val maxRequestsPerHost: Int = 5,
    private val maxRequests: Int = 64
) : Dispatcher() {

    private val mutex = Mutex()
    private val requestCounter = AtomicInteger(0)

    private val criticalQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val highQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val normalQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val lowQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val backgroundQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })

    private data class PriorityRequest(
        val request: Request,
        val priority: RequestPriority,
        val timestamp: Long = System.currentTimeMillis()
    )

    private fun getQueueForPriority(priority: RequestPriority): PriorityQueue<PriorityRequest> {
        return when (priority) {
            RequestPriority.CRITICAL -> criticalQueue
            RequestPriority.HIGH -> highQueue
            RequestPriority.NORMAL -> normalQueue
            RequestPriority.LOW -> lowQueue
            RequestPriority.BACKGROUND -> backgroundQueue
        }
    }

    fun enqueueRequest(request: Request) {
        val priority = request.tag(RequestPriority::class.java) ?: RequestPriority.NORMAL
        val priorityRequest = PriorityRequest(request, priority)

        val queue = getQueueForPriority(priority)
        queue.add(priorityRequest)

        processQueues()
    }

    private fun processQueues() {
        mutex.withLock {
            val runningRequests = requestCounter.get()
            val availableSlots = maxRequests - runningRequests

            if (availableSlots <= 0) return

            var processed = 0
            while (processed < availableSlots && hasQueuedRequests()) {
                val nextRequest = getNextRequest() ?: break
                super.enqueue(nextRequest.request)
                requestCounter.incrementAndGet()
                processed++
            }
        }
    }

    private fun hasQueuedRequests(): Boolean {
        return criticalQueue.isNotEmpty() ||
               highQueue.isNotEmpty() ||
               normalQueue.isNotEmpty() ||
               lowQueue.isNotEmpty() ||
               backgroundQueue.isNotEmpty()
    }

    private fun getNextRequest(): Request? {
        return when {
            criticalQueue.isNotEmpty() -> criticalQueue.poll()?.request
            highQueue.isNotEmpty() -> highQueue.poll()?.request
            normalQueue.isNotEmpty() -> normalQueue.poll()?.request
            lowQueue.isNotEmpty() -> lowQueue.poll()?.request
            backgroundQueue.isNotEmpty() -> backgroundQueue.poll()?.request
            else -> null
        }
    }

    fun onRequestComplete() {
        requestCounter.decrementAndGet()
        processQueues()
    }

    fun getQueueStats(): Map<RequestPriority, Int> {
        return mapOf(
            RequestPriority.CRITICAL to criticalQueue.size,
            RequestPriority.HIGH to highQueue.size,
            RequestPriority.NORMAL to normalQueue.size,
            RequestPriority.LOW to lowQueue.size,
            RequestPriority.BACKGROUND to backgroundQueue.size
        )
    }

    fun reset() {
        mutex.withLock {
            criticalQueue.clear()
            highQueue.clear()
            normalQueue.clear()
            lowQueue.clear()
            backgroundQueue.clear()
            requestCounter.set(0)
        }
    }

    override fun cancelAll() {
        super.cancelAll()
        reset()
    }
}
