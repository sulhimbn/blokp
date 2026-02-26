package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor() : ViewModel() {

    private val _messagesState = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<Message>>> = _messagesState

    private var currentUserId: String = "default_user_id"

    init {
        loadMessages(currentUserId)
    }

    fun loadMessages(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _messagesState.value = UiState.Loading
            try {
                val response = ApiConfig.getApiService().getMessages(userId)
                if (response.isSuccessful) {
                    val messages = response.body()
                    if (messages != null) {
                        _messagesState.value = UiState.Success(messages)
                    } else {
                        _messagesState.value = UiState.Error("No messages available")
                    }
                } else {
                    _messagesState.value = UiState.Error("Failed to load messages")
                }
            } catch (e: Exception) {
                _messagesState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun refreshMessages() {
        loadMessages(currentUserId)
    }
}
