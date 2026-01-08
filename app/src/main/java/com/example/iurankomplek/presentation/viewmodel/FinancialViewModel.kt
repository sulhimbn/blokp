package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FinancialViewModel(
    private val loadFinancialDataUseCase: LoadFinancialDataUseCase,
    private val calculateFinancialSummaryUseCase: com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase = com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase(),
    private val paymentSummaryIntegrationUseCase: com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase? = null
) : ViewModel() {
    
    private val _financialState = MutableStateFlow<UiState<PemanfaatanResponse>>(UiState.Loading)
    val financialState: StateFlow<UiState<PemanfaatanResponse>> = _financialState
    
    fun loadFinancialData() {
        if (_financialState.value is UiState.Loading) return // Prevent duplicate calls
        
        viewModelScope.launch {
            _financialState.value = UiState.Loading
            loadFinancialDataUseCase()
                .onSuccess { response ->
                    _financialState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _financialState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }
    
    /**
     * Calculates financial summary from financial data items
     * 
     * @param items List of LegacyDataItemDto to calculate summary for
     * @return FinancialSummary with calculated values
     */
    fun calculateFinancialSummary(items: List<com.example.iurankomplek.data.dto.LegacyDataItemDto>): com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase.FinancialSummary {
        return calculateFinancialSummaryUseCase(items)
    }
    
    /**
     * Integrates payment transactions into financial summary
     * Only available if TransactionRepository is provided
     * 
     * @return PaymentIntegrationResult with payment data or null if not available
     */
    suspend fun integratePaymentTransactions(): com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase.PaymentIntegrationResult? {
        return paymentSummaryIntegrationUseCase?.invoke()
    }
    
    class Factory(
        private val loadFinancialDataUseCase: LoadFinancialDataUseCase,
        private val calculateFinancialSummaryUseCase: com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase = com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase(),
        private val paymentSummaryIntegrationUseCase: com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinancialViewModel(loadFinancialDataUseCase, calculateFinancialSummaryUseCase, paymentSummaryIntegrationUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}