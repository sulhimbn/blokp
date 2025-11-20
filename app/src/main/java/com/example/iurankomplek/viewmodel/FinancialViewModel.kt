package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FinancialViewModel(
    private val pemanfaatanRepository: PemanfaatanRepository
) : ViewModel() {
    
    private val _financialState = MutableStateFlow<UiState<PemanfaatanResponse>>(UiState.Loading)
    val financialState: StateFlow<UiState<PemanfaatanResponse>> = _financialState
    
    fun loadFinancialData() {
        if (_financialState.value is UiState.Loading) return // Prevent duplicate calls
        
        viewModelScope.launch {
            _financialState.value = UiState.Loading
            pemanfaatanRepository.getPemanfaatan()
                .onSuccess { response ->
                    _financialState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _financialState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }
}