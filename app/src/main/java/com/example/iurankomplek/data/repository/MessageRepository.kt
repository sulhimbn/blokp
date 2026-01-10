package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.Message

interface MessageRepository {
    suspend fun getMessages(userId: String): OperationResult<List<Message>>
    suspend fun getMessagesWithUser(receiverId: String, senderId: String): OperationResult<List<Message>>
    suspend fun sendMessage(senderId: String, receiverId: String, content: String): OperationResult<Message>
    suspend fun getCachedMessages(userId: String): OperationResult<List<Message>>
    suspend fun clearCache(): OperationResult<Unit>
}
