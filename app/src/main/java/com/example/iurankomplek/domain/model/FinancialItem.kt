package com.example.iurankomplek.domain.model

/**
 * Domain model representing financial item data for calculations.
 * 
 * This is a pure domain model independent of data layer DTOs.
 * Used by use cases for financial calculations and validation.
 * 
 * @property iuranPerwarga Monthly contribution per resident
 * @property pengeluaranIuranWarga Total resident expenses
 * @property totalIuranIndividu Total individual contribution amount
 */
data class FinancialItem(
    val iuranPerwarga: Int = 0,
    val pengeluaranIuranWarga: Int = 0,
    val totalIuranIndividu: Int = 0
) {
    init {
        validate()
    }

    private fun validate() {
        require(iuranPerwarga >= 0) { "iuranPerwarga cannot be negative" }
        require(pengeluaranIuranWarga >= 0) { "pengeluaranIuranWarga cannot be negative" }
        require(totalIuranIndividu >= 0) { "totalIuranIndividu cannot be negative" }
        require(iuranPerwarga <= MAX_NUMERIC_VALUE) { "iuranPerwarga exceeds max value" }
        require(pengeluaranIuranWarga <= MAX_NUMERIC_VALUE) { "pengeluaranIuranWarga exceeds max value" }
        require(totalIuranIndividu <= MAX_NUMERIC_VALUE) { "totalIuranIndividu exceeds max value" }
    }

    companion object {
        const val MAX_NUMERIC_VALUE = 999999999

        fun fromLegacyDataItemDto(dto: com.example.iurankomplek.data.dto.LegacyDataItemDto): FinancialItem {
            return FinancialItem(
                iuranPerwarga = dto.iuran_perwarga,
                pengeluaranIuranWarga = dto.pengeluaran_iuran_warga,
                totalIuranIndividu = dto.total_iuran_individu
            )
        }

        fun fromLegacyDataItemDtoList(dtos: List<com.example.iurankomplek.data.dto.LegacyDataItemDto>): List<FinancialItem> {
            return dtos.map { fromLegacyDataItemDto(it) }
        }
    }
}
