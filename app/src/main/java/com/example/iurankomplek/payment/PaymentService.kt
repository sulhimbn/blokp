package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.TransactionRepository
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
        onSuccess: (com.example.iurankomplek.receipt.Receipt) -> Unit,
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
}