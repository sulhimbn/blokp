package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val transactionsState: StateFlow<UiState<List<Transaction>>> = _transactionsState

    private val _refundState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val refundState: StateFlow<UiState<Unit>> = _refundState

    fun loadTransactionsByStatus(status: PaymentStatus) {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            try {
                val transactions = transactionRepository.getTransactionsByStatus(status).first()
                _transactionsState.value = UiState.Success(transactions)
            } catch (exception: Exception) {
                _transactionsState.value = UiState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }

    fun refundPayment(transactionId: String, reason: String) {
        viewModelScope.launch {
            _refundState.value = UiState.Loading
            try {
                val result = transactionRepository.refundPayment(transactionId, reason)
                if (result.isSuccess) {
                    _refundState.value = UiState.Success(Unit)
                    loadAllTransactions()
                } else {
                    _refundState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Refund failed")
                }
            } catch (exception: Exception) {
                _refundState.value = UiState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadAllTransactions() {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                _transactionsState.value = UiState.Success(transactions)
            } catch (exception: Exception) {
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