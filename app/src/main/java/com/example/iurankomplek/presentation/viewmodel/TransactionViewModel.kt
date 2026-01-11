package com.example.iurankomplek.presentation.viewmodel

import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.domain.usecase.LoadTransactionsUseCase
import com.example.iurankomplek.domain.usecase.RefundPaymentUseCase
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransactionViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val refundPaymentUseCase: RefundPaymentUseCase
) : BaseViewModel() {

    private val _transactionsState = createMutableStateFlow<List<Transaction>>(UiState.Loading)
    val transactionsState: StateFlow<UiState<List<Transaction>>> = _transactionsState

    private val _refundState = createMutableStateFlow<Unit>(UiState.Idle)
    val refundState: StateFlow<UiState<Unit>> = _refundState

    fun loadTransactionsByStatus(status: com.example.iurankomplek.payment.PaymentStatus) {
        executeWithLoadingStateForResult(_transactionsState) {
            loadTransactionsUseCase(status)
        }
    }

    fun refundPayment(transactionId: String, reason: String) {
        executeWithoutLoadingState(
            operation = {
                refundPaymentUseCase(transactionId, reason)
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
        executeWithLoadingStateForResult(_transactionsState) {
            loadTransactionsUseCase()
        }
    }

    companion object {
        fun Factory(
            loadTransactionsUseCase: LoadTransactionsUseCase,
            refundPaymentUseCase: RefundPaymentUseCase
        ) = viewModelInstance {
            TransactionViewModel(loadTransactionsUseCase, refundPaymentUseCase)
        }
    }
}
