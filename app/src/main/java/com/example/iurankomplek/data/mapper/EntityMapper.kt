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
    
    fun toLegacyDto(userWithFinancial: UserWithFinancialRecords): LegacyDataItemDto {
        return LegacyDataItemDto(
                first_name = userWithFinancial.user.firstName,
                last_name = userWithFinancial.user.lastName,
                email = userWithFinancial.user.email,
                alamat = userWithFinancial.user.alamat,
                iuran_perwarga = userWithFinancial.latestFinancialRecord?.iuranPerwarga ?: 0,
                total_iuran_rekap = userWithFinancial.latestFinancialRecord?.totalIuranRekap ?: 0,
                jumlah_iuran_bulanan = userWithFinancial.latestFinancialRecord?.jumlahIuranBulanan ?: 0,
                total_iuran_individu = userWithFinancial.latestFinancialRecord?.totalIuranIndividu ?: 0,
                pengeluaran_iuran_warga = userWithFinancial.latestFinancialRecord?.pengeluaranIuranWarga ?: 0,
                pemanfaatan_iuran = userWithFinancial.latestFinancialRecord?.pemanfaatanIuran ?: "",
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
