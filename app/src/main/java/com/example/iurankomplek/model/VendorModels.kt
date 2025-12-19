package com.example.iurankomplek.model

// Response models for vendor-related API calls
data class VendorResponse(
    val data: List<Vendor>
)

data class SingleVendorResponse(
    val data: Vendor
)

data class WorkOrderResponse(
    val data: List<WorkOrder>
)

data class SingleWorkOrderResponse(
    val data: WorkOrder
)

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