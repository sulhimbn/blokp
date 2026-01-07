package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionRepository
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val transactionsState: StateFlow<UiState<List<Transaction>>> = _transactionsState

    fun loadTransactionsByStatus(status: PaymentStatus) {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            transactionRepository.getTransactionsByStatus(status)
                .first()
                .onSuccess { transactions ->
                    _transactionsState.value = UiState.Success(transactions)
                }
                .onFailure { exception ->
                    _transactionsState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }

    fun loadAllTransactions() {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            transactionRepository.getAllTransactions()
                .first()
                .onSuccess { transactions ->
                    _transactionsState.value = UiState.Success(transactions)
                }
                .onFailure { exception ->
                    _transactionsState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
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
