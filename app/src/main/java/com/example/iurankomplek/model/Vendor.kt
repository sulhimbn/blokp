package com.example.iurankomplek.model

/**
 * Vendor record as returned by the vendor endpoints. Defaults mirror Gson
 * behaviour for absent JSON members so partially populated payloads from the
 * API do not force every call site to invent values.
 */
data class Vendor(
    val id: String = "",
    val name: String = "",
    val contactPerson: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val specialty: String = "", // plumbing, electrical, landscaping, etc.
    val address: String = "",
    val licenseNumber: String = "",
    val insuranceInfo: String = "",
    val certifications: List<String> = emptyList(),
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val contractStart: String = "",
    val contractEnd: String = "",
    val isActive: Boolean = true
)