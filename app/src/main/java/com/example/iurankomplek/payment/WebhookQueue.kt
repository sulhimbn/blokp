package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.transaction.TransactionRepository
import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import kotlin.math.min
import kotlin.random.Random

class WebhookQueue(
    private val webhookEventDao: WebhookEventDao,
    private val transactionRepository: TransactionRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val eventChannel = Channel<Long>(capacity = Channel.UNLIMITED)
    private val json = Json { ignoreUnknownKeys = true }
    private val secureRandom = SecureRandom()

    private var isProcessing = false
    private var processingJob: Job? = null

    companion object {
        private val TAG = "${Constants.Tags.WEBHOOK_RECEIVER}.Queue"

        fun generateIdempotencyKey(): String {
            val timestamp = System.currentTimeMillis()
            val random = SecureRandom().nextInt()
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
                Log.w(TAG, "Failed to enrich payload, using original: ${e.message}")
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
        Log.d(TAG, "Enqueued webhook event: $id, type: $eventType, key: $idempotencyKey")
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
                Log.e(TAG, "Error processing queue: ${e.message}", e)
                delay(1000)
            }
        }
    }

    private suspend fun processEvent(eventId: Long) {
        val event = webhookEventDao.getEventById(eventId)
        if (event == null) {
            Log.w(TAG, "Event $eventId not found")
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
            
            val success = processWebhookPayload(event)

            if (success) {
                webhookEventDao.markAsDelivered(eventId)
                Log.d(TAG, "Webhook event $eventId delivered successfully")
            } else {
                throw Exception("Webhook processing returned false")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing webhook event $eventId: ${e.message}", e)
            
            if (event.retryCount >= event.maxRetries) {
                webhookEventDao.markAsFailed(eventId)
                Log.e(TAG, "Webhook event $eventId failed after ${event.retryCount} retries")
            } else {
                val nextRetryDelay = calculateRetryDelay(event.retryCount)
                val nextRetryAt = System.currentTimeMillis() + nextRetryDelay
                
                webhookEventDao.updateRetryInfo(
                    id = eventId,
                    retryCount = event.retryCount + 1,
                    nextRetryAt = nextRetryAt,
                    lastError = e.message?.take(500)
                )
                
                Log.d(TAG, "Webhook event $eventId scheduled for retry #${event.retryCount + 1} in ${nextRetryDelay}ms")
                eventChannel.send(eventId)
            }
        }
    }

    private suspend fun processWebhookPayload(event: WebhookEvent): Boolean {
        return try {
            val webhookPayload = json.decodeFromString<WebhookPayload>(event.payload)
            
            when (webhookPayload.eventType) {
                "payment.success" -> {
                    updateTransactionStatus(webhookPayload.transactionId, PaymentStatus.COMPLETED)
                }
                "payment.failed" -> {
                    updateTransactionStatus(webhookPayload.transactionId, PaymentStatus.FAILED)
                }
                "payment.refunded" -> {
                    updateTransactionStatus(webhookPayload.transactionId, PaymentStatus.REFUNDED)
                }
                else -> {
                    Log.d(TAG, "Unknown webhook event type: ${webhookPayload.eventType}")
                    return true
                }
            }
            
            true
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e(TAG, "Invalid JSON payload for event $event: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error processing webhook payload for event $event: ${e.message}", e)
            false
        }
    }

    private suspend fun updateTransactionStatus(transactionId: String?, status: PaymentStatus): Boolean {
        if (transactionId.isNullOrBlank()) {
            Log.w(TAG, "Transaction ID is null or blank")
            return false
        }

        return try {
            val sanitizedId = transactionId.trim().takeIf { it.isNotBlank() }
            if (sanitizedId == null) {
                Log.e(TAG, "Invalid transaction ID")
                return false
            }

            val transaction = transactionRepository.getTransactionById(sanitizedId)
            if (transaction != null) {
                val updatedTransaction = transaction.copy(status = status)
                transactionRepository.updateTransaction(updatedTransaction)
                true
            } else {
                Log.e(TAG, "Transaction not found: $sanitizedId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction status: ${e.message}", e)
            false
        }
    }

    internal fun calculateRetryDelay(retryCount: Int): Long {
        val exponentialDelay = (Constants.Webhook.INITIAL_RETRY_DELAY_MS *
            Math.pow(Constants.Webhook.RETRY_BACKOFF_MULTIPLIER, retryCount.toDouble())).toLong()

        val cappedDelay = min(exponentialDelay, Constants.Webhook.MAX_RETRY_DELAY_MS)

        val jitterMin = -Constants.Webhook.RETRY_JITTER_MS
        val jitterMax = Constants.Webhook.RETRY_JITTER_MS
        val jitter = secureRandom.nextLong() % (jitterMax - jitterMin + 1) + jitterMin

        return (cappedDelay + jitter).coerceAtLeast(0)
    }

    suspend fun retryFailedEvents(limit: Int = 50): Int {
        val cutoffTime = System.currentTimeMillis()
        val failedEvents = webhookEventDao.getPendingEventsByStatus(
            status = WebhookDeliveryStatus.FAILED,
            currentTime = cutoffTime,
            limit = limit
        )
        
        var retriedCount = 0
        for (event in failedEvents) {
            webhookEventDao.updateStatus(
                event.id,
                WebhookDeliveryStatus.PENDING
            )
            eventChannel.send(event.id)
            retriedCount++
        }
        
        Log.d(TAG, "Retrying $retriedCount failed events")
        return retriedCount
    }

    suspend fun cleanupOldEvents(): Int {
        val cutoffTime = System.currentTimeMillis() - 
            (Constants.Webhook.MAX_EVENT_RETENTION_DAYS * 24L * 60L * 60L * 1000L)
        
        val deletedCount = webhookEventDao.deleteEventsOlderThan(cutoffTime)
        Log.d(TAG, "Cleaned up $deletedCount old webhook events")
        return deletedCount
    }

    suspend fun getPendingEventCount(): Int {
        return webhookEventDao.countByStatus(WebhookDeliveryStatus.PENDING)
    }

    suspend fun getFailedEventCount(): Int {
        return webhookEventDao.countByStatus(WebhookDeliveryStatus.FAILED)
    }

    fun destroy() {
        stopProcessing()
        eventChannel.close()
        scope.cancel()
    }
}
