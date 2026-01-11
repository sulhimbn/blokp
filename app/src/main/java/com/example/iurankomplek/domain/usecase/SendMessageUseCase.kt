package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.OperationResult

class SendMessageUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(senderId: String, receiverId: String, content: String): OperationResult<Message> {
        return try {
            messageRepository.sendMessage(senderId, receiverId, content)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to send message")
        }
    }
}
