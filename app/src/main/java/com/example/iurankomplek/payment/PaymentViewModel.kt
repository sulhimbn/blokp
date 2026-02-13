package com.example.iurankomplek.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.transaction.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class PaymentUiState(
    val amount: BigDecimal = BigDecimal.ZERO,
    val selectedMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val isProcessing: Boolean = false,
    val isPaymentEnabled: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val receiptGenerator: ReceiptGenerator,
    private val sessionManager: UserSessionManager,
    private val eventBus: EventBus
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState

    fun setAmount(amount: BigDecimal) {
        _uiState.value = _uiState.value.copy(amount = amount, isPaymentEnabled = amount > BigDecimal.ZERO)
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedMethod = method)
    }

    fun processPayment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            val userId = sessionManager.currentUserId
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "User not logged in"
                )
                emitPaymentFailed("User not logged in")
                return@launch
            }

            val currentState = _uiState.value
            val request = PaymentRequest(
                amount = currentState.amount,
                description = "HOA Payment",
                customerId = userId,
                paymentMethod = currentState.selectedMethod
            )

            val result = transactionRepository.processPayment(request)
            result.onSuccess { transaction ->
                val receipt = receiptGenerator.generateReceipt(transaction)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = null
                )
                emitPaymentSuccess(transaction)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = error.message
                )
                emitPaymentFailed(error.message ?: "Payment failed")
            }
        }
    }

    private suspend fun emitPaymentSuccess(transaction: com.example.iurankomplek.transaction.Transaction) {
        eventBus.publish(AppEvent.PaymentCompleted(
            paymentId = transaction.id,
            amount = transaction.amount
        ))
        eventBus.publish(AppEvent.TransactionCreated(
            transactionId = transaction.id
        ))
        eventBus.publish(AppEvent.FinancialDataUpdated)
    }

    private suspend fun emitPaymentFailed(error: String) {
        eventBus.publish(AppEvent.PaymentFailed(error))
    }
}
