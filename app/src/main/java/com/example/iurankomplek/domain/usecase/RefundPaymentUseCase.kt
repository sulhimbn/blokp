package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.OperationResult

class RefundPaymentUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String, reason: String): OperationResult<Unit> {
        return try {
            transactionRepository.refundPayment(transactionId, reason)
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to refund payment")
        }
    }
}
