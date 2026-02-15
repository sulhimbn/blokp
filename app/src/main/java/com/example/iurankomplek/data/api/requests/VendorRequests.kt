package com.example.iurankomplek.data.api.requests

// Request models for vendor work order-related API calls
data class VendorWorkOrderRequest(
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val propertyId: String,
    val reporterId: String,
    val estimatedCost: Double,
    val attachments: List<String> = emptyList()
)

data class AssignVendorRequest(
    val vendorId: String,
    val scheduledDate: String? = null
)

data class UpdateWorkOrderStatusRequest(
    val status: String,
    val notes: String? = null
)
