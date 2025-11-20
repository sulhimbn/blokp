package com.example.iurankomplek.model

data class Message(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: String,
    val readStatus: Boolean,
    val attachments: List<String>
)