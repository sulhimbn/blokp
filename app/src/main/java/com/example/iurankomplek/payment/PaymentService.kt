package com.example.iurankomplek.payment

import com.example.iurankomplek.utils.ReceiptGenerator
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.dto.Receipt
import com.example.iurankomplek.utils.OperationResult
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
            when (result) {
                is OperationResult.Success -> {
                    val receipt = receiptGenerator.generateReceipt(result.data)
                    onSuccess(receipt)
                }
                is OperationResult.Error -> {
                    onError(result.message ?: "Unknown error occurred")
                }
                is OperationResult.Loading -> {
                    onError("Payment in progress")
                }
                is OperationResult.Empty -> {
                    onError("No payment result")
                }
            }
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
            when (result) {
                is OperationResult.Success -> {
                    onSuccess(result.data)
                }
                is OperationResult.Error -> {
                    onError(result.message ?: "Unknown error occurred")
                }
                is OperationResult.Loading -> {
                    onError("Refund in progress")
                }
                is OperationResult.Empty -> {
                    onError("No refund result")
                }
            }
        }
    }
}
