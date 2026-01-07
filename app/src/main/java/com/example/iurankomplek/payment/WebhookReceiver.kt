package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import okhttp3.*
import java.io.IOException

class WebhookReceiver(
    private val transactionRepository: TransactionRepository
) {
    private val client = OkHttpClient()
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = false
        encodeDefaults = false
    }

    companion object {
        private val TAG = Constants.Tags.WEBHOOK_RECEIVER
    }

    suspend fun setupWebhookListener(webhookUrl: String) {
        // In a real implementation, this would register for webhook events
        // For now, we'll just log that we're ready to receive webhooks
        Log.d(TAG, "Webhook listener setup for URL: $webhookUrl")
    }

    fun handleWebhookEvent(payload: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Validate payload is not empty or whitespace
                if (payload.isBlank()) {
                    Log.e(TAG, "Empty webhook payload received")
                    return@launch
                }

                // Parse the webhook payload using proper JSON deserialization
                val webhookPayload = parseWebhookPayload(payload)
                
                // Validate required fields are present
                if (webhookPayload.eventType.isBlank() || webhookPayload.transactionId.isBlank()) {
                    Log.e(TAG, "Invalid webhook payload: missing required fields")
                    return@launch
                }
                
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
                        Log.d(TAG, "Unknown webhook event: ${webhookPayload.eventType}")
                    }
                }
            } catch (e: kotlinx.serialization.SerializationException) {
                Log.e(TAG, "Invalid JSON payload: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling webhook: ${e.message}", e)
            }
        }
    }

    private suspend fun updateTransactionStatus(transactionId: String, status: PaymentStatus) {
        try {
            // Sanitize transaction ID to prevent injection
            val sanitizedId = transactionId.trim().takeIf { it.isNotBlank() }
            if (sanitizedId == null) {
                Log.e(TAG, "Invalid transaction ID: empty or whitespace")
                return
            }

            val transaction = transactionRepository.getTransactionById(sanitizedId)
            if (transaction != null) {
                val updatedTransaction = transaction.copy(
                    status = status,
                    // In a real implementation, updatedAt would be current timestamp
                )
                transactionRepository.updateTransaction(updatedTransaction)
                Log.d(TAG, "Transaction $sanitizedId updated to status: $status")
            } else {
                Log.e(TAG, "Transaction not found: $sanitizedId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction status: ${e.message}", e)
        }
    }

    private fun parseWebhookPayload(payload: String): WebhookPayload {
        // Use proper JSON deserialization with kotlinx.serialization
        // This prevents injection attacks and ensures type safety
        return json.decodeFromString<WebhookPayload>(payload)
    }
}

@Serializable
data class WebhookPayload(
    val eventType: String,
    val transactionId: String,
    val timestamp: Long? = null,
    val metadata: Map<String, String>? = null
)