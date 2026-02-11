package com.example.iurankomplek.model

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}