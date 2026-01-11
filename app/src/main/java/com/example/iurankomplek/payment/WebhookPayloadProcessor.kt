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
                    return true
                }
            }
            
            true
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e(TAG, "Invalid JSON payload")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error processing webhook payload")
            false
        }
    }

    private suspend fun updateTransactionStatus(transactionId: String?, status: PaymentStatus): Boolean {
        if (transactionId.isNullOrBlank()) {
            return false
        }

        return try {
            val sanitizedId = transactionId.trim().takeIf { it.isNotBlank() }
            if (sanitizedId == null) {
                return false
            }

            val transaction = transactionRepository.getTransactionById(sanitizedId)
            if (transaction != null) {
                val updatedTransaction = transaction.copy(status = status)
                transactionRepository.updateTransaction(updatedTransaction)
                true
            } else {
                Log.e(TAG, "Transaction not found")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction status")
            false
        }
    }
}
