package com.example.iurankomplek.network.model

data class SendMessageRequest(
    val senderId: String,
    val receiverId: String,
    val content: String,
    val attachments: List<String> = emptyList()
)
