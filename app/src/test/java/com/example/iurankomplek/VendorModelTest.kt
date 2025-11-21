package com.example.iurankomplek

import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder
import org.junit.Test
import org.junit.Assert.*

class VendorModelTest {
    
    @Test
    fun `vendor model should be created with correct properties`() {
        val vendor = Vendor(
            id = "1",
            name = "Plumbing Services Inc",
            contactPerson = "John Smith",
            phoneNumber = "123-456-7890",
            email = "contact@plumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "PL-12345",
            insuranceInfo = "General liability coverage",
            certifications = listOf("Licensed", "Bonded"),
            rating = 4.5,
            totalReviews = 25,
            contractStart = "2023-01-01",
            contractEnd = "2024-12-31",
            isActive = true
        )
        
        assertEquals("1", vendor.id)
        assertEquals("Plumbing Services Inc", vendor.name)
        assertEquals("John Smith", vendor.contactPerson)
        assertEquals("123-456-7890", vendor.phoneNumber)
        assertEquals("contact@plumbing.com", vendor.email)
        assertEquals("plumbing", vendor.specialty)
        assertEquals("123 Main St", vendor.address)
        assertEquals("PL-12345", vendor.licenseNumber)
        assertEquals("General liability coverage", vendor.insuranceInfo)
        assertEquals(listOf("Licensed", "Bonded"), vendor.certifications)
        assertEquals(4.5, vendor.rating, 0.01)
        assertEquals(25, vendor.totalReviews)
        assertEquals("2023-01-01", vendor.contractStart)
        assertEquals("2024-12-31", vendor.contractEnd)
        assertTrue(vendor.isActive)
    }
    
    @Test
    fun `workOrder model should be created with correct properties`() {
        val workOrder = WorkOrder(
            id = "WO-001",
            title = "Fix leaking pipe",
            description = "Kitchen sink pipe is leaking",
            category = "plumbing",
            priority = "high",
            status = "pending",
            vendorId = null,
            vendorName = null,
            assignedAt = null,
            scheduledDate = null,
            completedAt = null,
            estimatedCost = 150.0,
            actualCost = 0.0,
            propertyId = "PROPERTY-001",
            reporterId = "USER-001",
            createdAt = "2023-06-15T10:30:00Z",
            updatedAt = "2023-06-15T10:30:00Z",
            attachments = listOf(),
            notes = listOf()
        )
        
        assertEquals("WO-001", workOrder.id)
        assertEquals("Fix leaking pipe", workOrder.title)
        assertEquals("Kitchen sink pipe is leaking", workOrder.description)
        assertEquals("plumbing", workOrder.category)
        assertEquals("high", workOrder.priority)
        assertEquals("pending", workOrder.status)
        assertNull(workOrder.vendorId)
        assertNull(workOrder.vendorName)
        assertEquals(150.0, workOrder.estimatedCost, 0.01)
        assertEquals(0.0, workOrder.actualCost, 0.01)
        assertEquals("PROPERTY-001", workOrder.propertyId)
        assertEquals("USER-001", workOrder.reporterId)
    }
}