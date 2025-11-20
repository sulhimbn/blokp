package com.example.iurankomplek.model

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val priority: String,
    val createdAt: String,
    val readBy: List<String>
)