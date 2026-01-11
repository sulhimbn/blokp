package com.example.iurankomplek.presentation.viewmodel

import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.domain.usecase.LoadMessagesUseCase
import com.example.iurankomplek.domain.usecase.SendMessageUseCase
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MessageViewModel(
    private val loadMessagesUseCase: LoadMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : BaseViewModel() {

    private val _messagesState = createMutableStateFlow<List<Message>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<Message>>> = _messagesState

    private val _sendMessageState = createMutableStateFlow<Message>(UiState.Idle)
    val sendMessageState: StateFlow<UiState<Message>> = _sendMessageState

    fun loadMessages(userId: String) {
        executeWithLoadingStateForResult(_messagesState) {
            loadMessagesUseCase(userId)
        }
    }

    fun loadMessagesWithUser(receiverId: String, senderId: String) {
        executeWithLoadingStateForResult(_messagesState) {
            loadMessagesUseCase(receiverId, senderId)
        }
    }

    fun sendMessage(senderId: String, receiverId: String, content: String) {
        executeWithLoadingStateForResult(_sendMessageState, preventDuplicate = false) {
            sendMessageUseCase(senderId, receiverId, content)
        }
    }

    companion object {
        fun Factory(
            loadMessagesUseCase: LoadMessagesUseCase,
            sendMessageUseCase: SendMessageUseCase
        ) = viewModelInstance {
            MessageViewModel(loadMessagesUseCase, sendMessageUseCase)
        }
    }
}
