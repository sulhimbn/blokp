package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.Constants
import kotlinx.serialization.json.Json

class WebhookPayloadProcessor(
    private val transactionRepository: TransactionRepository
) {
    private val TAG = "${Constants.Tags.WEBHOOK_RECEIVER}.PayloadProcessor"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun processWebhookPayload(event: WebhookEvent): Boolean {
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
}
