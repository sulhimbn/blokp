package com.example.iurankomplek.model

data class DataItem(
    val first_name : String,
    val last_name: String,
    val email: String,
    val alamat: String,
    val iuran_perwarga: Int,
    val total_iuran_rekap: Int,
    val jumlah_iuran_bulanan: Int,
    val total_iuran_individu: Int,
    val pengeluaran_iuran_warga: Int,
    val pemanfaatan_iuran: String,
    val avatar: String
) {
    /**
     * Validates that this DataItem's financial values are within acceptable limits
     * @return true if all financial values are valid, false otherwise
     */
    fun isValid(): Boolean {
        return iuran_perwarga >= 0 && 
               iuran_perwarga <= 100_000_000 &&
               total_iuran_rekap >= 0 && 
               total_iuran_rekap <= 100_000_000 &&
               jumlah_iuran_bulanan >= 0 && 
               jumlah_iuran_bulanan <= 100_000_000 &&
               total_iuran_individu >= 0 && 
               total_iuran_individu <= 100_000_000 &&
               pengeluaran_iuran_warga >= 0 && 
               pengeluaran_iuran_warga <= 100_000_000
    }
    
    /**
     * Sanitizes this DataItem by ensuring all financial values are within acceptable limits
     * @return A new DataItem with sanitized values
     */
    fun sanitized(): DataItem {
        return copy(
            iuran_perwarga = iuran_perwarga.coerceIn(0, 100_000_000),
            total_iuran_rekap = total_iuran_rekap.coerceIn(0, 100_000_000),
            jumlah_iuran_bulanan = jumlah_iuran_bulanan.coerceIn(0, 100_000_000),
            total_iuran_individu = total_iuran_individu.coerceIn(0, 100_000_000),
            pengeluaran_iuran_warga = pengeluaran_iuran_warga.coerceIn(0, 100_000_000)
        )
    }
}