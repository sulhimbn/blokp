package com.example.iurankomplek.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.iurankomplek.data.constraints.DatabaseConstraints
import java.util.Date

@Entity(
    tableName = DatabaseConstraints.FinancialRecords.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [DatabaseConstraints.Users.Columns.ID],
            childColumns = [DatabaseConstraints.FinancialRecords.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [DatabaseConstraints.FinancialRecords.Columns.USER_ID, DatabaseConstraints.FinancialRecords.Columns.UPDATED_AT]),
        Index(value = [DatabaseConstraints.FinancialRecords.Columns.UPDATED_AT]),
        Index(value = [DatabaseConstraints.FinancialRecords.Columns.USER_ID, DatabaseConstraints.FinancialRecords.Columns.TOTAL_IURAN_REKAP])
    ]
)
data class FinancialRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.ID)
    val id: Long = 0,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.USER_ID)
    val userId: Long,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.IURAN_PERWARGA, defaultValue = "0")
    val iuranPerwarga: Int,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.JUMLAH_IURAN_BULANAN, defaultValue = "0")
    val jumlahIuranBulanan: Int,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.TOTAL_IURAN_INDIVIDU, defaultValue = "0")
    val totalIuranIndividu: Int,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.PENGELUARAN_IURAN_WARGA, defaultValue = "0")
    val pengeluaranIuranWarga: Int,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.TOTAL_IURAN_REKAP, defaultValue = "0")
    val totalIuranRekap: Int,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.PEMANFAATAN_IURAN)
    val pemanfaatanIuran: String,

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.CREATED_AT)
    val createdAt: Date = Date(),

    @ColumnInfo(name = DatabaseConstraints.FinancialRecords.Columns.UPDATED_AT)
    val updatedAt: Date = Date()
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
        require(iuranPerwarga <= DatabaseConstraints.FinancialRecords.Constraints.MAX_NUMERIC_VALUE) { "Iuran perwarga exceeds max value" }
        require(jumlahIuranBulanan <= DatabaseConstraints.FinancialRecords.Constraints.MAX_NUMERIC_VALUE) { "Jumlah iuran bulanan exceeds max value" }
        require(totalIuranIndividu <= DatabaseConstraints.FinancialRecords.Constraints.MAX_NUMERIC_VALUE) { "Total iuran individu exceeds max value" }
        require(pengeluaranIuranWarga <= DatabaseConstraints.FinancialRecords.Constraints.MAX_NUMERIC_VALUE) { "Pengeluaran iuran warga exceeds max value" }
        require(totalIuranRekap <= DatabaseConstraints.FinancialRecords.Constraints.MAX_NUMERIC_VALUE) { "Total iuran rekap exceeds max value" }
        require(pemanfaatanIuran.isNotBlank()) { "Pemanfaatan iuran cannot be blank" }
        require(pemanfaatanIuran.length <= DatabaseConstraints.FinancialRecords.Constraints.MAX_PEMANFAATAN_LENGTH) { "Pemanfaatan iuran too long" }
    }
}
