package com.example.iurankomplek.network.model

data class CreateCommunityPostRequest(
    val authorId: String,
    val title: String,
    val content: String,
    val category: String
)
