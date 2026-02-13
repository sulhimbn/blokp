package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.utils.Result
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Abstract base ViewModel providing common functionality for all ViewModels
 */
abstract class BaseViewModel<T> : ViewModel() {
    
    protected val _uiState = MutableStateFlow<UiState<T>>(UiState.Loading)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()
    
    protected fun setLoading() {
        _uiState.value = UiState.Loading
    }
    
    protected fun setSuccess(data: T) {
        _uiState.value = UiState.Success(data)
    }
    
    protected fun setError(message: String) {
        _uiState.value = UiState.Error(message)
    }
    
    protected fun <R> handleResult(
        result: Result<R>,
        onSuccess: (R) -> Unit,
        onError: (String) -> Unit = { setError(it) }
    ) {
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error -> onError(result.message)
            is Result.Loading -> setLoading()
            is Result.Empty -> onError("No data available")
        }
    }
    
    protected fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            setLoading()
            try {
                block()
            } catch (e: Exception) {
                setError(e.message ?: "Unknown error occurred")
            }
        }
    }
}
