package com.example.iurankomplek.model

import com.example.iurankomplek.utils.DataValidator

data class ValidatedDataItem(
    val firstName: String,
    val lastName: String,
    val email: String,
    val alamat: String,
    val iuranPerwarga: Int,
    val totalIuranRekap: Int,
    val jumlahIuranBulanan: Int,
    val totalIuranIndividu: Int,
    val pengeluaranIuranWarga: Int,
    val pemanfaatanIuran: String,
    val avatar: String
) {
    companion object {
        fun fromDataItem(dataItem: DataItem): ValidatedDataItem {
            return ValidatedDataItem(
                firstName = DataValidator.sanitizeName(dataItem.first_name),
                lastName = DataValidator.sanitizeName(dataItem.last_name),
                email = DataValidator.sanitizeEmail(dataItem.email),
                alamat = DataValidator.sanitizeAddress(dataItem.alamat),
                iuranPerwarga = DataValidator.sanitizeFinancialValue(dataItem.iuran_perwarga),
                totalIuranRekap = DataValidator.sanitizeFinancialValue(dataItem.total_iuran_rekap),
                jumlahIuranBulanan = DataValidator.sanitizeFinancialValue(dataItem.jumlah_iuran_bulanan),
                totalIuranIndividu = DataValidator.sanitizeFinancialValue(dataItem.total_iuran_individu),
                pengeluaranIuranWarga = DataValidator.sanitizeFinancialValue(dataItem.pengeluaran_iuran_warga),
                pemanfaatanIuran = DataValidator.sanitizePemanfaatan(dataItem.pemanfaatan_iuran),
                avatar = dataItem.avatar // Avatar already validated separately in image loading
            )
        }
    }
}