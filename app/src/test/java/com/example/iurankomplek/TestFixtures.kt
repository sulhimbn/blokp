package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder

/**
 * Shared builders for production models.
 *
 * `DataItem`, `Vendor` and `WorkOrder` declare every property as required, which makes
 * positional construction in tests unreadable and forces each test to repeat a wall of
 * irrelevant defaults. These builders keep the values a test actually cares about visible
 * and let everything else default.
 */
object TestFixtures {

    fun dataItem(
        firstName: String? = "John",
        lastName: String? = "Doe",
        email: String? = "john.doe@example.com",
        alamat: String? = "Jl. Test 1",
        iuranPerwarga: Int = 0,
        totalIuranRekap: Int = 0,
        jumlahIuranBulanan: Int = 0,
        totalIuranIndividu: Int = 0,
        pengeluaranIuranWarga: Int = 0,
        pemanfaatanIuran: String? = "Test",
        avatar: String? = ""
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
        id: String = "vendor-1",
        name: String = "Acme Plumbing",
        contactPerson: String = "Budi Santoso",
        phoneNumber: String = "08123456789",
        email: String = "acme@example.com",
        specialty: String = "plumbing",
        address: String = "Jl. Vendor 1",
        licenseNumber: String = "LIC-001",
        insuranceInfo: String = "INS-001",
        certifications: List<String> = listOf("S1", "S2"),
        rating: Double = 4.5,
        totalReviews: Int = 12,
        contractStart: String = "2024-01-01",
        contractEnd: String = "2025-01-01",
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
        title: String = "Leaking pipe",
        description: String = "Water leaking under sink",
        category: String = "plumbing",
        priority: String = "high",
        status: String = "pending",
        vendorId: String? = null,
        vendorName: String? = null,
        assignedAt: String? = null,
        scheduledDate: String? = null,
        completedAt: String? = null,
        estimatedCost: Double = 100.0,
        actualCost: Double = 0.0,
        propertyId: String = "Blok A",
        reporterId: String = "user-1",
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
}