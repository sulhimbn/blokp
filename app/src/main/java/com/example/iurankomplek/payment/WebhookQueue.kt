package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.SecureRandom

sealed class WebhookException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ProcessingFailed(message: String = "Webhook processing returned false", cause: Throwable? = null) : WebhookException(message, cause)
}

class WebhookQueue(
    private val webhookEventDao: WebhookEventDao,
    private val transactionRepository: TransactionRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val eventChannel = Channel<Long>(capacity = Channel.UNLIMITED)
    private val json = Json { ignoreUnknownKeys = true }

    private val retryCalculator = WebhookRetryCalculator()
    private val payloadProcessor = WebhookPayloadProcessor(transactionRepository)
    private val eventCleaner = WebhookEventCleaner(webhookEventDao, eventChannel)
    private val eventMonitor = WebhookEventMonitor(webhookEventDao)

    private var isProcessing = false
    private var processingJob: Job? = null

    companion object {
        private val TAG = "${Constants.Tags.WEBHOOK_RECEIVER}.Queue"
        private val SECURE_RANDOM = SecureRandom()

        fun generateIdempotencyKey(): String {
            val timestamp = System.currentTimeMillis()
            val random = SECURE_RANDOM.nextInt()
            return "${Constants.Webhook.IDEMPOTENCY_KEY_PREFIX}${timestamp}_${kotlin.math.abs(random)}"
        }
    }

    init {
        startProcessing()
    }

    suspend fun enqueue(
        eventType: String,
        payload: String,
        transactionId: String? = null,
        metadata: Map<String, String>? = null
    ): Long {
        val idempotencyKey = generateIdempotencyKey()

        val enrichedPayload = if (metadata != null) {
            try {
                val parsed = json.decodeFromString<Map<String, Any>>(payload)
                val enriched = parsed.toMutableMap().apply {
                    putAll(metadata)
                    put("idempotencyKey", idempotencyKey)
                    put("enqueuedAt", System.currentTimeMillis())
                }
                json.encodeToString(enriched)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to enrich payload, using original")
                payload
            }
        } else {
            payload
        }

        val webhookEvent = WebhookEvent(
            idempotencyKey = idempotencyKey,
            eventType = eventType,
            payload = enrichedPayload,
            transactionId = transactionId,
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 0,
            maxRetries = Constants.Webhook.MAX_RETRIES
        )

        val id = webhookEventDao.insert(webhookEvent)
        eventChannel.trySend(id)
        return id
    }

    fun stopProcessing() {
        isProcessing = false
        processingJob?.cancel()
        Log.d(TAG, "Webhook queue processing stopped")
    }

    fun startProcessing() {
        if (isProcessing) {
            Log.d(TAG, "Processing already running")
            return
        }
        
        isProcessing = true
        processingJob = scope.launch {
            Log.d(TAG, "Webhook queue processing started")
            processQueue()
        }
    }

    private suspend fun processQueue() {
        while (isProcessing) {
            try {
                val eventId = eventChannel.receive()
                processEvent(eventId)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "Processing cancelled")
                    break
                }
                Log.e(TAG, "Error processing queue")
                delay(Constants.Webhook.INITIAL_RETRY_DELAY_MS)
            }
        }
    }

    private suspend fun processEvent(eventId: Long) {
        val event = webhookEventDao.getEventById(eventId)
        if (event == null) {
            return
        }

        if (event.status == WebhookDeliveryStatus.DELIVERED || 
            event.status == WebhookDeliveryStatus.CANCELLED) {
            return
        }

        if (event.status != WebhookDeliveryStatus.PENDING) {
            val currentTime = System.currentTimeMillis()
            if (event.nextRetryAt != null && event.nextRetryAt > currentTime) {
                delay(event.nextRetryAt - currentTime)
                eventChannel.send(eventId)
                return
            }
        }

        try {
            webhookEventDao.updateStatus(eventId, WebhookDeliveryStatus.PROCESSING)
            
            val success = payloadProcessor.processWebhookPayload(event)

            if (success) {
                webhookEventDao.markAsDelivered(eventId)
            } else {
                throw WebhookException.ProcessingFailed()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing webhook event")
            
            if (event.retryCount >= event.maxRetries) {
                webhookEventDao.markAsFailed(eventId)
                Log.e(TAG, "Webhook event failed after maximum retries")
            } else {
                val nextRetryDelay = retryCalculator.calculateRetryDelay(event.retryCount)
                val nextRetryAt = System.currentTimeMillis() + nextRetryDelay
                
                webhookEventDao.updateRetryInfo(
                    id = eventId,
                    retryCount = event.retryCount + 1,
                    nextRetryAt = nextRetryAt,
                    lastError = e.message?.take(500)
                )
                
                eventChannel.send(eventId)
            }
        }
    }

    suspend fun retryFailedEvents(limit: Int = Constants.Webhook.DEFAULT_RETRY_LIMIT): Int {
        return eventCleaner.retryFailedEvents(limit)
    }

    suspend fun cleanupOldEvents(): Int {
        return eventCleaner.cleanupOldEvents()
    }

    suspend fun getPendingEventCount(): Int {
        return eventMonitor.getPendingEventCount()
    }

    suspend fun getFailedEventCount(): Int {
        return eventMonitor.getFailedEventCount()
    }

    internal fun calculateRetryDelay(retryCount: Int): Long {
        return retryCalculator.calculateRetryDelay(retryCount)
    }

    fun destroy() {
        stopProcessing()
        eventChannel.close()
        scope.cancel()
    }
}
