package com.example.iurankomplek.network.model

data class AssignVendorRequest(
    val vendorId: String,
    val scheduledDate: String? = null
)
