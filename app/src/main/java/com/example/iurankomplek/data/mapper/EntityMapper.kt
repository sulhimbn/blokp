package com.example.iurankomplek.data.mapper

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import java.util.Date

object EntityMapper {
    
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
    
    fun toLegacyDto(userWithFinancial: UserWithFinancialRecords): LegacyDataItemDto {
        val financialRecord = userWithFinancial.latestFinancialRecord
            ?: throw IllegalStateException("User must have at least one financial record")
        
        return LegacyDataItemDto(
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
        )
    }
    
    fun fromLegacyDtoList(dtoList: List<LegacyDataItemDto>): List<Pair<UserEntity, FinancialRecordEntity>> {
        return dtoList.mapIndexed { index, dto ->
            fromLegacyDto(dto, userId = (index + 1).toLong())
        }
    }
    
    fun toLegacyDtoList(usersWithFinancials: List<UserWithFinancialRecords>): List<LegacyDataItemDto> {
        return usersWithFinancials.map { toLegacyDto(it) }
    }
}
