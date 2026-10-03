package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder

/**
 * Shared factories for production models.
 *
 * The production models are Gson-deserialized DTOs with every field required, so tests
 * that only care about a couple of fields would otherwise have to spell out all eleven
 * `DataItem` arguments. These helpers keep each test focused on the fields it asserts on
 * while still producing fully valid instances.
 */
object TestFixtures {

    fun dataItem(
        firstName: String? = "Test",
        lastName: String? = "User",
        email: String? = "test.user@example.com",
        alamat: String? = "123 Main St",
        iuranPerwarga: Int = 100,
        totalIuranRekap: Int = 500,
        jumlahIuranBulanan: Int = 200,
        totalIuranIndividu: Int = 150,
        pengeluaranIuranWarga: Int = 50,
        pemanfaatanIuran: String? = "Maintenance",
        avatar: String? = "https://example.com/avatar.jpg"
    ): DataItem = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = alamat,
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = totalIuranRekap,
        jumlah_iuran_bulanan = jumlahIuranBulanan,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaranIuranWarga,
        pemanfaatan_iuran = pemanfaatanIuran,
        avatar = avatar
    )

    fun vendor(
        id: String = "1",
        name: String = "Plumbing Services Inc",
        contactPerson: String = "John Smith",
        phoneNumber: String = "123-456-7890",
        email: String = "contact@plumbing.com",
        specialty: String = "plumbing",
        address: String = "123 Main St",
        licenseNumber: String = "PL-12345",
        insuranceInfo: String = "General liability coverage",
        certifications: List<String> = listOf("Licensed", "Bonded"),
        rating: Double = 4.5,
        totalReviews: Int = 25,
        contractStart: String = "2023-01-01",
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
        id: String = "WO-001",
        title: String = "Fix leaking pipe",
        description: String = "Kitchen sink pipe is leaking",
        category: String = "plumbing",
        priority: String = "high",
        status: String = "pending",
        vendorId: String? = null,
        vendorName: String? = null,
        assignedAt: String? = null,
        scheduledDate: String? = null,
        completedAt: String? = null,
        estimatedCost: Double = 150.0,
        actualCost: Double = 0.0,
        propertyId: String = "PROPERTY-001",
        reporterId: String = "USER-001",
        createdAt: String = "2023-06-15T10:30:00Z",
        updatedAt: String = "2023-06-15T10:30:00Z",
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
        id: String = "1",
        title: String = "Maintenance Notice",
        content: String = "Water will be off on Monday",
        category: String = "maintenance",
        priority: String = "normal",
        createdAt: String = "2023-06-15T10:30:00Z",
        readBy: List<String> = emptyList()
    ): Announcement = Announcement(
        id = id,
        title = title,
        content = content,
        category = category,
        priority = priority,
        createdAt = createdAt,
        readBy = readBy
    )

    fun message(
        id: String = "1",
        senderId: String = "USER-001",
        receiverId: String = "USER-002",
        content: String = "Hello",
        timestamp: String = "2023-06-15T10:30:00Z",
        readStatus: Boolean = false,
        attachments: List<String> = emptyList()
    ): Message = Message(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        timestamp = timestamp,
        readStatus = readStatus,
        attachments = attachments
    )
}
