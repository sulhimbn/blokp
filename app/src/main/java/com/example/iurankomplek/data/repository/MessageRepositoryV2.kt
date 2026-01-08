package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.repository.BaseRepositoryV2
import com.example.iurankomplek.data.repository.cache.CacheStrategy
import com.example.iurankomplek.data.repository.cache.InMemoryCacheStrategy
import com.example.iurankomplek.model.Message
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/**
 * Refactored MessageRepository using unified repository pattern.
 * Extends BaseRepositoryV2 for consistent error handling and caching.
 *
 * BEFORE (58 lines):
 * - Extended BaseRepository (old version)
 * - Manual ConcurrentHashMap for caching
 * - Duplicate circuit breaker logic
 *
 * AFTER (this implementation):
 * - Extends BaseRepositoryV2 (enhanced)
 * - Uses InMemoryCacheStrategy
 * - No circuit breaker duplication
 * - Unified error handling
 * - Clearer code structure
 */
class MessageRepositoryV2(
    private val apiService: com.example.iurankomplek.network.ApiService
) : MessageRepository(), BaseRepositoryV2<List<Message>>() {

    override val cacheStrategy: CacheStrategy<List<Message>> =
        InMemoryCacheStrategy()

    override suspend fun getMessages(userId: String): Result<List<Message>> {
        return fetchWithCache(
            cacheKey = "messages_$userId",
            forceRefresh = false,
            fromNetwork = { apiService.getMessages(userId) }
        )
    }

    override suspend fun getMessagesWithUser(
        receiverId: String,
        senderId: String
    ): Result<List<Message>> {
        return fetchWithCache(
            cacheKey = "messages_${receiverId}_${senderId}",
            forceRefresh = false,
            fromNetwork = { apiService.getMessagesWithUser(receiverId, senderId) }
        )
    }

    override suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String
    ): Result<Message> {
        val request = SendMessageRequest(
            senderId = senderId,
            receiverId = receiverId,
            content = content
        )
        return fetchWithCache(
            cacheKey = null,
            forceRefresh = true,
            fromNetwork = { apiService.sendMessage(request) },
            fromCache = { null }
        )
    }

    override suspend fun getCachedMessages(userId: String): Result<List<Message>> {
        return try {
            val cached = cacheStrategy.get("messages_$userId")
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            this@MessageRepositoryV2.clearCache()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
