package com.example.iurankomplek.data.entity

import java.util.Date

data class FinancialRecordEntity(
    val id: Long = 0,
    val userId: Long,
    val iuranPerwarga: Int,
    val jumlahIuranBulanan: Int,
    val totalIuranIndividu: Int,
    val pengeluaranIuranWarga: Int,
    val totalIuranRekap: Int,
    val pemanfaatanIuran: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    init {
        validate()
    }

    private fun validate() {
        require(userId > 0) { "User ID must be positive" }
        require(iuranPerwarga >= 0) { "Iuran perwarga cannot be negative" }
        require(jumlahIuranBulanan >= 0) { "Jumlah iuran bulanan cannot be negative" }
        require(totalIuranIndividu >= 0) { "Total iuran individu cannot be negative" }
        require(pengeluaranIuranWarga >= 0) { "Pengeluaran iuran warga cannot be negative" }
        require(totalIuranRekap >= 0) { "Total iuran rekap cannot be negative" }
        require(pemanfaatanIuran.isNotBlank()) { "Pemanfaatan iuran cannot be blank" }
    }
}
