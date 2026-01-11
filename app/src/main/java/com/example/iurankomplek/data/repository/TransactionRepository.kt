package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.model.PaymentResponse
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun processPayment(request: PaymentRequest): OperationResult<Transaction>
    suspend fun getTransactionById(id: String): Transaction?
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByUserId(userId: Long): Flow<List<Transaction>>
    fun getTransactionsByStatus(status: com.example.iurankomplek.payment.PaymentStatus): Flow<List<Transaction>>
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: Long,
        paymentMethod: String
    ): OperationResult<com.example.iurankomplek.model.PaymentResponse>
    suspend fun refundPayment(transactionId: String, reason: String?): OperationResult<RefundResponse>
}
