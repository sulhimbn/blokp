package com.example.iurankomplek.data.entity

data class UserWithFinancialRecords(
    val user: UserEntity,
    val financialRecords: List<FinancialRecordEntity>
) {
    val latestFinancialRecord: FinancialRecordEntity?
        get() = financialRecords.maxByOrNull { it.updatedAt }
}
