package com.example.iurankomplek.core.base

import kotlin.jvm.JvmName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.utils.OperationResult
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    
    protected fun <T> executeWithLoadingState(
        stateFlow: MutableStateFlow<UiState<T>>,
        preventDuplicate: Boolean = true,
        operation: suspend () -> T
    ) {
        if (preventDuplicate && stateFlow.value is UiState.Loading) {
            return
        }
        
        viewModelScope.launch {
            stateFlow.value = UiState.Loading
            try {
                val result = operation()
                stateFlow.value = UiState.Success(result)
            } catch (exception: Exception) {
                stateFlow.value = UiState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }
    
    protected fun <T> executeWithLoadingStateForResult(
        stateFlow: MutableStateFlow<UiState<T>>,
        preventDuplicate: Boolean = true,
        operation: suspend () -> OperationResult<T>
    ) {
        if (preventDuplicate && stateFlow.value is UiState.Loading) {
            return
        }

        viewModelScope.launch {
            stateFlow.value = UiState.Loading
            try {
                val result = operation()
                when (result) {
                    is OperationResult.Success -> {
                        stateFlow.value = UiState.Success(result.data)
                    }
                    is OperationResult.Error -> {
                        stateFlow.value = UiState.Error(result.message)
                    }
                    is OperationResult.Loading -> {
                        stateFlow.value = UiState.Loading
                    }
                    OperationResult.Empty -> {
                        stateFlow.value = UiState.Error("No data available")
                    }
                }
            } catch (exception: Exception) {
                stateFlow.value = UiState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }
    
    protected fun <T> executeWithoutLoadingState(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = operation()
                onSuccess(result)
            } catch (exception: Exception) {
                onError(exception.message ?: "Unknown error occurred")
            }
        }
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @JvmName("createMutableStateFlowLoading")
    protected fun createMutableStateFlow(initialValue: UiState<Nothing> = UiState.Loading): MutableStateFlow<UiState<Nothing>> {
        return MutableStateFlow(initialValue)
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @JvmName("createMutableStateFlowTyped")
    protected fun <T> createMutableStateFlow(initialValue: UiState<T>): MutableStateFlow<UiState<T>> {
        return MutableStateFlow(initialValue)
    }
}