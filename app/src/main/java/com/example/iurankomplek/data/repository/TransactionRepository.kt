package com.example.iurankomplek.data.repository

import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.data.api.models.PaymentResponse
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: String,
        paymentMethod: String
    ): Result<PaymentResponse>

    suspend fun processPayment(request: PaymentRequest): Result<Transaction>

    suspend fun getTransactionById(id: String): Transaction?

    fun getTransactionsByUserId(userId: String): Flow<List<Transaction>>

    fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>>

    suspend fun updateTransaction(transaction: Transaction)

    suspend fun refundPayment(transactionId: String, reason: String?): Result<RefundResponse>

    suspend fun deleteTransaction(transaction: Transaction)
}
