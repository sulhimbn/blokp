package com.example.iurankomplek.network.model

data class CreateVendorRequest(
    val name: String,
    val contactPerson: String,
    val phoneNumber: String,
    val email: String,
    val specialty: String,
    val address: String,
    val licenseNumber: String,
    val insuranceInfo: String,
    val contractStart: String,
    val contractEnd: String
)

data class UpdateVendorRequest(
    val name: String,
    val contactPerson: String,
    val phoneNumber: String,
    val email: String,
    val specialty: String,
    val address: String,
    val licenseNumber: String,
    val insuranceInfo: String,
    val contractStart: String,
    val contractEnd: String,
    val isActive: Boolean
)

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

data class AssignVendorRequest(
    val vendorId: String,
    val scheduledDate: String? = null
)

data class UpdateWorkOrderRequest(
    val status: String,
    val notes: String? = null
)

data class SendMessageRequest(
    val senderId: String,
    val receiverId: String,
    val content: String
)

data class CreateCommunityPostRequest(
    val authorId: String,
    val title: String,
    val content: String,
    val category: String
)

data class InitiatePaymentRequest(
    val amount: String,
    val description: String,
    val customerId: String,
    val paymentMethod: String
)
