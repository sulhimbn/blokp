package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.model.CreateCommunityPostRequest
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class CommunityPostRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : CommunityPostRepository, BaseRepository {
    private val cache = ConcurrentHashMap<String, CommunityPost>()
    
    override suspend fun getCommunityPosts(forceRefresh: Boolean): Result<List<CommunityPost>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return Result.success(cache.values.toList())
        }

        return executeWithCircuitBreakerV2 { apiService.getCommunityPosts() }
    }
 
    override suspend fun createCommunityPost(
        authorId: String,
        title: String,
        content: String,
        category: String
    ): Result<CommunityPost> {
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
 
    override suspend fun getCachedCommunityPosts(): Result<List<CommunityPost>> {
        return try {
            Result.success(cache.values.toList())
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
