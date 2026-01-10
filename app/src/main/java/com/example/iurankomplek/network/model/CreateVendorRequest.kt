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
