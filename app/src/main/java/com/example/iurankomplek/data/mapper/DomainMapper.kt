package com.example.iurankomplek.data.mapper

import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.domain.model.FinancialRecord
import com.example.iurankomplek.domain.model.User

/**
 * Mapper for converting between domain models and data entities.
 * 
 * This mapper provides conversion between:
 * - UserEntity ↔ User (domain model)
 * - FinancialRecordEntity ↔ FinancialRecord (domain model)
 * 
 * Domain models are pure business entities without framework dependencies.
 * Entities are Room database entities with persistence annotations.
 */
object DomainMapper {

    fun toDomainModel(userEntity: UserEntity): User {
        return User(
            id = userEntity.id,
            email = userEntity.email,
            firstName = userEntity.firstName,
            lastName = userEntity.lastName,
            alamat = userEntity.alamat,
            avatar = userEntity.avatar
        )
    }

    fun toDomainModelList(userEntities: List<UserEntity>): List<User> {
        return userEntities.map { toDomainModel(it) }
    }

    fun fromDomainModel(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            alamat = user.alamat,
            avatar = user.avatar,
            createdAt = java.util.Date(),
            updatedAt = java.util.Date()
        )
    }

    fun fromDomainModelList(users: List<User>): List<UserEntity> {
        return users.map { fromDomainModel(it) }
    }

    fun toDomainModel(financialRecordEntity: FinancialRecordEntity): FinancialRecord {
        return FinancialRecord(
            id = financialRecordEntity.id,
            userId = financialRecordEntity.userId,
            iuranPerwarga = financialRecordEntity.iuranPerwarga,
            jumlahIuranBulanan = financialRecordEntity.jumlahIuranBulanan,
            totalIuranIndividu = financialRecordEntity.totalIuranIndividu,
            pengeluaranIuranWarga = financialRecordEntity.pengeluaranIuranWarga,
            totalIuranRekap = financialRecordEntity.totalIuranRekap,
            pemanfaatanIuran = financialRecordEntity.pemanfaatanIuran
        )
    }

    fun toDomainModelList(financialRecordEntities: List<FinancialRecordEntity>): List<FinancialRecord> {
        return financialRecordEntities.map { toDomainModel(it) }
    }

    fun fromDomainModel(financialRecord: FinancialRecord): FinancialRecordEntity {
        return FinancialRecordEntity(
            id = financialRecord.id,
            userId = financialRecord.userId,
            iuranPerwarga = financialRecord.iuranPerwarga,
            jumlahIuranBulanan = financialRecord.jumlahIuranBulanan,
            totalIuranIndividu = financialRecord.totalIuranIndividu,
            pengeluaranIuranWarga = financialRecord.pengeluaranIuranWarga,
            totalIuranRekap = financialRecord.totalIuranRekap,
            pemanfaatanIuran = financialRecord.pemanfaatanIuran,
            createdAt = java.util.Date(),
            updatedAt = java.util.Date()
        )
    }

    fun fromDomainModelList(financialRecords: List<FinancialRecord>): List<FinancialRecordEntity> {
        return financialRecords.map { fromDomainModel(it) }
    }
}
