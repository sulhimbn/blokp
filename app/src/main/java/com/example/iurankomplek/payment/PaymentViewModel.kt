package com.example.iurankomplek.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.utils.ReceiptGenerator
import com.example.iurankomplek.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class PaymentUiState(
    val amount: BigDecimal = BigDecimal.ZERO,
    val selectedMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val isProcessing: Boolean = false,
    val isPaymentEnabled: Boolean = false,
    val errorMessage: String? = null
)

class PaymentViewModel(
    private val transactionRepository: TransactionRepository,
    private val receiptGenerator: ReceiptGenerator
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
            
            val currentState = _uiState.value
            val request = PaymentRequest(
                amount = currentState.amount,
                description = "HOA Payment",
                customerId = "current_user_id", // This would come from auth system
                paymentMethod = currentState.selectedMethod
            )
            
            val result = transactionRepository.processPayment(request)
            result.onSuccess { transaction ->
                // Generate receipt
                val receipt = receiptGenerator.generateReceipt(transaction)
                // In a real app, we would save or display the receipt
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = error.message
                )
            }
        }
    }
}