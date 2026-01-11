package com.example.iurankomplek.data.dto

data class FinancialDto(
    val iuran_perwarga: Int,
    val jumlah_iuran_bulanan: Int,
    val total_iuran_individu: Int,
    val pengeluaran_iuran_warga: Int,
    val total_iuran_rekap: Int,
    val pemanfaatan_iuran: String
)
