package com.example.iurankomplek.network.model

import org.junit.Assert.*
import org.junit.Test

class ApiResponseTest {
    
    @Test
    fun `ApiResponse with data and metadata`() {
        val requestData = mapOf("id" to "123", "name" to "Test")
        val response = ApiResponse(
            data = requestData,
            requestId = "test-request-id",
            timestamp = 1234567890000L
        )
        
        assertEquals(requestData, response.data)
        assertEquals("test-request-id", response.requestId)
        assertEquals(1234567890000L, response.timestamp)
    }
    
    @Test
    fun `ApiResponse with null requestId`() {
        val requestData = "test-data"
        val response = ApiResponse(
            data = requestData,
            requestId = null
        )
        
        assertEquals(requestData, response.data)
        assertNull(response.requestId)
        assertTrue(response.timestamp > 0)
    }
    
    @Test
    fun `ApiResponse with default timestamp`() {
        val response = ApiResponse(data = "test")
        
        assertNotNull(response.timestamp)
        val expectedMaxTime = System.currentTimeMillis() + 1000
        assertTrue(response.timestamp <= expectedMaxTime)
    }
    
    @Test
    fun `ApiListResponse with pagination`() {
        val data = listOf("item1", "item2", "item3")
        val pagination = PaginationMetadata(
            page = 1,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = true,
            hasPrevious = false
        )
        
        val response = ApiListResponse(
            data = data,
            pagination = pagination,
            requestId = "test-request-id"
        )
        
        assertEquals(data, response.data)
        assertEquals(pagination, response.pagination)
        assertEquals("test-request-id", response.requestId)
        assertTrue(response.timestamp > 0)
    }
    
    @Test
    fun `ApiListResponse without pagination`() {
        val data = listOf("item1", "item2")
        val response = ApiListResponse(data = data)
        
        assertEquals(data, response.data)
        assertNull(response.pagination)
        assertNull(response.requestId)
        assertTrue(response.timestamp > 0)
    }
    
    @Test
    fun `ApiListResponse with empty data`() {
        val response = ApiListResponse<String>(data = emptyList())
        
        assertTrue(response.data.isEmpty())
        assertNull(response.pagination)
        assertNull(response.requestId)
    }
    
    @Test
    fun `PaginationMetadata with all fields`() {
        val pagination = PaginationMetadata(
            page = 1,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = true,
            hasPrevious = false
        )
        
        assertEquals(1, pagination.page)
        assertEquals(20, pagination.pageSize)
        assertEquals(100, pagination.totalItems)
        assertEquals(5, pagination.totalPages)
        assertTrue(pagination.hasNext)
        assertFalse(pagination.hasPrevious)
    }
    
    @Test
    fun `PaginationMetadata first page`() {
        val pagination = PaginationMetadata(
            page = 1,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = true,
            hasPrevious = false
        )
        
        assertFalse(pagination.hasPrevious)
        assertTrue(pagination.hasNext)
    }
    
    @Test
    fun `PaginationMetadata last page`() {
        val pagination = PaginationMetadata(
            page = 5,
            pageSize = 20,
            totalItems = 100,
            totalPages = 5,
            hasNext = false,
            hasPrevious = true
        )
        
        assertTrue(pagination.hasPrevious)
        assertFalse(pagination.hasNext)
    }
    
    @Test
    fun `PaginationMetadata single page`() {
        val pagination = PaginationMetadata(
            page = 1,
            pageSize = 20,
            totalItems = 15,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false
        )
        
        assertFalse(pagination.hasPrevious)
        assertFalse(pagination.hasNext)
        assertEquals(1, pagination.totalPages)
    }
    
    @Test
    fun `ApiError with all fields`() {
        val error = ApiError(
            code = "VALIDATION_ERROR",
            message = "Invalid email format",
            details = "Email must contain @ symbol",
            requestId = "test-request-id",
            timestamp = 1234567890000L
        )
        
        assertEquals("VALIDATION_ERROR", error.code)
        assertEquals("Invalid email format", error.message)
        assertEquals("Email must contain @ symbol", error.details)
        assertEquals("test-request-id", error.requestId)
        assertEquals(1234567890000L, error.timestamp)
    }
    
    @Test
    fun `ApiError with minimal fields`() {
        val error = ApiError(
            code = "NOT_FOUND",
            message = "Resource not found"
        )
        
        assertEquals("NOT_FOUND", error.code)
        assertEquals("Resource not found", error.message)
        assertNull(error.details)
        assertNull(error.requestId)
        assertTrue(error.timestamp > 0)
    }
    
    @Test
    fun `ApiError with null details`() {
        val error = ApiError(
            code = "INTERNAL_SERVER_ERROR",
            message = "Server error",
            details = null
        )
        
        assertNull(error.details)
        assertEquals("INTERNAL_SERVER_ERROR", error.code)
    }
}
