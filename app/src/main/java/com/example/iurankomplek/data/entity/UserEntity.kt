package com.example.iurankomplek.data.entity

import java.util.Date

data class UserEntity(
    val id: Long = 0,
    val email: String,
    val firstName: String,
    val lastName: String,
    val alamat: String,
    val avatar: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    init {
        validate()
    }

    private fun validate() {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(alamat.isNotBlank()) { "Alamat cannot be blank" }
        require(email.contains("@")) { "Email must contain @ symbol" }
    }

    val fullName: String
        get() = "$firstName $lastName"
}
