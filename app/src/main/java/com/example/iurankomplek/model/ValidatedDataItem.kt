package com.example.iurankomplek.model

import com.example.iurankomplek.utils.InputSanitizer

/**
 * DataItem with validation and sanitization
 */
data class ValidatedDataItem(
    val first_name: String,
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
    companion object {
        /**
         * Creates a ValidatedDataItem from a regular DataItem, with validation and sanitization
         */
        fun fromDataItem(dataItem: DataItem): ValidatedDataItem {
            // Validate financial values to be non-negative
            require(dataItem.iuran_perwarga >= 0) { "iuran_perwarga must be non-negative" }
            require(dataItem.total_iuran_rekap >= 0) { "total_iuran_rekap must be non-negative" }
            require(dataItem.jumlah_iuran_bulanan >= 0) { "jumlah_iuran_bulanan must be non-negative" }
            require(dataItem.total_iuran_individu >= 0) { "total_iuran_individu must be non-negative" }
            require(dataItem.pengeluaran_iuran_warga >= 0) { "pengeluaran_iuran_warga must be non-negative" }
            
            // Check for potential overflow issues
            require(dataItem.iuran_perwarga <= Int.MAX_VALUE / 2) { "iuran_perwarga value too large" }
            require(dataItem.total_iuran_rekap <= Int.MAX_VALUE / 2) { "total_iuran_rekap value too large" }
            require(dataItem.jumlah_iuran_bulanan <= Int.MAX_VALUE / 2) { "jumlah_iuran_bulanan value too large" }
            require(dataItem.total_iuran_individu <= Int.MAX_VALUE / 6) { "total_iuran_individu value too large (for multiplication by 3)" }
            require(dataItem.pengeluaran_iuran_warga <= Int.MAX_VALUE / 2) { "pengeluaran_iuran_warga value too large" }
            
            return ValidatedDataItem(
                first_name = InputSanitizer.sanitizeName(dataItem.first_name),
                last_name = InputSanitizer.sanitizeName(dataItem.last_name),
                email = InputSanitizer.sanitizeEmail(dataItem.email),
                alamat = InputSanitizer.sanitizeAddress(dataItem.alamat),
                iuran_perwarga = dataItem.iuran_perwarga,
                total_iuran_rekap = dataItem.total_iuran_rekap,
                jumlah_iuran_bulanan = dataItem.jumlah_iuran_bulanan,
                total_iuran_individu = dataItem.total_iuran_individu,
                pengeluaran_iuran_warga = dataItem.pengeluaran_iuran_warga,
                pemanfaatan_iuran = InputSanitizer.sanitizePemanfaatan(dataItem.pemanfaatan_iuran),
                avatar = dataItem.avatar
            )
        }
        
        /**
         * Creates a ValidatedDataItem with basic validation
         */
        fun createValidated(
            first_name: String,
            last_name: String,
            email: String,
            alamat: String,
            iuran_perwarga: Int,
            total_iuran_rekap: Int,
            jumlah_iuran_bulanan: Int,
            total_iuran_individu: Int,
            pengeluaran_iuran_warga: Int,
            pemanfaatan_iuran: String,
            avatar: String
        ): ValidatedDataItem {
            require(iuran_perwarga >= 0) { "iuran_perwarga must be non-negative" }
            require(total_iuran_rekap >= 0) { "total_iuran_rekap must be non-negative" }
            require(jumlah_iuran_bulanan >= 0) { "jumlah_iuran_bulanan must be non-negative" }
            require(total_iuran_individu >= 0) { "total_iuran_individu must be non-negative" }
            require(pengeluaran_iuran_warga >= 0) { "pengeluaran_iuran_warga must be non-negative" }
            
            require(iuran_perwarga <= Int.MAX_VALUE / 2) { "iuran_perwarga value too large" }
            require(total_iuran_rekap <= Int.MAX_VALUE / 2) { "total_iuran_rekap value too large" }
            require(jumlah_iuran_bulanan <= Int.MAX_VALUE / 2) { "jumlah_iuran_bulanan value too large" }
            require(total_iuran_individu <= Int.MAX_VALUE / 6) { "total_iuran_individu value too large (for multiplication by 3)" }
            require(pengeluaran_iuran_warga <= Int.MAX_VALUE / 2) { "pengeluaran_iuran_warga value too large" }
            
            return ValidatedDataItem(
                first_name = InputSanitizer.sanitizeName(first_name),
                last_name = InputSanitizer.sanitizeName(last_name),
                email = InputSanitizer.sanitizeEmail(email),
                alamat = InputSanitizer.sanitizeAddress(alamat),
                iuran_perwarga = iuran_perwarga,
                total_iuran_rekap = total_iuran_rekap,
                jumlah_iuran_bulanan = jumlah_iuran_bulanan,
                total_iuran_individu = total_iuran_individu,
                pengeluaran_iuran_warga = pengeluaran_iuran_warga,
                pemanfaatan_iuran = InputSanitizer.sanitizePemanfaatan(pemanfaatan_iuran),
                avatar = avatar
            )
        }
    }
}