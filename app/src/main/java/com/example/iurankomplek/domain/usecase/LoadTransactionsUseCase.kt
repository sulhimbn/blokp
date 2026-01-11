package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.flow.Flow

class LoadTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): OperationResult<List<Transaction>> {
        return try {
            OperationResult.Success(transactionRepository.getAllTransactions().first())
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load transactions")
        }
    }

    suspend operator fun invoke(status: com.example.iurankomplek.payment.PaymentStatus): OperationResult<List<Transaction>> {
        return try {
            OperationResult.Success(transactionRepository.getTransactionsByStatus(status).first())
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load transactions")
        }
    }
}
