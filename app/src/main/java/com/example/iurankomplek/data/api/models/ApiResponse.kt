package com.example.iurankomplek.data.api.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data")
    val data: T,
    
    @SerializedName("request_id")
    val requestId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(data)
        
        fun <T> successWithMetadata(
            data: T,
            requestId: String,
            timestamp: Long
        ): ApiResponse<T> = ApiResponse(
            data = data,
            requestId = requestId,
            timestamp = timestamp
        )
    }
}

data class ApiListResponse<T>(
    @SerializedName("data")
    val data: List<T>,
    
    @SerializedName("pagination")
    val pagination: PaginationMetadata? = null,
    
    @SerializedName("request_id")
    val requestId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long? = null
) {
    companion object {
        fun <T> success(data: List<T>): ApiListResponse<T> = ApiListResponse(data)
        
        fun <T> successWithMetadata(
            data: List<T>,
            requestId: String,
            timestamp: Long,
            pagination: PaginationMetadata? = null
        ): ApiListResponse<T> = ApiListResponse(
            data = data,
            pagination = pagination,
            requestId = requestId,
            timestamp = timestamp
        )
    }
}

data class PaginationMetadata(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("page_size")
    val pageSize: Int,
    
    @SerializedName("total_items")
    val totalItems: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("has_next")
    val hasNext: Boolean,
    
    @SerializedName("has_previous")
    val hasPrevious: Boolean
) {
    val isFirstPage: Boolean
        get() = page == 1
    
    val isLastPage: Boolean
        get() = !hasNext
}

data class ApiErrorResponse(
    @SerializedName("error")
    val error: ApiErrorDetail,
    
    @SerializedName("request_id")
    val requestId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long? = null
)

data class ApiErrorDetail(
    @SerializedName("code")
    val code: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("details")
    val details: String? = null,
    
    @SerializedName("field")
    val field: String? = null
) {
    fun toDisplayMessage(): String {
        return if (details != null && field != null) {
            "$message: $field - $details"
        } else if (details != null) {
            "$message: $details"
        } else {
            message
        }
    }
}
