package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class TransactionViewModel(
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {

    private val _transactionsState = createMutableStateFlow<List<Transaction>>(UiState.Loading)
    val transactionsState: StateFlow<UiState<List<Transaction>>> = _transactionsState

    private val _refundState = createMutableStateFlow<Unit>(UiState.Idle)
    val refundState: StateFlow<UiState<Unit>> = _refundState

    fun loadTransactionsByStatus(status: PaymentStatus) {
        executeWithLoadingState(_transactionsState) {
            transactionRepository.getTransactionsByStatus(status).first()
        }
    }

    fun refundPayment(transactionId: String, reason: String) {
        executeWithoutLoadingState(
            operation = {
                transactionRepository.refundPayment(transactionId, reason)
                loadAllTransactions()
            },
            onSuccess = {
                _refundState.value = UiState.Success(Unit)
            },
            onError = { error ->
                _refundState.value = UiState.Error(error)
            }
        )
    }

    fun loadAllTransactions() {
        executeWithLoadingState(_transactionsState) {
            transactionRepository.getAllTransactions().first()
        }
    }

    class Factory(private val transactionRepository: TransactionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TransactionViewModel(transactionRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}