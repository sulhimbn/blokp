package com.example.iurankomplek.data.entity

import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords

object EntityValidator {
    
    /**
     * Validates a UserEntity
     * @return Pair of (isValid, errorMessage)
     */
    fun validateUser(user: UserEntity): Pair<Boolean, String?> {
        when {
            user.email.isBlank() -> return false to "Email cannot be blank"
            !isValidEmail(user.email) -> return false to "Email format is invalid"
            user.firstName.isBlank() -> return false to "First name cannot be blank"
            user.lastName.isBlank() -> return false to "Last name cannot be blank"
            user.alamat.isBlank() -> return false to "Alamat cannot be blank"
            user.avatar.isBlank() -> return false to "Avatar URL cannot be blank"
            user.firstName.length > 100 -> return false to "First name exceeds maximum length (100)"
            user.lastName.length > 100 -> return false to "Last name exceeds maximum length (100)"
            user.alamat.length > 500 -> return false to "Alamat exceeds maximum length (500)"
            user.email.length > 255 -> return false to "Email exceeds maximum length (255)"
            !isValidUrl(user.avatar) -> return false to "Avatar URL is invalid"
            else -> return true to null
        }
    }
    
    /**
     * Validates a FinancialRecordEntity
     * @return Pair of (isValid, errorMessage)
     */
    fun validateFinancialRecord(record: FinancialRecordEntity): Pair<Boolean, String?> {
        when {
            record.userId <= 0 -> return false to "User ID must be positive"
            record.iuranPerwarga < 0 -> return false to "Iuran perwarga cannot be negative"
            record.jumlahIuranBulanan < 0 -> return false to "Jumlah iuran bulanan cannot be negative"
            record.totalIuranIndividu < 0 -> return false to "Total iuran individu cannot be negative"
            record.pengeluaranIuranWarga < 0 -> return false to "Pengeluaran iuran warga cannot be negative"
            record.totalIuranRekap < 0 -> return false to "Total iuran rekap cannot be negative"
            record.pemanfaatanIuran.isBlank() -> return false to "Pemanfaatan iuran cannot be blank"
            record.pemanfaatanIuran.length > 500 -> return false to "Pemanfaatan iuran exceeds maximum length (500)"
            record.iuranPerwarga > 999999999 -> return false to "Iuran perwarga exceeds maximum value"
            record.jumlahIuranBulanan > 999999999 -> return false to "Jumlah iuran bulanan exceeds maximum value"
            record.totalIuranIndividu > 999999999 -> return false to "Total iuran individu exceeds maximum value"
            record.pengeluaranIuranWarga > 999999999 -> return false to "Pengeluaran iuran warga exceeds maximum value"
            record.totalIuranRekap > 999999999 -> return false to "Total iuran rekap exceeds maximum value"
            else -> return true to null
        }
    }
    
    /**
     * Validates a UserWithFinancialRecords
     * @return Pair of (isValid, errorMessage)
     */
    fun validateUserWithFinancials(userWithFinancials: UserWithFinancialRecords): Pair<Boolean, String?> {
        val (userValid, userError) = validateUser(userWithFinancials.user)
        if (!userValid) return false to userError
        
        if (userWithFinancials.financialRecords.isEmpty()) {
            return false to "User must have at least one financial record"
        }
        
        userWithFinancials.financialRecords.forEachIndexed { index, record ->
            val (recordValid, recordError) = validateFinancialRecord(record)
            if (!recordValid) {
                return false to "Financial record at index $index: $recordError"
            }
        }
        
        return true to null
    }
    
    /**
     * Validates that financial records belong to specified user
     * @return Pair of (isValid, errorMessage)
     */
    fun validateFinancialRecordOwnership(
        user: UserEntity,
        records: List<FinancialRecordEntity>
    ): Pair<Boolean, String?> {
        records.forEach { record ->
            if (record.userId != user.id) {
                return false to "Financial record ID ${record.id} does not belong to user ID ${user.id}"
            }
        }
        return true to null
    }
    
    /**
     * Validates email format using regex
     */
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailPattern.matches(email)
    }
    
    /**
     * Validates URL format
     */
    private fun isValidUrl(url: String): Boolean {
        val urlPattern = Regex("^https?://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+.*$")
        return urlPattern.matches(url)
    }
    
    /**
     * Validates a list of UserEntities
     * @return Pair of (isValid, errorMessages)
     */
    fun validateUserList(users: List<UserEntity>): Pair<Boolean, List<String>> {
        val errors = mutableListOf<String>()
        
        users.forEachIndexed { index, user ->
            val (isValid, error) = validateUser(user)
            if (!isValid) {
                errors.add("User at index $index: $error")
            }
        }
        
        return if (errors.isEmpty()) true to emptyList() else false to errors
    }
    
    /**
     * Validates a list of FinancialRecordEntities
     * @return Pair of (isValid, errorMessages)
     */
    fun validateFinancialRecordList(records: List<FinancialRecordEntity>): Pair<Boolean, List<String>> {
        val errors = mutableListOf<String>()
        
        records.forEachIndexed { index, record ->
            val (isValid, error) = validateFinancialRecord(record)
            if (!isValid) {
                errors.add("Record at index $index: $error")
            }
        }
        
        return if (errors.isEmpty()) true to emptyList() else false to errors
    }
}
