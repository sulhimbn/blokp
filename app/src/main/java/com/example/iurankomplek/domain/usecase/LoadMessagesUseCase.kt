package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.OperationResult

class LoadMessagesUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(userId: String): OperationResult<List<Message>> {
        return try {
            messageRepository.getMessages(userId)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load messages")
        }
    }

    suspend operator fun invoke(receiverId: String, senderId: String): OperationResult<List<Message>> {
        return try {
            messageRepository.getMessagesWithUser(receiverId, senderId)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load messages")
        }
    }
}
