package com.example.iurankomplek.data.mapper

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.model.DataItem
import java.util.Date

object EntityMapper {
    
    fun toDataItem(dto: LegacyDataItemDto): DataItem {
        return DataItem(
            first_name = dto.first_name,
            last_name = dto.last_name,
            email = dto.email,
            alamat = dto.alamat,
            iuran_perwarga = dto.iuran_perwarga,
            total_iuran_rekap = dto.total_iuran_rekap,
            jumlah_iuran_bulanan = dto.jumlah_iuran_bulanan,
            total_iuran_individu = dto.total_iuran_individu,
            pengeluaran_iuran_warga = dto.pengeluaran_iuran_warga,
            pemanfaatan_iuran = dto.pemanfaatan_iuran,
            avatar = dto.avatar
        )
    }
    
    fun toDataItemList(dtoList: List<LegacyDataItemDto>): List<DataItem> {
        return dtoList.map { toDataItem(it) }
    }
    
    fun fromLegacyDto(dto: LegacyDataItemDto, userId: Long = 0): Pair<UserEntity, FinancialRecordEntity> {
        val userEntity = UserEntity(
            id = userId,
            email = dto.email,
            firstName = dto.first_name,
            lastName = dto.last_name,
            alamat = dto.alamat,
            avatar = dto.avatar,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val financialRecordEntity = FinancialRecordEntity(
            id = 0,
            userId = userId,
            iuranPerwarga = dto.iuran_perwarga,
            jumlahIuranBulanan = dto.jumlah_iuran_bulanan,
            totalIuranIndividu = dto.total_iuran_individu,
            pengeluaranIuranWarga = dto.pengeluaran_iuran_warga,
            totalIuranRekap = dto.total_iuran_rekap,
            pemanfaatanIuran = dto.pemanfaatan_iuran,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        return Pair(userEntity, financialRecordEntity)
    }
    
    fun toLegacyDto(userWithFinancial: UserWithFinancialRecords): Result<LegacyDataItemDto> {
        return try {
            val financialRecord = userWithFinancial.latestFinancialRecord
                ?: return Result.failure(IllegalStateException("User must have at least one financial record"))

            Result.success(LegacyDataItemDto(
                first_name = userWithFinancial.user.firstName,
                last_name = userWithFinancial.user.lastName,
                email = userWithFinancial.user.email,
                alamat = userWithFinancial.user.alamat,
                iuran_perwarga = financialRecord.iuranPerwarga,
                total_iuran_rekap = financialRecord.totalIuranRekap,
                jumlah_iuran_bulanan = financialRecord.jumlahIuranBulanan,
                total_iuran_individu = financialRecord.totalIuranIndividu,
                pengeluaran_iuran_warga = financialRecord.pengeluaranIuranWarga,
                pemanfaatan_iuran = financialRecord.pemanfaatanIuran,
                avatar = userWithFinancial.user.avatar
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun fromLegacyDtoList(dtoList: List<LegacyDataItemDto>): List<Pair<UserEntity, FinancialRecordEntity>> {
        return dtoList.mapIndexed { index, dto ->
            fromLegacyDto(dto, userId = (index + 1).toLong())
        }
    }
    
    fun toLegacyDtoList(usersWithFinancials: List<UserWithFinancialRecords>): Result<List<LegacyDataItemDto>> {
        return try {
            val results = usersWithFinancials.map { toLegacyDto(it) }
            val failures = results.filter { it.isFailure }
            if (failures.isNotEmpty()) {
                Result.failure(failures.first().exceptionOrNull() ?: Exception("Mapping failed"))
            } else {
                Result.success(results.map { it.getOrThrow() })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
