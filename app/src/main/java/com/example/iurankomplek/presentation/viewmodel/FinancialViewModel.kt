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
    private val loadFinancialDataUseCase: LoadFinancialDataUseCase
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
    
    class Factory(private val loadFinancialDataUseCase: LoadFinancialDataUseCase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinancialViewModel(loadFinancialDataUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}