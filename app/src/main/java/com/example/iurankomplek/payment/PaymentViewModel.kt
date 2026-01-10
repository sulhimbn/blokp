package com.example.iurankomplek.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.domain.usecase.ValidatePaymentUseCase
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
    val errorMessage: String? = null,
    val validationError: String? = null
)

sealed class PaymentEvent {
    object Processing : PaymentEvent()
    data class Success(val message: String) : PaymentEvent()
    data class Error(val message: String) : PaymentEvent()
    data class ValidationError(val message: String) : PaymentEvent()
}

class PaymentViewModel(
    private val transactionRepository: TransactionRepository,
    private val receiptGenerator: ReceiptGenerator,
    private val validatePaymentUseCase: ValidatePaymentUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState
    
    private val _paymentEvent = MutableStateFlow<PaymentEvent?>(null)
    val paymentEvent: StateFlow<PaymentEvent?> = _paymentEvent

    fun setAmount(amount: BigDecimal) {
        _uiState.value = _uiState.value.copy(amount = amount, isPaymentEnabled = amount > BigDecimal.ZERO)
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedMethod = method)
    }

    fun validateAndProcessPayment(amountText: String, spinnerPosition: Int) {
        val validationResult = validatePaymentUseCase(amountText, spinnerPosition)
        
        validationResult.onSuccess { validatedPayment ->
            setAmount(validatedPayment.amount)
            selectPaymentMethod(validatedPayment.paymentMethod)
            processPayment()
        }.onFailure { error ->
            _paymentEvent.value = PaymentEvent.ValidationError(error.message ?: "Invalid payment data")
        }
    }

    fun processPayment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            _paymentEvent.value = PaymentEvent.Processing
            
            val currentState = _uiState.value
            val request = PaymentRequest(
                amount = currentState.amount,
                description = "HOA Payment",
                customerId = "current_user_id", // This would come from auth system
                paymentMethod = currentState.selectedMethod
            )
            
            val result = transactionRepository.processPayment(request)
            result.onSuccess { _ ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = null
                )
                _paymentEvent.value = PaymentEvent.Success("Payment processed successfully")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = error.message
                )
                _paymentEvent.value = PaymentEvent.Error(error.message ?: "Payment failed")
            }
        }
    }
    
    fun clearEvent() {
        _paymentEvent.value = null
    }
}