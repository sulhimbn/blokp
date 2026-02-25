package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PaymentService(
    private val transactionRepository: TransactionRepository,
    private val receiptGenerator: ReceiptGenerator,
    private val externalScope: CoroutineScope? = null
) {
    // Managed coroutine scope to prevent memory leaks
    private val serviceScope = externalScope ?: CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val jobs = mutableListOf<Job>()

    fun processPayment(
        amount: BigDecimal,
        description: String,
        customerId: String,
        paymentMethod: PaymentMethod,
        onSuccess: (com.example.iurankomplek.receipt.Receipt) -> Unit,
        onError: (String) -> Unit
    ) {
        val job = serviceScope.launch {
            val request = PaymentRequest(
                amount = amount,
                description = description,
                customerId = customerId,
                paymentMethod = paymentMethod
            )
            
            val result = transactionRepository.processPayment(request)
            result.fold(
                onSuccess = { transaction ->
                    val receipt = receiptGenerator.generateReceipt(transaction)
                    onSuccess(receipt)
                },
                onFailure = { error ->
                    onError(error.message ?: "Unknown error occurred")
                }
            )
        }
        jobs.add(job)
    }
    
    fun refundPayment(
        transactionId: String,
        reason: String? = null,
        onSuccess: (RefundResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val job = serviceScope.launch {
            val result = transactionRepository.refundPayment(transactionId, reason)
            result.fold(
                onSuccess = { response ->
                    onSuccess(response)
                },
                onFailure = { error ->
                    onError(error.message ?: "Unknown error occurred")
                }
            )
        }
        jobs.add(job)
    }

    /**
     * Cleanup method to cancel all running coroutines and prevent memory leaks.
     * Must be called when the service is no longer needed (e.g., in onDestroy for Android components).
     */
    fun destroy() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        serviceScope.cancel()
    }
}
