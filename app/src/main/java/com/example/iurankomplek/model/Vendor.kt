package com.example.iurankomplek.model

data class Vendor(
    val id: String,
    val name: String,
    val contactPerson: String,
    val phoneNumber: String,
    val email: String,
    val specialty: String, // plumbing, electrical, landscaping, etc.
    val address: String,
    val licenseNumber: String,
    val insuranceInfo: String,
    val certifications: List<String>,
    val rating: Double,
    val totalReviews: Int,
    val contractStart: String,
    val contractEnd: String,
    val isActive: Boolean
)