package com.example.iurankomplek.presentation.viewmodel

import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase
import com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase
import com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase
import com.example.iurankomplek.domain.model.FinancialItem
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FinancialViewModel(
    private val loadFinancialDataUseCase: LoadFinancialDataUseCase,
    private val calculateFinancialSummaryUseCase: CalculateFinancialSummaryUseCase = CalculateFinancialSummaryUseCase(),
    private val paymentSummaryIntegrationUseCase: PaymentSummaryIntegrationUseCase? = null
) : BaseViewModel() {

    private val _financialState = createMutableStateFlow<PemanfaatanResponse>(UiState.Loading)
    val financialState: StateFlow<UiState<PemanfaatanResponse>> = _financialState

    fun loadFinancialData() {
        executeWithLoadingStateForResult(_financialState) {
            loadFinancialDataUseCase()
        }
    }

    fun calculateFinancialSummary(items: List<FinancialItem>): CalculateFinancialSummaryUseCase.FinancialSummary {
        return calculateFinancialSummaryUseCase(items)
    }

    suspend fun integratePaymentTransactions(): PaymentSummaryIntegrationUseCase.PaymentIntegrationResult? {
        return paymentSummaryIntegrationUseCase?.invoke()
    }

    companion object {
        fun Factory(
            loadFinancialDataUseCase: LoadFinancialDataUseCase,
            calculateFinancialSummaryUseCase: CalculateFinancialSummaryUseCase = CalculateFinancialSummaryUseCase(),
            paymentSummaryIntegrationUseCase: PaymentSummaryIntegrationUseCase? = null
        ) = viewModelInstance {
            FinancialViewModel(loadFinancialDataUseCase, calculateFinancialSummaryUseCase, paymentSummaryIntegrationUseCase)
        }
    }
}