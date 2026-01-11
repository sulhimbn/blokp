package com.example.iurankomplek.network.model

data class CreateWorkOrderRequest(
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val propertyId: String,
    val reporterId: String,
    val estimatedCost: Double,
    val attachments: List<String> = emptyList()
)
