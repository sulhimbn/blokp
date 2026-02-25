package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.PaymentStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction operations
 * Provides abstraction for payment processing, refunds, and transaction queries
 */
interface TransactionRepository {
    
    /**
     * Initiates payment via API
     */
    suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: String,
        paymentMethod: String
    ): Result<PaymentResponse>
    
    /**
     * Processes a payment request
     */
    suspend fun processPayment(request: PaymentRequest): Result<Transaction>
    
    /**
     * Gets transaction by ID
     */
    suspend fun getTransactionById(id: String): Transaction?
    
    /**
     * Gets all transactions for a user
     */
    fun getTransactionsByUserId(userId: String): Flow<List<Transaction>>
    
    /**
     * Gets transactions by payment status
     */
    fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>>
    
    /**
     * Updates an existing transaction
     */
    suspend fun updateTransaction(transaction: Transaction)
    
    /**
     * Processes a refund for a transaction
     */
    suspend fun refundPayment(transactionId: String, reason: String?): Result<RefundResponse>
    
    /**
     * Deletes a transaction
     */
    suspend fun deleteTransaction(transaction: Transaction)
}
