package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder

/**
 * Shared builders for production models.
 *
 * The production models declare every field as required, so tests that only care about a
 * handful of fields would otherwise have to spell out the rest. These factories keep the
 * fields a test does not exercise explicit at the call site of the fixture instead.
 */
object TestFixtures {

    fun dataItem(
        first_name: String? = "John",
        last_name: String? = "Doe",
        email: String? = "john@example.com",
        alamat: String? = "Jl. Contoh No. 1",
        iuran_perwarga: Int = 0,
        total_iuran_rekap: Int = 0,
        jumlah_iuran_bulanan: Int = 0,
        total_iuran_individu: Int = 0,
        pengeluaran_iuran_warga: Int = 0,
        pemanfaatan_iuran: String? = "Perbaikan jalan komplek",
        avatar: String? = "https://example.com/avatar1.jpg"
    ): DataItem = DataItem(
        first_name = first_name,
        last_name = last_name,
        email = email,
        alamat = alamat,
        iuran_perwarga = iuran_perwarga,
        total_iuran_rekap = total_iuran_rekap,
        jumlah_iuran_bulanan = jumlah_iuran_bulanan,
        total_iuran_individu = total_iuran_individu,
        pengeluaran_iuran_warga = pengeluaran_iuran_warga,
        pemanfaatan_iuran = pemanfaatan_iuran,
        avatar = avatar
    )

    fun user(
        id: String = "user-1",
        email: String = "john@example.com",
        firstName: String = "John",
        lastName: String = "Doe",
        avatar: String? = "https://example.com/avatar1.jpg"
    ): User = User(id, email, firstName, lastName, avatar)

    fun vendor(
        id: String = "vendor-1",
        name: String = "Vendor 1",
        contactPerson: String = "John Doe",
        phoneNumber: String = "1234567890",
        email: String = "vendor1@example.com",
        specialty: String = "Cleaning",
        address: String = "123 Main St",
        licenseNumber: String = "LICENSE123",
        insuranceInfo: String = "INSURANCE123",
        certifications: List<String> = listOf("ISO9001"),
        rating: Double = 4.5,
        totalReviews: Int = 10,
        contractStart: String = "2024-01-01",
        contractEnd: String = "2024-12-31",
        isActive: Boolean = true
    ): Vendor = Vendor(
        id = id,
        name = name,
        contactPerson = contactPerson,
        phoneNumber = phoneNumber,
        email = email,
        specialty = specialty,
        address = address,
        licenseNumber = licenseNumber,
        insuranceInfo = insuranceInfo,
        certifications = certifications,
        rating = rating,
        totalReviews = totalReviews,
        contractStart = contractStart,
        contractEnd = contractEnd,
        isActive = isActive
    )

    fun workOrder(
        id: String = "wo-1",
        title: String = "Fix Leaking Pipe",
        description: String = "Pipe is leaking in bathroom",
        category: String = "Plumbing",
        priority: String = "high",
        status: String = "pending",
        vendorId: String? = null,
        vendorName: String? = null,
        assignedAt: String? = null,
        scheduledDate: String? = null,
        completedAt: String? = null,
        estimatedCost: Double = 150.0,
        actualCost: Double = 0.0,
        propertyId: String = "prop1",
        reporterId: String = "user1",
        createdAt: String = "2024-01-01T00:00:00Z",
        updatedAt: String = "2024-01-01T00:00:00Z",
        attachments: List<String> = emptyList(),
        notes: List<String> = emptyList()
    ): WorkOrder = WorkOrder(
        id = id,
        title = title,
        description = description,
        category = category,
        priority = priority,
        status = status,
        vendorId = vendorId,
        vendorName = vendorName,
        assignedAt = assignedAt,
        scheduledDate = scheduledDate,
        completedAt = completedAt,
        estimatedCost = estimatedCost,
        actualCost = actualCost,
        propertyId = propertyId,
        reporterId = reporterId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        attachments = attachments,
        notes = notes
    )

    fun announcement(
        id: String = "ann-1",
        title: String = "Announcement 1",
        content: String = "Content 1",
        category: String = "general",
        priority: String = "normal",
        createdAt: String = "2024-01-01T00:00:00Z",
        readBy: List<String> = emptyList()
    ): Announcement = Announcement(id, title, content, category, priority, createdAt, readBy)

    fun message(
        id: String = "msg-1",
        senderId: String = "user1",
        receiverId: String = "vendor1",
        content: String = "Hello",
        timestamp: String = "2024-01-01T00:00:00Z",
        readStatus: Boolean = false,
        attachments: List<String> = emptyList()
    ): Message = Message(id, senderId, receiverId, content, timestamp, readStatus, attachments)

    fun communityPost(
        id: String = "post-1",
        authorId: String = "user1",
        title: String = "Post 1",
        content: String = "Content 1",
        category: String = "general",
        likes: Int = 0,
        comments: List<com.example.iurankomplek.model.Comment> = emptyList(),
        createdAt: String = "2024-01-01T00:00:00Z"
    ): CommunityPost = CommunityPost(id, authorId, title, content, category, likes, comments, createdAt)
}