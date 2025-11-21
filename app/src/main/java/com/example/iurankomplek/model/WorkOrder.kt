package com.example.iurankomplek.model

data class WorkOrder(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // plumbing, electrical, etc.
    val priority: String, // low, medium, high, urgent
    val status: String, // pending, assigned, in_progress, completed, cancelled
    val vendorId: String?,
    val vendorName: String?,
    val assignedAt: String?,
    val scheduledDate: String?,
    val completedAt: String?,
    val estimatedCost: Double,
    val actualCost: Double,
    val propertyId: String, // associated property or area
    val reporterId: String, // who reported the issue
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<String>, // photo URLs or document links
    val notes: List<String>
)