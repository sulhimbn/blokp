package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.CommunityPost

interface CommunityPostRepository {
    suspend fun getCommunityPosts(forceRefresh: Boolean = false): Result<List<CommunityPost>>
    suspend fun createCommunityPost(authorId: String, title: String, content: String, category: String): Result<CommunityPost>
    suspend fun getCachedCommunityPosts(): Result<List<CommunityPost>>
    suspend fun clearCache(): Result<Unit>
}
