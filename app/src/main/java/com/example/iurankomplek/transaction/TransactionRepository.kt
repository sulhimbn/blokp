package com.example.iurankomplek.transaction

import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import java.util.Date

class TransactionRepository @Inject constructor(
    private val paymentGateway: PaymentGateway,
    private val transactionDao: TransactionDao
) {
    suspend fun processPayment(request: PaymentRequest): Result<Transaction> {
        return try {
            val transaction = Transaction.create(request)
            transactionDao.insert(transaction)

            val paymentResult = paymentGateway.processPayment(request)
            paymentResult.onSuccess { response ->
                val updatedTransaction = transaction.copy(
                    status = PaymentStatus.COMPLETED,
                    updatedAt = Date()
                )
                transactionDao.update(updatedTransaction)
            }.onFailure { error ->
                val failedTransaction = transaction.copy(
                    status = PaymentStatus.FAILED,
                    updatedAt = Date()
                )
                transactionDao.update(failedTransaction)
            }

            paymentResult.map { transaction }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUserId(userId)
    }

    fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByStatus(status)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}