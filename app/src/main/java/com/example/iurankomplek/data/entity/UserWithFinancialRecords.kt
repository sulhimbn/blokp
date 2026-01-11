package com.example.iurankomplek.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithFinancialRecords(
    @Embedded
    val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val financialRecords: List<FinancialRecordEntity>
) {
    val latestFinancialRecord: FinancialRecordEntity?
        get() = financialRecords.maxByOrNull { it.updatedAt }
}
