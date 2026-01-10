package com.example.iurankomplek.network.model

data class UpdateWorkOrderRequest(
    val status: String,
    val notes: String? = null
)
