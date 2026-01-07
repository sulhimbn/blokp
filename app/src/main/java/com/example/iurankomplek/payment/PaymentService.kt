package com.example.iurankomplek.payment

import com.example.iurankomplek.utils.ReceiptGenerator
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.dto.Receipt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PaymentService(
    private val transactionRepository: TransactionRepository,
    private val receiptGenerator: ReceiptGenerator
) {
    fun processPayment(
        amount: BigDecimal,
        description: String,
        customerId: String,
        paymentMethod: PaymentMethod,
        onSuccess: (Receipt) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
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
    }
    
    fun refundPayment(
        transactionId: String,
        reason: String? = null,
        onSuccess: (RefundResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
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
    }
}