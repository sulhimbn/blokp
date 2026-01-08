package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _messagesState = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<Message>>> = _messagesState

    private val _sendMessageState = MutableStateFlow<UiState<Message>>(UiState.Idle)
    val sendMessageState: StateFlow<UiState<Message>> = _sendMessageState

    fun loadMessages(userId: String) {
        if (_messagesState.value is UiState.Loading) return

        viewModelScope.launch {
            _messagesState.value = UiState.Loading
            messageRepository.getMessages(userId)
                .onSuccess { messages ->
                    _messagesState.value = UiState.Success(messages)
                }
                .onFailure { exception ->
                    _messagesState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }

    fun loadMessagesWithUser(receiverId: String, senderId: String) {
        if (_messagesState.value is UiState.Loading) return

        viewModelScope.launch {
            _messagesState.value = UiState.Loading
            messageRepository.getMessagesWithUser(receiverId, senderId)
                .onSuccess { messages ->
                    _messagesState.value = UiState.Success(messages)
                }
                .onFailure { exception ->
                    _messagesState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, content: String) {
        viewModelScope.launch {
            _sendMessageState.value = UiState.Loading
            messageRepository.sendMessage(senderId, receiverId, content)
                .onSuccess { message ->
                    _sendMessageState.value = UiState.Success(message)
                }
                .onFailure { exception ->
                    _sendMessageState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }

    class Factory(private val messageRepository: MessageRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MessageViewModel(messageRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}