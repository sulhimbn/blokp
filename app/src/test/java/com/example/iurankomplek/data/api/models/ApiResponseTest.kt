package com.example.iurankomplek.data.api.models

import org.junit.Assert.*
import org.junit.Test

class ApiResponseTest {
    
    @Test
    fun `ApiResponse success creates valid response`() {
        val data = "test data"
        val response = ApiResponse.success(data)
        
        assertEquals(data, response.data)
        assertNull(response.requestId)
        assertNull(response.timestamp)
    }
    
    @Test
    fun `ApiResponse successWithMetadata creates valid response with metadata`() {
        val data = "test data"
        val requestId = "test-request-id"
        val timestamp = System.currentTimeMillis()
        
        val response = ApiResponse.successWithMetadata(data, requestId, timestamp)
        
        assertEquals(data, response.data)
        assertEquals(requestId, response.requestId)
        assertEquals(timestamp, response.timestamp)
    }
    
    @Test
    fun `ApiListResponse success creates valid list response`() {
        val data = listOf("item1", "item2", "item3")
        val response = ApiListResponse.success(data)
        
        assertEquals(data, response.data)
        assertNull(response.pagination)
        assertNull(response.requestId)
        assertNull(response.timestamp)
    }
    
    @Test
    fun `ApiListResponse successWithMetadata creates valid list response with metadata`() {
        val data = listOf("item1", "item2")
        val requestId = "test-request-id"
        val timestamp = System.currentTimeMillis()
        val pagination = PaginationMetadata(
            page = 1,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = true,
            hasPrevious = false
        )
        
        val response = ApiListResponse.successWithMetadata(data, requestId, timestamp, pagination)
        
        assertEquals(data, response.data)
        assertEquals(pagination, response.pagination)
        assertEquals(requestId, response.requestId)
        assertEquals(timestamp, response.timestamp)
    }
}

class PaginationMetadataTest {
    
    @Test
    fun `isFirstPage returns true for page 1`() {
        val pagination = PaginationMetadata(
            page = 1,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = true,
            hasPrevious = false
        )
        
        assertTrue(pagination.isFirstPage)
    }
    
    @Test
    fun `isFirstPage returns false for page 2`() {
        val pagination = PaginationMetadata(
            page = 2,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = true,
            hasPrevious = true
        )
        
        assertFalse(pagination.isFirstPage)
    }
    
    @Test
    fun `isLastPage returns true when hasNext is false`() {
        val pagination = PaginationMetadata(
            page = 5,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = false,
            hasPrevious = true
        )
        
        assertTrue(pagination.isLastPage)
    }
    
    @Test
    fun `isLastPage returns false when hasNext is true`() {
        val pagination = PaginationMetadata(
            page = 1,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = true,
            hasPrevious = false
        )
        
        assertFalse(pagination.isLastPage)
    }
}

class ApiErrorDetailTest {
    
    @Test
    fun `toDisplayMessage returns message when details and field are null`() {
        val error = ApiErrorDetail(
            code = "VALIDATION_ERROR",
            message = "Invalid input"
        )
        
        assertEquals("Invalid input", error.toDisplayMessage())
    }
    
    @Test
    fun `toDisplayMessage returns message and details when field is null`() {
        val error = ApiErrorDetail(
            code = "VALIDATION_ERROR",
            message = "Invalid input",
            details = "Email format is incorrect"
        )
        
        assertEquals("Invalid input: Email format is incorrect", error.toDisplayMessage())
    }
    
    @Test
    fun `toDisplayMessage returns full message with field and details`() {
        val error = ApiErrorDetail(
            code = "VALIDATION_ERROR",
            message = "Invalid input",
            details = "Email format is incorrect",
            field = "email"
        )
        
        assertEquals("Invalid input: email - Email format is incorrect", error.toDisplayMessage())
    }
}
