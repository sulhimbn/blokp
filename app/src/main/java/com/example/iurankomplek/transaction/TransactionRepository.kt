package com.example.iurankomplek.transaction

import com.example.iurankomplek.model.PaymentResponse
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.RefundResponse
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun processPayment(request: PaymentRequest): Result<Transaction>
    suspend fun getTransactionById(id: String): Transaction?
    fun getTransactionsByUserId(userId: String): Flow<List<Transaction>>
    fun getTransactionsByStatus(status: com.example.iurankomplek.payment.PaymentStatus): Flow<List<Transaction>>
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: String,
        paymentMethod: String
    ): Result<com.example.iurankomplek.model.PaymentResponse>
    suspend fun refundPayment(transactionId: String, reason: String?): Result<RefundResponse>
}
