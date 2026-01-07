package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.transaction.TransactionRepository
import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException

@Serializable
data class WebhookPayload(
    val eventType: String,
    val transactionId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

class WebhookReceiver(
    private val transactionRepository: TransactionRepository,
    private val webhookQueue: WebhookQueue? = null
) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val TAG = Constants.Tags.WEBHOOK_RECEIVER
    }

    suspend fun setupWebhookListener(webhookUrl: String) {
        Log.d(TAG, "Webhook listener setup completed")
    }

    fun handleWebhookEvent(payload: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (payload.isBlank()) {
                    Log.e(TAG, "Empty webhook payload received")
                    return@launch
                }

                val webhookPayload = parseWebhookPayload(payload)
                
                if (webhookPayload.eventType.isBlank()) {
                    Log.e(TAG, "Invalid webhook payload: missing event type")
                    return@launch
                }

                if (webhookQueue != null) {
                    webhookQueue.enqueue(
                        eventType = webhookPayload.eventType,
                        payload = payload,
                        transactionId = webhookPayload.transactionId,
                        metadata = webhookPayload.metadata
                    )
                    Log.d(TAG, "Webhook event queued: ${webhookPayload.eventType}")
                } else {
                    processImmediately(webhookPayload)
                }
            } catch (e: kotlinx.serialization.SerializationException) {
                Log.e(TAG, "Invalid JSON payload: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling webhook: ${e.message}", e)
            }
        }
    }

    private suspend fun processImmediately(webhookPayload: WebhookPayload) {
        try {
            val sanitizedId = webhookPayload.transactionId?.trim()?.takeIf { it.isNotBlank() }
            if (sanitizedId == null) {
                Log.e(TAG, "Invalid transaction ID: empty or whitespace")
                return
            }

            val transaction = transactionRepository.getTransactionById(sanitizedId)
            if (transaction != null) {
                val status = when (webhookPayload.eventType) {
                    "payment.success" -> PaymentStatus.COMPLETED
                    "payment.failed" -> PaymentStatus.FAILED
                    "payment.refunded" -> PaymentStatus.REFUNDED
                    else -> {
                        Log.d(TAG, "Unknown webhook event type: ${webhookPayload.eventType}")
                        return
                    }
                }
                
                val updatedTransaction = transaction.copy(status = status)
                transactionRepository.updateTransaction(updatedTransaction)
                Log.d(TAG, "Transaction status updated immediately")
            } else {
                Log.e(TAG, "Transaction not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing webhook immediately: ${e.message}", e)
        }
    }

    private fun parseWebhookPayload(payload: String): WebhookPayload {
        return json.decodeFromString<WebhookPayload>(payload)
    }
}


