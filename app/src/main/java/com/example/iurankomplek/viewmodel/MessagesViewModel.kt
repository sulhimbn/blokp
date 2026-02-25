package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _messagesState = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<Message>>> = _messagesState

    fun loadMessages(userId: String) {
        if (_messagesState.value is UiState.Loading) return

        viewModelScope.launch {
            _messagesState.value = UiState.Loading
            try {
                val response = apiService.getMessages(userId)
                if (response.isSuccessful) {
                    val messages = response.body()
                    if (messages != null) {
                        _messagesState.value = UiState.Success(messages)
                    } else {
                        _messagesState.value = UiState.Error("No messages available")
                    }
                } else {
                    _messagesState.value = UiState.Error("Failed to load messages: ${response.code()}")
                }
            } catch (e: Exception) {
                _messagesState.value = UiState.Error("Network error: ${e.message}")
            }
        }
    }

    class Factory(
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MessagesViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
