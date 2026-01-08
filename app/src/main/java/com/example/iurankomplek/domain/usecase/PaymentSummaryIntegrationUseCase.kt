package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.first

/**
 * Use case for integrating payment transactions into financial summary
 * Encapsulates business logic for payment data integration
 */
class PaymentSummaryIntegrationUseCase(
    private val transactionRepository: TransactionRepository
) {
    
    /**
     * Result class for payment integration
     */
    data class PaymentIntegrationResult(
        val paymentTotal: Int,
        val transactionCount: Int,
        val isIntegrated: Boolean,
        val error: String? = null
    )
    
    /**
     * Integrates payment transactions with financial totals
     *
     * @return PaymentIntegrationResult with payment data
     */
    suspend operator fun invoke(): PaymentIntegrationResult {
        return try {
            val completedTransactions = fetchCompletedTransactions()
            
            if (completedTransactions.isEmpty()) {
                PaymentIntegrationResult(
                    paymentTotal = 0,
                    transactionCount = 0,
                    isIntegrated = false
                )
            } else {
                val paymentTotal = calculatePaymentTotal(completedTransactions)
                
                PaymentIntegrationResult(
                    paymentTotal = paymentTotal,
                    transactionCount = completedTransactions.size,
                    isIntegrated = true
                )
            }
        } catch (e: Exception) {
            PaymentIntegrationResult(
                paymentTotal = 0,
                transactionCount = 0,
                isIntegrated = false,
                error = "Failed to integrate payment data: ${e.message}"
            )
        }
    }
    
    /**
     * Fetches completed transactions from repository
     */
    private suspend fun fetchCompletedTransactions() =
        transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED).first()
    
    /**
     * Calculates total payment amount from transactions
     */
    private fun calculatePaymentTotal(transactions: List<com.example.iurankomplek.data.transaction.Transaction>) =
        transactions.sumOf { it.amount.toInt() }
}
