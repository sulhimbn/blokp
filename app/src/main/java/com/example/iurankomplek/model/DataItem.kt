package com.example.iurankomplek.model

data class DataItem(
    val first_name : String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val alamat: String? = null,
    val iuran_perwarga: Int,
    val total_iuran_rekap: Int,
    val jumlah_iuran_bulanan: Int,
    val total_iuran_individu: Int,
    val pengeluaran_iuran_warga: Int,
    val pemanfaatan_iuran: String? = null,
    val avatar: String? = null
)