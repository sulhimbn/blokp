package com.example.iurankomplek.model

data class DataItem(
    val first_name : String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val alamat: String? = null,
    val iuran_perwarga: Int = 0,
    val total_iuran_rekap: Int = 0,
    val jumlah_iuran_bulanan: Int = 0,
    val total_iuran_individu: Int = 0,
    val pengeluaran_iuran_warga: Int = 0,
    val pemanfaatan_iuran: String? = null,
    val avatar: String? = null
)