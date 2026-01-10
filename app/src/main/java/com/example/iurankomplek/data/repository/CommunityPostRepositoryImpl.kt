package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.model.CreateCommunityPostRequest
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class CommunityPostRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : CommunityPostRepository, BaseRepository() {
    private val cache = ConcurrentHashMap<String, CommunityPost>()

    override suspend fun getCommunityPosts(forceRefresh: Boolean): OperationResult<List<CommunityPost>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return OperationResult.Success(cache.values.toList())
        }

        return executeWithCircuitBreakerV2 { apiService.getCommunityPosts() }
    }

    override suspend fun createCommunityPost(
        authorId: String,
        title: String,
        content: String,
        category: String
    ): OperationResult<CommunityPost> {
        val request = CreateCommunityPostRequest(
            authorId = authorId,
            title = title,
            content = content,
            category = category
        )
        return executeWithCircuitBreakerV1 {
            apiService.createCommunityPost(request)
        }
    }

    override suspend fun getCachedCommunityPosts(): OperationResult<List<CommunityPost>> {
        return try {
            OperationResult.Success(cache.values.toList())
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
}
