package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.model.SendMessageRequest
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class MessageRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : MessageRepository, BaseRepository() {
    private val cache = ConcurrentHashMap<String, List<Message>>()

    private val messageFallbackManager = FallbackManager<List<Message>>(
        fallbackStrategy = CachedMessagesFallback(cache),
        config = FallbackConfig(enableFallback = true, fallbackTimeoutMs = 5000L, logFallbackUsage = true)
    )

    override suspend fun getMessages(userId: String): OperationResult<List<Message>> {
        return messageFallbackManager.executeWithFallback(
            primaryOperation = {
                executeWithCircuitBreakerV2 { apiService.getMessages(userId) }
                    .also { result ->
                        if (result is OperationResult.Success) {
                            cache[userId] = result.data
                        }
                    }
            }
        )
    }

    override suspend fun getMessagesWithUser(receiverId: String, senderId: String): OperationResult<List<Message>> {
        return messageFallbackManager.executeWithFallback(
            primaryOperation = { executeWithCircuitBreakerV2 { apiService.getMessagesWithUser(receiverId, senderId) } }
        )
    }

    override suspend fun sendMessage(senderId: String, receiverId: String, content: String): OperationResult<Message> {
        val request = SendMessageRequest(
            senderId = senderId,
            receiverId = receiverId,
            content = content
        )
        return executeWithCircuitBreakerV1 { apiService.sendMessage(request) }
    }

    override suspend fun getCachedMessages(userId: String): OperationResult<List<Message>> {
        return try {
            val messages = cache[userId] ?: emptyList()
            OperationResult.Success(messages)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache(): OperationResult<Unit> {
        return try {
            cache.clear()
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    private class CachedMessagesFallback(private val cache: ConcurrentHashMap<String, List<Message>>) : CachedDataFallback<List<Message>>() {
        override suspend fun getCachedData(): List<Message>? {
            return try {
                cache.values.firstOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }
}
