package com.example.iurankomplek.data.validation

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.entity.EntityValidator
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import kotlinx.coroutines.flow.first

object DatabaseIntegrityValidator {

    suspend fun validateUserBeforeInsert(user: UserEntity): ValidationResult {
        val (isValid, error) = EntityValidator.validateUser(user)
        if (!isValid) {
            return ValidationResult(isValid = false, error = error ?: "Unknown validation error")
        }

        val userDao = CacheManager.getUserDao()
        val existingUser = userDao.getUserByEmail(user.email)
        if (existingUser != null) {
            return ValidationResult(
                isValid = false,
                error = "User with email '${user.email}' already exists"
            )
        }

        return ValidationResult(isValid = true, error = null)
    }

    suspend fun validateUserBeforeUpdate(user: UserEntity): ValidationResult {
        val (isValid, error) = EntityValidator.validateUser(user)
        if (!isValid) {
            return ValidationResult(isValid = false, error = error ?: "Unknown validation error")
        }

        val userDao = CacheManager.getUserDao()
        val existingUser = userDao.getUserByEmail(user.email)
        if (existingUser != null && existingUser.id != user.id) {
            return ValidationResult(
                isValid = false,
                error = "Email '${user.email}' is already used by another user"
            )
        }

        val originalUser = userDao.getUserById(user.id)
        if (originalUser == null) {
            return ValidationResult(
                isValid = false,
                error = "User with ID ${user.id} does not exist"
            )
        }

        return ValidationResult(isValid = true, error = null)
    }

    suspend fun validateFinancialRecordBeforeInsert(record: FinancialRecordEntity): ValidationResult {
        val (isValid, error) = EntityValidator.validateFinancialRecord(record)
        if (!isValid) {
            return ValidationResult(isValid = false, error = error ?: "Unknown validation error")
        }

        val userDao = CacheManager.getUserDao()
        val user = userDao.getUserById(record.userId)
        if (user == null) {
            return ValidationResult(
                isValid = false,
                error = "User with ID ${record.userId} does not exist"
            )
        }

        if (user.isDeleted) {
            return ValidationResult(
                isValid = false,
                error = "Cannot create financial record for deleted user with ID ${record.userId}"
            )
        }

        return ValidationResult(isValid = true, error = null)
    }

    suspend fun validateFinancialRecordBeforeUpdate(record: FinancialRecordEntity): ValidationResult {
        val (isValid, error) = EntityValidator.validateFinancialRecord(record)
        if (!isValid) {
            return ValidationResult(isValid = false, error = error ?: "Unknown validation error")
        }

        val userDao = CacheManager.getUserDao()
        val user = userDao.getUserById(record.userId)
        if (user == null) {
            return ValidationResult(
                isValid = false,
                error = "User with ID ${record.userId} does not exist"
            )
        }

        val financialDao = CacheManager.getFinancialRecordDao()
        val originalRecord = financialDao.getFinancialRecordById(record.id)
        if (originalRecord == null) {
            return ValidationResult(
                isValid = false,
                error = "Financial record with ID ${record.id} does not exist"
            )
        }

        if (user.isDeleted) {
            return ValidationResult(
                isValid = false,
                error = "Cannot update financial record for deleted user with ID ${record.userId}"
            )
        }

        if (originalRecord.userId != record.userId) {
            return ValidationResult(
                isValid = false,
                error = "Cannot change user ID of financial record"
            )
        }

        return ValidationResult(isValid = true, error = null)
    }

    suspend fun validateUserDelete(userId: Long): ValidationResult {
        val userDao = CacheManager.getUserDao()
        val user = userDao.getUserById(userId)
        if (user == null) {
            return ValidationResult(
                isValid = false,
                error = "User with ID $userId does not exist"
            )
        }

        if (user.isDeleted) {
            return ValidationResult(
                isValid = false,
                error = "User with ID $userId is already deleted"
            )
        }

        return ValidationResult(isValid = true, error = null)
    }

    suspend fun validateFinancialRecordDelete(recordId: Long): ValidationResult {
        val financialDao = CacheManager.getFinancialRecordDao()
        val record = financialDao.getFinancialRecordById(recordId)
        if (record == null) {
            return ValidationResult(
                isValid = false,
                error = "Financial record with ID $recordId does not exist"
            )
        }

        if (record.isDeleted) {
            return ValidationResult(
                isValid = false,
                error = "Financial record with ID $recordId is already deleted"
            )
        }

        return ValidationResult(isValid = true, error = null)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val error: String?
    )
}
