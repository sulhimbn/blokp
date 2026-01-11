package com.example.iurankomplek.data.repository

import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.payment.toApiPaymentResponse
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.utils.OperationResult
import com.example.iurankomplek.utils.onSuccess
import com.example.iurankomplek.utils.onError
import com.example.iurankomplek.utils.map
import kotlinx.coroutines.flow.Flow
import java.util.Date

sealed class PaymentException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class UnknownError(override val message: String = "Unknown payment error", override val cause: Throwable? = null) : PaymentException(message, cause)
}

class TransactionRepositoryImpl(
    private val paymentGateway: PaymentGateway,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: Long,
        paymentMethod: String
    ): OperationResult<com.example.iurankomplek.model.PaymentResponse> {
        return try {
            val paymentRequest = PaymentRequest(
                amount = java.math.BigDecimal(amount),
                description = description,
                customerId = customerId.toString(),
                paymentMethod = when (paymentMethod) {
                    "CREDIT_CARD" -> PaymentMethod.CREDIT_CARD
                    "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER
                    "E_WALLET" -> PaymentMethod.E_WALLET
                    "VIRTUAL_ACCOUNT" -> PaymentMethod.VIRTUAL_ACCOUNT
                    else -> PaymentMethod.CREDIT_CARD
                }
            )
            val gatewayResult: OperationResult<com.example.iurankomplek.payment.PaymentResponse> = paymentGateway.processPayment(paymentRequest)
            return when (gatewayResult) {
                is OperationResult.Success -> OperationResult.Success(gatewayResult.data.toApiPaymentResponse())
                is OperationResult.Error -> throw gatewayResult.exception
                else -> throw PaymentException.UnknownError()
            }
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Payment failed")
        }
    }

    override suspend fun processPayment(request: PaymentRequest): OperationResult<Transaction> {
        return try {
            val transaction = Transaction.create(request)
            transactionDao.insert(transaction)

            val paymentResult = paymentGateway.processPayment(request)
            when (paymentResult) {
                is OperationResult.Success -> {
                    val updatedTransaction = transaction.copy(
                        status = PaymentStatus.COMPLETED,
                        updatedAt = Date()
                    )
                    transactionDao.update(updatedTransaction)
                    OperationResult.Success(transaction)
                }
                is OperationResult.Error -> {
                    val failedTransaction = transaction.copy(
                        status = PaymentStatus.FAILED,
                        updatedAt = Date()
                    )
                    transactionDao.update(failedTransaction)
                    OperationResult.Error(paymentResult.exception, paymentResult.message ?: "Payment failed")
                }
                is OperationResult.Loading -> OperationResult.Error(IllegalStateException("Payment still in progress"), "Payment in progress")
                is OperationResult.Empty -> OperationResult.Error(IllegalStateException("No payment result"), "No payment result")
            }
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Payment failed")
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    override fun getTransactionsByUserId(userId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUserId(userId)
    }

    override fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByStatus(status)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    override suspend fun refundPayment(transactionId: String, reason: String?): OperationResult<RefundResponse> {
        return try {
            val refundResult = paymentGateway.refundPayment(transactionId)

            if (refundResult is OperationResult.Success) {
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
            OperationResult.Error(e, e.message ?: "Refund failed")
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}
