package com.example.iurankomplek.network.interceptor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ExecutorService

class PriorityDispatcher(
    private val maxRequestsPerHost: Int = 5,
    private val maxRequests: Int = 64
) {
    private val mutex = Mutex()
    private val requestCounter = AtomicInteger(0)
    private val okHttpDispatcher = Dispatcher()

    private val criticalQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val highQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val normalQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val lowQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })
    private val backgroundQueue = PriorityQueue<PriorityRequest>(compareBy { it.timestamp })

    private val dispatcherJob = Job()
    private val executorScope = CoroutineScope(dispatcherJob + Dispatchers.IO)

    private val executor: ExecutorService = Executors.newCachedThreadPool { r ->
        Thread(r).apply { isDaemon = true }
    }

    private data class PriorityRequest(
        val request: Request,
        val priority: RequestPriority,
        val timestamp: Long = System.currentTimeMillis()
    )

    init {
        okHttpDispatcher.maxRequests = maxRequests
        okHttpDispatcher.maxRequestsPerHost = maxRequestsPerHost
        okHttpDispatcher.executorService(executor)
    }

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

        if (dispatcherJob.isActive) {
            processQueues()
        }
    }

    private fun processQueues() {
        if (!dispatcherJob.isActive) return

        executorScope.launch {
            while (hasQueuedRequests() && dispatcherJob.isActive) {
                val nextRequest = mutex.withLock {
                    val runningRequests = requestCounter.get()
                    val availableSlots = maxRequests - runningRequests

                    if (availableSlots <= 0) return@withLock null

                    val request = getNextRequest() ?: return@withLock null
                    requestCounter.incrementAndGet()
                    request
                }

                nextRequest?.let { req ->
                    executor.execute {
                        try {
                            okHttpDispatcher.enqueue(req)
                        } catch (e: Exception) {
                            requestCounter.decrementAndGet()
                        }
                    }
                }
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
        if (dispatcherJob.isActive) {
            processQueues()
        }
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
        criticalQueue.clear()
        highQueue.clear()
        normalQueue.clear()
        lowQueue.clear()
        backgroundQueue.clear()
        requestCounter.set(0)
        okHttpDispatcher.cancelAll()
    }

    fun getDispatcher(): Dispatcher {
        return okHttpDispatcher
    }

    fun getMaxRequestsPerHost(): Int {
        return maxRequestsPerHost
    }

    fun getMaxRequests(): Int {
        return maxRequests
    }

    fun shutdown() {
        dispatcherJob.cancel()
        executor.shutdown()
    }
}
