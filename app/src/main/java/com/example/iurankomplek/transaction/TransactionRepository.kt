package com.example.iurankomplek.transaction

import com.example.iurankomplek.data.repository.TransactionRepository as TransactionRepositoryInterface
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.payment.toApiPaymentResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val paymentGateway: PaymentGateway,
    private val transactionDao: TransactionDao
) : TransactionRepositoryInterface {
    
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
                        "CREDIT_CARD" -> com.example.iurankomplek.payment.PaymentMethod.CREDIT_CARD
                        "BANK_TRANSFER" -> com.example.iurankomplek.payment.PaymentMethod.BANK_TRANSFER
                        "E_WALLET" -> com.example.iurankomplek.payment.PaymentMethod.E_WALLET
                        "VIRTUAL_ACCOUNT" -> com.example.iurankomplek.payment.PaymentMethod.VIRTUAL_ACCOUNT
                        else -> com.example.iurankomplek.payment.PaymentMethod.CREDIT_CARD
                    }
                )
            )
            response.mapCatching { it.toApiPaymentResponse() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Transaction
    override suspend fun processPayment(request: PaymentRequest): Result<Transaction> {
        return try {
            val transaction = Transaction.create(request)
            transactionDao.insert(transaction)

            val paymentResult = paymentGateway.processPayment(request)
            paymentResult.onSuccess { response ->
                val updatedTransaction = transaction.copy(
                    status = PaymentStatus.COMPLETED,
                    updatedAt = Calendar.getInstance().time
                )
                transactionDao.update(updatedTransaction)
            }.onFailure { error ->
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

    @Transaction
    override suspend fun refundPayment(transactionId: String, reason: String?): Result<RefundResponse> {
        return try {
            val refundResult = paymentGateway.refundPayment(transactionId)
            
            refundResult.onSuccess { response ->
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
