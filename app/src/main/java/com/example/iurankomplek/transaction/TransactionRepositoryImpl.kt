package com.example.iurankomplek.transaction

import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepositoryImpl(
    private val paymentGateway: PaymentGateway,
    private val transactionDao: TransactionDao
) : TransactionRepository {
    
// Additional methods for API integration
      suspend fun initiatePaymentViaApi(
          amount: String,
          description: String,
          customerId: String,
          paymentMethod: String
      ): Result<com.example.iurankomplek.model.PaymentResponse> {
          // This method properly integrates with the payment gateway using suspend functions
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

    suspend fun refundPayment(transactionId: String, reason: String?): Result<com.example.iurankomplek.payment.RefundResponse> {
        return try {
            val refundResult = paymentGateway.refundPayment(transactionId)
            
            refundResult.onSuccess { response ->
                // Update the original transaction status to REFUNDED
                val originalTransaction = getTransactionById(transactionId)
                if (originalTransaction != null) {
                    val refundedTransaction = originalTransaction.copy(
                        status = com.example.iurankomplek.payment.PaymentStatus.REFUNDED,
                        updatedAt = java.util.Date()
                    )
                    transactionDao.update(refundedTransaction)
                }
            }
            
            refundResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}
