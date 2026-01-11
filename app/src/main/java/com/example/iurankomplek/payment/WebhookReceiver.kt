package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WebhookPayload(
    val eventType: String,
    val transactionId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

class WebhookReceiver(
    private val transactionRepository: TransactionRepository,
    private val webhookQueue: WebhookQueue? = null,
    private val signatureVerifier: WebhookSignatureVerifier = WebhookSignatureVerifier()
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val TAG = Constants.Tags.WEBHOOK_RECEIVER
        private const val SIGNATURE_HEADER = "X-Webhook-Signature"
    }

    suspend fun setupWebhookListener(_webhookUrl: String) {
        Log.d(TAG, "Webhook listener setup completed")
    }

    fun handleWebhookEvent(
        payload: String,
        headers: Map<String, String> = emptyMap()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (payload.isBlank()) {
                    Log.e(TAG, "Empty webhook payload received")
                    return@launch
                }

                val signature = WebhookSignatureVerifier.extractSignature(headers)
                val verificationResult = signatureVerifier.verifyWebhookSignature(payload, signature)

                when (verificationResult) {
                    is WebhookVerificationResult.Valid -> {
                        processWebhook(payload)
                    }
                    is WebhookVerificationResult.Invalid -> {
                        Log.e(TAG, "Invalid webhook signature")
                        return@launch
                    }
                    is WebhookVerificationResult.Skipped -> {
                        Log.w(TAG, "Webhook signature verification skipped")
                        processWebhook(payload)
                    }
                }
            } catch (e: kotlinx.serialization.SerializationException) {
                Log.e(TAG, "Invalid JSON payload")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling webhook")
            }
        }
    }

    private suspend fun processWebhook(payload: String) {
        try {
            val webhookPayload = parseWebhookPayload(payload)

            if (webhookPayload.eventType.isBlank()) {
                Log.e(TAG, "Invalid webhook payload: missing event type")
                return
            }

            if (webhookQueue != null) {
                webhookQueue.enqueue(
                    eventType = webhookPayload.eventType,
                    payload = payload,
                    transactionId = webhookPayload.transactionId,
                    metadata = webhookPayload.metadata
                )
            } else {
                processImmediately(webhookPayload)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing webhook")
        }
    }

    private suspend fun processImmediately(webhookPayload: WebhookPayload) {
        try {
            val sanitizedId = webhookPayload.transactionId?.trim()?.takeIf { it.isNotBlank() }
            if (sanitizedId == null) {
                return
            }

            val transaction = transactionRepository.getTransactionById(sanitizedId)
            if (transaction != null) {
                val status = when (webhookPayload.eventType) {
                    "payment.success" -> PaymentStatus.COMPLETED
                    "payment.failed" -> PaymentStatus.FAILED
                    "payment.refunded" -> PaymentStatus.REFUNDED
                    else -> {
                        return
                    }
                }
                
                val updatedTransaction = transaction.copy(status = status)
                transactionRepository.updateTransaction(updatedTransaction)
            } else {
                Log.e(TAG, "Transaction not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing webhook immediately")
        }
    }

    private fun parseWebhookPayload(payload: String): WebhookPayload {
        return json.decodeFromString<WebhookPayload>(payload)
    }
}


