package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MessageViewModel(
    private val messageRepository: MessageRepository
) : BaseViewModel() {

    private val _messagesState = createMutableStateFlow<List<Message>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<Message>>> = _messagesState

    private val _sendMessageState = createMutableStateFlow<Message>(UiState.Idle)
    val sendMessageState: StateFlow<UiState<Message>> = _sendMessageState

    fun loadMessages(userId: String) {
        executeWithLoadingStateForResult(_messagesState) {
            messageRepository.getMessages(userId)
        }
    }

    fun loadMessagesWithUser(receiverId: String, senderId: String) {
        executeWithLoadingStateForResult(_messagesState) {
            messageRepository.getMessagesWithUser(receiverId, senderId)
        }
    }

    fun sendMessage(senderId: String, receiverId: String, content: String) {
        executeWithLoadingStateForResult(_sendMessageState, preventDuplicate = false) {
            messageRepository.sendMessage(senderId, receiverId, content)
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