package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.payment.toApiPaymentResponse
import com.example.iurankomplek.transaction.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDao
import java.util.Calendar
import javax.inject.Inject

/**
 * Implementation of TransactionRepository
 * Handles payment processing, refunds, and transaction database operations
 */
class TransactionRepositoryImpl @Inject constructor(
    private val paymentGateway: PaymentGateway,
    private val transactionDao: TransactionDao
) : TransactionRepository {
    
    override suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: String,
        paymentMethod: String
    ): Result<PaymentResponse> {
        return try {
            val response = paymentGateway.processPayment(
                PaymentRequest(
                    amount = java.math.BigDecimal(amount),
                    description = description,
                    customerId = customerId,
                    paymentMethod = when (paymentMethod) {
                        "CREDIT_CARD" -> PaymentMethod.CREDIT_CARD
                        "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER
                        "E_WALLET" -> PaymentMethod.E_WALLET
                        "VIRTUAL_ACCOUNT" -> PaymentMethod.VIRTUAL_ACCOUNT
                        else -> PaymentMethod.CREDIT_CARD
                    }
                )
            )
            response.mapCatching { it.toApiPaymentResponse() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun processPayment(request: PaymentRequest): Result<Transaction> {
        return try {
            val transaction = Transaction.create(request)
            transactionDao.insert(transaction)

            val paymentResult = paymentGateway.processPayment(request)
            paymentResult.onSuccess { _ ->
                val updatedTransaction = transaction.copy(
                    status = PaymentStatus.COMPLETED,
                    updatedAt = Calendar.getInstance().time
                )
                transactionDao.update(updatedTransaction)
            }.onFailure { _ ->
                val failedTransaction = transaction.copy(
                    status = PaymentStatus.FAILED,
                    updatedAt = Calendar.getInstance().time
                )
                transactionDao.update(failedTransaction)
            }

            paymentResult.map { transaction }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    override fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUserId(userId)
    }

    override fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByStatus(status)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    override suspend fun refundPayment(
        transactionId: String,
        reason: String?
    ): Result<RefundResponse> {
        return try {
            val refundResult = paymentGateway.refundPayment(transactionId)
            
            refundResult.onSuccess { _ ->
                // Update the original transaction status to REFUNDED
                val originalTransaction = getTransactionById(transactionId)
                if (originalTransaction != null) {
                    val refundedTransaction = originalTransaction.copy(
                        status = PaymentStatus.REFUNDED,
                        updatedAt = Calendar.getInstance().time
                    )
                    transactionDao.update(refundedTransaction)
                }
            }
            
            refundResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}
