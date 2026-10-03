package com.example.iurankomplek.model

/**
 * Work order record as returned by the work-order endpoints. Defaults mirror
 * Gson behaviour for absent JSON members; optional lifecycle fields stay
 * nullable because the API omits them until the order advances.
 */
data class WorkOrder(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "", // plumbing, electrical, etc.
    val priority: String = "", // low, medium, high, urgent
    val status: String = "pending", // pending, assigned, in_progress, completed, cancelled
    val vendorId: String? = null,
    val vendorName: String? = null,
    val assignedAt: String? = null,
    val scheduledDate: String? = null,
    val completedAt: String? = null,
    val estimatedCost: Double = 0.0,
    val actualCost: Double = 0.0,
    val propertyId: String = "", // associated property or area
    val reporterId: String = "", // who reported the issue
    val createdAt: String = "",
    val updatedAt: String = "",
    val attachments: List<String> = emptyList(), // photo URLs or document links
    val notes: List<String> = emptyList()
)