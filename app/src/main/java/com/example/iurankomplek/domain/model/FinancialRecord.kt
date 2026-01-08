package com.example.iurankomplek.domain.model

/**
 * Domain model representing a financial record in the business domain.
 * 
 * This is a pure domain model representing the FinancialRecord entity in the business layer.
 * It contains business logic and validation rules independent of any framework.
 * 
 * @property id Unique identifier for the financial record
 * @property userId ID of the user who owns this financial record
 * @property iuranPerwarga Monthly contribution per resident
 * @property jumlahIuranBulanan Total monthly contribution amount
 * @property totalIuranIndividu Total individual contribution amount
 * @property pengeluaranIuranWarga Total resident expenses
 * @property totalIuranRekap Total recapitulated contributions
 * @property pemanfaatanIuran Description of fund usage
 */
data class FinancialRecord(
    val id: Long = 0,
    val userId: Long,
    val iuranPerwarga: Int = 0,
    val jumlahIuranBulanan: Int = 0,
    val totalIuranIndividu: Int = 0,
    val pengeluaranIuranWarga: Int = 0,
    val totalIuranRekap: Int = 0,
    val pemanfaatanIuran: String
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
        require(iuranPerwarga <= MAX_NUMERIC_VALUE) { "Iuran perwarga exceeds max value" }
        require(jumlahIuranBulanan <= MAX_NUMERIC_VALUE) { "Jumlah iuran bulanan exceeds max value" }
        require(totalIuranIndividu <= MAX_NUMERIC_VALUE) { "Total iuran individu exceeds max value" }
        require(pengeluaranIuranWarga <= MAX_NUMERIC_VALUE) { "Pengeluaran iuran warga exceeds max value" }
        require(totalIuranRekap <= MAX_NUMERIC_VALUE) { "Total iuran rekap exceeds max value" }
        require(pemanfaatanIuran.isNotBlank()) { "Pemanfaatan iuran cannot be blank" }
        require(pemanfaatanIuran.length <= MAX_PEMANFAATAN_LENGTH) { "Pemanfaatan iuran too long" }
    }

    companion object {
        const val MAX_NUMERIC_VALUE = 999999999
        const val MAX_PEMANFAATAN_LENGTH = 500

        fun fromEntity(
            id: Long,
            userId: Long,
            iuranPerwarga: Int,
            jumlahIuranBulanan: Int,
            totalIuranIndividu: Int,
            pengeluaranIuranWarga: Int,
            totalIuranRekap: Int,
            pemanfaatanIuran: String
        ): FinancialRecord {
            return FinancialRecord(
                id = id,
                userId = userId,
                iuranPerwarga = iuranPerwarga,
                jumlahIuranBulanan = jumlahIuranBulanan,
                totalIuranIndividu = totalIuranIndividu,
                pengeluaranIuranWarga = pengeluaranIuranWarga,
                totalIuranRekap = totalIuranRekap,
                pemanfaatanIuran = pemanfaatanIuran
            )
        }
    }
}
