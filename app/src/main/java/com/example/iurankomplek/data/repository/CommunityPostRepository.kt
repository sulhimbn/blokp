package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult
import com.example.iurankomplek.model.CommunityPost

interface CommunityPostRepository {
    suspend fun getCommunityPosts(forceRefresh: Boolean = false): OperationResult<List<CommunityPost>>
    suspend fun createCommunityPost(authorId: String, title: String, content: String, category: String): OperationResult<CommunityPost>
    suspend fun getCachedCommunityPosts(): OperationResult<List<CommunityPost>>
    suspend fun clearCache(): OperationResult<Unit>
}
