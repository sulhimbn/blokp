package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.model.SendMessageRequest
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class MessageRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : MessageRepository(), BaseRepository() {
    private val cache = ConcurrentHashMap<String, List<Message>>()

    override suspend fun getMessages(userId: String): Result<List<Message>> {
        val cachedMessages = cache[userId]
        if (cachedMessages != null) {
            return Result.success(cachedMessages)
        }

        return executeWithCircuitBreaker { apiService.getMessages(userId) }
            .also { result ->
                result.onSuccess { messages ->
                    cache[userId] = messages
                }
            }
    }

    override suspend fun getMessagesWithUser(receiverId: String, senderId: String): Result<List<Message>> {
        return executeWithCircuitBreaker { apiService.getMessagesWithUser(receiverId, senderId) }
    }

    override suspend fun sendMessage(senderId: String, receiverId: String, content: String): Result<Message> {
        val request = SendMessageRequest(
            senderId = senderId,
            receiverId = receiverId,
            content = content
        )
        return executeWithCircuitBreaker { apiService.sendMessage(request) }
    }

    override suspend fun getCachedMessages(userId: String): Result<List<Message>> {
        return try {
            val messages = cache[userId] ?: emptyList()
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            cache.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
