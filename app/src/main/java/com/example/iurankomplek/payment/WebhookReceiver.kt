package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.IOException

class WebhookReceiver(
    private val transactionRepository: TransactionRepository
) {
    private val client = OkHttpClient()
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

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
                // Parse the webhook payload (simplified for this example)
                val event = parseWebhookPayload(payload)
                
                when (event.eventType) {
                    "payment.success" -> {
                        updateTransactionStatus(event.transactionId, PaymentStatus.COMPLETED)
                    }
                    "payment.failed" -> {
                        updateTransactionStatus(event.transactionId, PaymentStatus.FAILED)
                    }
                    "payment.refunded" -> {
                        updateTransactionStatus(event.transactionId, PaymentStatus.REFUNDED)
                    }
                    else -> {
                        Log.d(TAG, "Unknown webhook event: ${event.eventType}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling webhook: ${e.message}", e)
            }
        }
    }

    private suspend fun updateTransactionStatus(transactionId: String, status: PaymentStatus) {
        try {
            val transaction = transactionRepository.getTransactionById(transactionId)
            if (transaction != null) {
                val updatedTransaction = transaction.copy(
                    status = status,
                    // In a real implementation, updatedAt would be current timestamp
                )
                transactionRepository.updateTransaction(updatedTransaction)
                Log.d(TAG, "Transaction $transactionId updated to status: $status")
            } else {
                Log.e(TAG, "Transaction not found: $transactionId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction status: ${e.message}", e)
        }
    }

    private fun parseWebhookPayload(payload: String): WebhookEvent {
        // This is a simplified parser - in a real implementation, you'd use proper JSON parsing
        // For this demo, we'll just create a basic event based on the payload
        return WebhookEvent(
            eventType = extractEventType(payload),
            transactionId = extractTransactionId(payload)
        )
    }

    private fun extractEventType(payload: String): String {
        // Simplified extraction - in real implementation, parse JSON properly
        return if (payload.contains("success")) {
            "payment.success"
        } else if (payload.contains("failed")) {
            "payment.failed"
        } else if (payload.contains("refunded")) {
            "payment.refunded"
        } else {
            "unknown"
        }
    }

    private fun extractTransactionId(payload: String): String {
        // Simplified extraction - in real implementation, parse JSON properly
        return "transaction_id_from_payload" // This would be extracted from the actual payload
    }
}

data class WebhookEvent(
    val eventType: String,
    val transactionId: String
)