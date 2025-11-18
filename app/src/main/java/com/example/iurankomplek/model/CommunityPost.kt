package com.example.iurankomplek.model

data class Comment(
    val id: String,
    val authorId: String,
    val content: String,
    val timestamp: String
)

data class CommunityPost(
    val id: String,
    val authorId: String,
    val title: String,
    val content: String,
    val category: String,
    val likes: Int,
    val comments: List<Comment>,
    val createdAt: String
)