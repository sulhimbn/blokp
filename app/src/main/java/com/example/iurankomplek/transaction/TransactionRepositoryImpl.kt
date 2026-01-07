package com.example.iurankomplek.transaction

import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.payment.RefundResponse
import kotlinx.coroutines.flow.Flow
import java.util.Date
import kotlin.Result

class TransactionRepositoryImpl(
    private val paymentGateway: PaymentGateway,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: String,
        paymentMethod: String
    ): Result<com.example.iurankomplek.model.PaymentResponse> {
        return try {
            val paymentRequest = PaymentRequest(
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
            val kotlinResult: Result<com.example.iurankomplek.payment.PaymentResponse> = paymentGateway.processPayment(paymentRequest)
            return when (kotlinResult) {
                is Result.Success -> kotlinResult.getOrThrow().toApiPaymentResponse()
                is Result.Failure -> throw kotlinResult.exception ?: Exception("Unknown error")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processPayment(request: PaymentRequest): Result<Transaction> {
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

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
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

    override suspend fun refundPayment(transactionId: String, reason: String?): Result<RefundResponse> {
        return try {
            val refundResult = paymentGateway.refundPayment(transactionId)

            refundResult.onSuccess { response ->
                val originalTransaction = getTransactionById(transactionId)
                if (originalTransaction != null) {
                    val refundedTransaction = originalTransaction.copy(
                        status = PaymentStatus.REFUNDED,
                        updatedAt = Date()
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
