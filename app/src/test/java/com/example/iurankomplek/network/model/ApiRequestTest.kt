package com.example.iurankomplek.network.model

import org.junit.Assert.*
import org.junit.Test

class ApiRequestTest {
    
    @Test
    fun `CreateVendorRequest with all fields`() {
        val request = CreateVendorRequest(
            name = "ACME Services",
            contactPerson = "John Smith",
            phoneNumber = "+1234567890",
            email = "john@acme.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC123",
            insuranceInfo = "INS456",
            contractStart = "2024-01-01",
            contractEnd = "2024-12-31"
        )
        
        assertEquals("ACME Services", request.name)
        assertEquals("John Smith", request.contactPerson)
        assertEquals("+1234567890", request.phoneNumber)
        assertEquals("john@acme.com", request.email)
        assertEquals("plumbing", request.specialty)
        assertEquals("123 Main St", request.address)
        assertEquals("LIC123", request.licenseNumber)
        assertEquals("INS456", request.insuranceInfo)
        assertEquals("2024-01-01", request.contractStart)
        assertEquals("2024-12-31", request.contractEnd)
    }
    
    @Test
    fun `UpdateVendorRequest with all fields`() {
        val request = UpdateVendorRequest(
            name = "ACME Services Updated",
            contactPerson = "Jane Doe",
            phoneNumber = "+0987654321",
            email = "jane@acme.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "LIC789",
            insuranceInfo = "INS012",
            contractStart = "2024-06-01",
            contractEnd = "2025-05-31",
            isActive = false
        )
        
        assertEquals("ACME Services Updated", request.name)
        assertEquals("Jane Doe", request.contactPerson)
        assertEquals("+0987654321", request.phoneNumber)
        assertEquals("jane@acme.com", request.email)
        assertEquals("electrical", request.specialty)
        assertEquals("456 Oak Ave", request.address)
        assertEquals("LIC789", request.licenseNumber)
        assertEquals("INS012", request.insuranceInfo)
        assertEquals("2024-06-01", request.contractStart)
        assertEquals("2025-05-31", request.contractEnd)
        assertFalse(request.isActive)
    }
    
    @Test
    fun `UpdateVendorRequest with active vendor`() {
        val request = UpdateVendorRequest(
            name = "ACME",
            contactPerson = "John",
            phoneNumber = "123",
            email = "john@acme.com",
            specialty = "plumbing",
            address = "123 St",
            licenseNumber = "LIC",
            insuranceInfo = "INS",
            contractStart = "2024-01-01",
            contractEnd = "2024-12-31",
            isActive = true
        )
        
        assertTrue(request.isActive)
    }
    
    @Test
    fun `CreateWorkOrderRequest with all fields`() {
        val request = CreateWorkOrderRequest(
            title = "Fix leaking faucet",
            description = "Kitchen sink is leaking",
            category = "plumbing",
            priority = "high",
            propertyId = "PROP123",
            reporterId = "USER456",
            estimatedCost = 150.0,
            attachments = listOf("photo1.jpg", "photo2.jpg")
        )
        
        assertEquals("Fix leaking faucet", request.title)
        assertEquals("Kitchen sink is leaking", request.description)
        assertEquals("plumbing", request.category)
        assertEquals("high", request.priority)
        assertEquals("PROP123", request.propertyId)
        assertEquals("USER456", request.reporterId)
        assertEquals(150.0, request.estimatedCost, 0.01)
        assertEquals(2, request.attachments.size)
        assertTrue(request.attachments.contains("photo1.jpg"))
    }
    
    @Test
    fun `CreateWorkOrderRequest with empty attachments`() {
        val request = CreateWorkOrderRequest(
            title = "Test",
            description = "Test description",
            category = "test",
            priority = "low",
            propertyId = "PROP1",
            reporterId = "USER1",
            estimatedCost = 0.0
        )
        
        assertTrue(request.attachments.isEmpty())
        assertEquals(0.0, request.estimatedCost, 0.01)
    }
    
    @Test
    fun `AssignVendorRequest with scheduledDate`() {
        val request = AssignVendorRequest(
            vendorId = "VEND123",
            scheduledDate = "2024-06-15T10:00:00Z"
        )
        
        assertEquals("VEND123", request.vendorId)
        assertEquals("2024-06-15T10:00:00Z", request.scheduledDate)
    }
    
    @Test
    fun `AssignVendorRequest with null scheduledDate`() {
        val request = AssignVendorRequest(
            vendorId = "VEND123",
            scheduledDate = null
        )
        
        assertEquals("VEND123", request.vendorId)
        assertNull(request.scheduledDate)
    }
    
    @Test
    fun `UpdateWorkOrderRequest with notes`() {
        val request = UpdateWorkOrderRequest(
            status = "in_progress",
            notes = "Vendor on site"
        )
        
        assertEquals("in_progress", request.status)
        assertEquals("Vendor on site", request.notes)
    }
    
    @Test
    fun `UpdateWorkOrderRequest without notes`() {
        val request = UpdateWorkOrderRequest(
            status = "completed",
            notes = null
        )
        
        assertEquals("completed", request.status)
        assertNull(request.notes)
    }
    
    @Test
    fun `SendMessageRequest with all fields`() {
        val request = SendMessageRequest(
            senderId = "USER123",
            receiverId = "USER456",
            content = "Hello, how are you?"
        )
        
        assertEquals("USER123", request.senderId)
        assertEquals("USER456", request.receiverId)
        assertEquals("Hello, how are you?", request.content)
    }
    
    @Test
    fun `CreateCommunityPostRequest with all fields`() {
        val request = CreateCommunityPostRequest(
            authorId = "USER123",
            title = "Community Event",
            content = "Join us for the annual block party!",
            category = "events"
        )
        
        assertEquals("USER123", request.authorId)
        assertEquals("Community Event", request.title)
        assertEquals("Join us for the annual block party!", request.content)
        assertEquals("events", request.category)
    }
    
    @Test
    fun `InitiatePaymentRequest with all fields`() {
        val request = InitiatePaymentRequest(
            amount = "150000.00",
            description = "Monthly fee",
            customerId = "USER123",
            paymentMethod = "BANK_TRANSFER"
        )
        
        assertEquals("150000.00", request.amount)
        assertEquals("Monthly fee", request.description)
        assertEquals("USER123", request.customerId)
        assertEquals("BANK_TRANSFER", request.paymentMethod)
    }
    
    @Test
    fun `InitiatePaymentRequest with different payment methods`() {
        val creditCardRequest = InitiatePaymentRequest(
            amount = "50000.00",
            description = "Payment",
            customerId = "USER1",
            paymentMethod = "CREDIT_CARD"
        )
        
        val eWalletRequest = InitiatePaymentRequest(
            amount = "75000.00",
            description = "Payment",
            customerId = "USER2",
            paymentMethod = "E_WALLET"
        )
        
        assertEquals("CREDIT_CARD", creditCardRequest.paymentMethod)
        assertEquals("E_WALLET", eWalletRequest.paymentMethod)
    }
}
