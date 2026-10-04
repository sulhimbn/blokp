package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.Comment
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder

object TestFixtures {

    fun dataItem(
        firstName: String? = "John",
        lastName: String? = "Doe",
        email: String? = "john@example.com",
        alamat: String? = "Jl. Merdeka No. 1",
        iuranPerwarga: Int = 100_000,
        totalIuranRekap: Int = 300_000,
        jumlahIuranBulanan: Int = 100_000,
        totalIuranIndividu: Int = 150_000,
        pengeluaranIuranWarga: Int = 50_000,
        pemanfaatanIuran: String? = "Perbaikan jalan",
        avatar: String? = "https://example.com/a.jpg"
    ) = DataItem(
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
        id: String = "v-1",
        name: String = "CV Sinar Jaya",
        contactPerson: String = "Budi",
        phoneNumber: String = "0812000000",
        email: String = "budi@sinarjaya.test",
        specialty: String = "plumbing",
        address: String = "Jl. Industri 5",
        licenseNumber: String = "LIC-001",
        insuranceInfo: String = "AS-001",
        certifications: List<String> = listOf("PPRC"),
        rating: Double = 4.5,
        totalReviews: Int = 12,
        contractStart: String = "2026-01-01",
        contractEnd: String = "2026-12-31",
        isActive: Boolean = true
    ) = Vendor(
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
        title: String = "Pipa bocor",
        description: String = "Bocor di lantai 2",
        category: String = "plumbing",
        priority: String = "high",
        status: String = "pending",
        vendorId: String? = null,
        vendorName: String? = null,
        assignedAt: String? = null,
        scheduledDate: String? = null,
        completedAt: String? = null,
        estimatedCost: Double = 500_000.0,
        actualCost: Double = 0.0,
        propertyId: String = "prop-1",
        reporterId: String = "user-1",
        createdAt: String = "2026-01-01T00:00:00Z",
        updatedAt: String = "2026-01-01T00:00:00Z",
        attachments: List<String> = emptyList(),
        notes: List<String> = emptyList()
    ) = WorkOrder(
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
        title: String = "Pengumuman",
        content: String = "Isi pengumuman",
        category: String = "info",
        priority: String = "normal",
        createdAt: String = "2026-01-01T00:00:00Z",
        readBy: List<String> = emptyList()
    ) = Announcement(id, title, content, category, priority, createdAt, readBy)

    fun message(
        id: String = "msg-1",
        senderId: String = "user-1",
        receiverId: String = "user-2",
        content: String = "Halo",
        timestamp: String = "2026-01-01T00:00:00Z",
        readStatus: Boolean = false,
        attachments: List<String> = emptyList()
    ) = Message(id, senderId, receiverId, content, timestamp, readStatus, attachments)

    fun comment(
        id: String = "cmt-1",
        authorId: String = "user-2",
        content: String = "Setuju",
        timestamp: String = "2026-01-01T00:00:00Z"
    ) = Comment(id, authorId, content, timestamp)

    fun user(
        id: String = "user-1",
        email: String = "john@example.com",
        firstName: String = "John",
        lastName: String = "Doe",
        avatar: String? = null
    ) = User(id, email, firstName, lastName, avatar)
}
