package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.flow.Flow

class LoadMessagesUseCase(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(userId: String): Flow<OperationResult<List<Message>>> {
        return try {
            messageRepository.getMessages(userId)
                .map { OperationResult.Success(it) }
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(OperationResult.Error(e, e.message ?: "Failed to load messages"))
        }
    }

    operator fun invoke(receiverId: String, senderId: String): Flow<OperationResult<List<Message>>> {
        return try {
            messageRepository.getMessagesWithUser(receiverId, senderId)
                .map { OperationResult.Success(it) }
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(OperationResult.Error(e, e.message ?: "Failed to load messages"))
        }
    }
}
