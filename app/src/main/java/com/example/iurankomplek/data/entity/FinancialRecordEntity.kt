package com.example.iurankomplek.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.iurankomplek.data.constraints.FinancialRecordConstraints
import com.example.iurankomplek.data.constraints.UserConstraints
import java.util.Date

@Entity(
    tableName = "financial_records",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "updated_at"]),
        Index(value = ["user_id", "total_iuran_rekap"])
    ]
)
data class FinancialRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "iuran_perwarga", defaultValue = "0")
    val iuranPerwarga: Int,

    @ColumnInfo(name = "jumlah_iuran_bulanan", defaultValue = "0")
    val jumlahIuranBulanan: Int,

    @ColumnInfo(name = "total_iuran_individu", defaultValue = "0")
    val totalIuranIndividu: Int,

    @ColumnInfo(name = "pengeluaran_iuran_warga", defaultValue = "0")
    val pengeluaranIuranWarga: Int,

    @ColumnInfo(name = "total_iuran_rekap", defaultValue = "0")
    val totalIuranRekap: Int,

    @ColumnInfo(name = "pemanfaatan_iuran")
    val pemanfaatanIuran: String,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
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
        require(iuranPerwarga <= FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE) { "Iuran perwarga exceeds max value" }
        require(jumlahIuranBulanan <= FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE) { "Jumlah iuran bulanan exceeds max value" }
        require(totalIuranIndividu <= FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE) { "Total iuran individu exceeds max value" }
        require(pengeluaranIuranWarga <= FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE) { "Pengeluaran iuran warga exceeds max value" }
        require(totalIuranRekap <= FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE) { "Total iuran rekap exceeds max value" }
        require(pemanfaatanIuran.length <= FinancialRecordConstraints.Constraints.MAX_PEMANFAATAN_LENGTH) { "Pemanfaatan iuran too long" }
    }
}
