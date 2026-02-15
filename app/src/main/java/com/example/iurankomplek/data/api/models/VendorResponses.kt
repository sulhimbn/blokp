package com.example.iurankomplek.data.api.models

import com.example.iurankomplek.model.Vendor

// Response models for vendor-related API calls
data class VendorResponse(
    val data: List<Vendor>
)

data class SingleVendorResponse(
    val data: Vendor
)
