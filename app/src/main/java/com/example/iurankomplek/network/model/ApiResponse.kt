package com.example.iurankomplek.network.model

data class ApiResponse<T>(
    val data: T,
    val requestId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ApiListResponse<T>(
    val data: List<T>,
    val pagination: PaginationMetadata? = null,
    val requestId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class PaginationMetadata(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class ApiError(
    val code: String,
    val message: String,
    val details: String? = null,
    val requestId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
