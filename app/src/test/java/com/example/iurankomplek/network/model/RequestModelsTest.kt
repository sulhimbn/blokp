package com.example.iurankomplek.network.model

import org.junit.Assert.*
import org.junit.Test

class CreateVendorRequestTest {

    @Test
    fun `CreateVendorRequest should contain all required fields`() {
        val request = CreateVendorRequest(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "123-456-7890",
            email = "contact@abcplumbing.com",
            specialty = "Plumbing",
            address = "123 Main St, City",
            licenseNumber = "PLM-12345",
            insuranceInfo = "Policy #INS-54321",
            contractStart = "2024-01-01",
            contractEnd = "2024-12-31"
        )

        assertEquals("ABC Plumbing", request.name)
        assertEquals("John Doe", request.contactPerson)
        assertEquals("123-456-7890", request.phoneNumber)
        assertEquals("contact@abcplumbing.com", request.email)
        assertEquals("Plumbing", request.specialty)
        assertEquals("123 Main St, City", request.address)
        assertEquals("PLM-12345", request.licenseNumber)
        assertEquals("Policy #INS-54321", request.insuranceInfo)
        assertEquals("2024-01-01", request.contractStart)
        assertEquals("2024-12-31", request.contractEnd)
    }

    @Test
    fun `CreateVendorRequest should handle empty strings`() {
        val request = CreateVendorRequest(
            name = "",
            contactPerson = "",
            phoneNumber = "",
            email = "",
            specialty = "",
            address = "",
            licenseNumber = "",
            insuranceInfo = "",
            contractStart = "",
            contractEnd = ""
        )

        assertEquals("", request.name)
        assertEquals("", request.contactPerson)
        assertEquals("", request.phoneNumber)
        assertEquals("", request.email)
        assertEquals("", request.specialty)
        assertEquals("", request.address)
        assertEquals("", request.licenseNumber)
        assertEquals("", request.insuranceInfo)
        assertEquals("", request.contractStart)
        assertEquals("", request.contractEnd)
    }

    @Test
    fun `CreateVendorRequest should handle special characters in fields`() {
        val request = CreateVendorRequest(
            name = "O'Brien's Electrical",
            contactPerson = "Mary O'Connor-Smith",
            phoneNumber = "+1 (555) 123-4567",
            email = "info@obriens-electrical.com",
            specialty = "Electrical & HVAC",
            address = "Apt 4B, 789 Main St, New York, NY 10001",
            licenseNumber = "ELEC-2024-001",
            insuranceInfo = "Policy: LIA-2024-9999; Liability: $1M",
            contractStart = "2024-01-15",
            contractEnd = "2025-01-14"
        )

        assertTrue(request.name.contains("'"))
        assertTrue(request.contactPerson.contains("-"))
        assertTrue(request.email.contains("@"))
        assertTrue(request.insuranceInfo.contains(";"))
    }
}

class SendMessageRequestTest {

    @Test
    fun `SendMessageRequest should contain all fields`() {
        val request = SendMessageRequest(
            senderId = "user_123",
            receiverId = "user_456",
            content = "Hello, how are you?",
            attachments = listOf("file1.pdf", "file2.jpg")
        )

        assertEquals("user_123", request.senderId)
        assertEquals("user_456", request.receiverId)
        assertEquals("Hello, how are you?", request.content)
        assertEquals(2, request.attachments.size)
        assertEquals("file1.pdf", request.attachments[0])
        assertEquals("file2.jpg", request.attachments[1])
    }

    @Test
    fun `SendMessageRequest should default attachments to empty list`() {
        val request = SendMessageRequest(
            senderId = "user_123",
            receiverId = "user_456",
            content = "Test message"
        )

        assertTrue(request.attachments.isEmpty())
    }

    @Test
    fun `SendMessageRequest should handle empty content`() {
        val request = SendMessageRequest(
            senderId = "user_123",
            receiverId = "user_456",
            content = ""
        )

        assertEquals("", request.content)
    }

    @Test
    fun `SendMessageRequest should handle long content`() {
        val longContent = "A".repeat(1000)
        val request = SendMessageRequest(
            senderId = "user_123",
            receiverId = "user_456",
            content = longContent
        )

        assertEquals(1000, request.content.length)
    }

    @Test
    fun `SendMessageRequest should handle multiple attachments`() {
        val attachments = listOf("file1.pdf", "file2.jpg", "file3.png", "file4.docx")
        val request = SendMessageRequest(
            senderId = "user_123",
            receiverId = "user_456",
            content = "Multiple files attached",
            attachments = attachments
        )

        assertEquals(4, request.attachments.size)
    }
}

class CreateCommunityPostRequestTest {

    @Test
    fun `CreateCommunityPostRequest should contain all fields`() {
        val request = CreateCommunityPostRequest(
            authorId = "user_789",
            title = "Community Meeting Announcement",
            content = "Join us for our monthly community meeting this Saturday at 10 AM.",
            category = "Announcements"
        )

        assertEquals("user_789", request.authorId)
        assertEquals("Community Meeting Announcement", request.title)
        assertEquals("Join us for our monthly community meeting this Saturday at 10 AM.", request.content)
        assertEquals("Announcements", request.category)
    }

    @Test
    fun `CreateCommunityPostRequest should handle empty content`() {
        val request = CreateCommunityPostRequest(
            authorId = "user_789",
            title = "Empty Post",
            content = "",
            category = "General"
        )

        assertEquals("", request.content)
    }

    @Test
    fun `CreateCommunityPostRequest should handle special characters in content`() {
        val request = CreateCommunityPostRequest(
            authorId = "user_789",
            title = "Special Characters Test",
            content = "Hello! @user #hashtag\nLine 1\nLine 2\n\tTabbed text",
            category = "General"
        )

        assertTrue(request.content.contains("@"))
        assertTrue(request.content.contains("#"))
        assertTrue(request.content.contains("\n"))
        assertTrue(request.content.contains("\t"))
    }

    @Test
    fun `CreateCommunityPostRequest should handle unicode content`() {
        val request = CreateCommunityPostRequest(
            authorId = "user_789",
            title = "Unicode Test",
            content = "Hello 世界 🌍\nمرحبا بالعالم\nこんにちは",
            category = "General"
        )

        assertTrue(request.content.contains("世界"))
        assertTrue(request.content.contains("🌍"))
        assertTrue(request.content.contains("مرحبا"))
        assertTrue(request.content.contains("こんにちは"))
    }
}

class InitiatePaymentRequestTest {

    @Test
    fun `InitiatePaymentRequest should contain all fields`() {
        val request = InitiatePaymentRequest(
            userId = "user_123",
            amount = 150000.50,
            paymentMethod = "BANK_TRANSFER",
            description = "Monthly fee - January 2026"
        )

        assertEquals("user_123", request.userId)
        assertEquals(150000.50, request.amount, 0.001)
        assertEquals("BANK_TRANSFER", request.paymentMethod)
        assertEquals("Monthly fee - January 2026", request.description)
    }

    @Test
    fun `InitiatePaymentRequest should default description to null`() {
        val request = InitiatePaymentRequest(
            userId = "user_123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD"
        )

        assertNull(request.description)
    }

    @Test
    fun `InitiatePaymentRequest should handle zero amount`() {
        val request = InitiatePaymentRequest(
            userId = "user_123",
            amount = 0.0,
            paymentMethod = "CASH"
        )

        assertEquals(0.0, request.amount, 0.001)
    }

    @Test
    fun `InitiatePaymentRequest should handle large amounts`() {
        val request = InitiatePaymentRequest(
            userId = "user_123",
            amount = 10000000.0,
            paymentMethod = "BANK_TRANSFER"
        )

        assertEquals(10000000.0, request.amount, 0.001)
    }

    @Test
    fun `InitiatePaymentRequest should handle decimal amounts`() {
        val request = InitiatePaymentRequest(
            userId = "user_123",
            amount = 123.45,
            paymentMethod = "E_WALLET"
        )

        assertEquals(123.45, request.amount, 0.001)
    }

    @Test
    fun `InitiatePaymentRequest should handle empty description`() {
        val request = InitiatePaymentRequest(
            userId = "user_123",
            amount = 50000.0,
            paymentMethod = "CASH",
            description = ""
        )

        assertEquals("", request.description)
    }
}
