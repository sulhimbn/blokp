package com.example.iurankomplek.data.cache

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity

object CacheHelper {

    suspend fun saveEntityWithFinancialRecords(
        userFinancialPairs: List<Pair<UserEntity, FinancialRecordEntity>>
    ) {
        val userDao = CacheManager.getUserDao()
        val financialRecordDao = CacheManager.getFinancialRecordDao()

        if (userFinancialPairs.isEmpty()) {
            return
        }

        val now = java.util.Date()

        val emails = userFinancialPairs.map { it.first.email }
        val existingUsers = userDao.getUsersByEmails(emails)
        val userMap = existingUsers.associateBy { it.email }

        val usersToInsert = mutableListOf<UserEntity>()
        val usersToUpdate = mutableListOf<UserEntity>()
        val userIdToFinancialMap = mutableMapOf<Long, FinancialRecordEntity>()

        userFinancialPairs.forEach { (user, financial) ->
            val existingUser = userMap[user.email]
            if (existingUser != null) {
                val userId = existingUser.id
                usersToUpdate.add(user.copy(id = userId, updatedAt = now))
                userIdToFinancialMap[userId] = financial
            } else {
                usersToInsert.add(user)
            }
        }

        if (usersToInsert.isNotEmpty()) {
            val insertedIds = userDao.insertAll(usersToInsert)
            usersToInsert.forEachIndexed { index, user ->
                userIdToFinancialMap[insertedIds[index]] = userFinancialPairs[index].second
            }
        }

        if (usersToUpdate.isNotEmpty()) {
            userDao.updateAll(usersToUpdate)
        }

        val userIds = userIdToFinancialMap.keys.toList()
        val existingFinancials = financialRecordDao.getFinancialRecordsByUserIds(userIds)
        val financialMap = existingFinancials.associateBy { it.userId }

        val financialsToInsert = mutableListOf<FinancialRecordEntity>()
        val financialsToUpdate = mutableListOf<FinancialRecordEntity>()

        userIdToFinancialMap.forEach { (userId, financial) ->
            val existingFinancial = financialMap[userId]
            if (existingFinancial != null) {
                financialsToUpdate.add(financial.copy(
                    id = existingFinancial.id,
                    userId = userId,
                    updatedAt = now
                ))
            } else {
                financialsToInsert.add(financial.copy(userId = userId, updatedAt = now))
            }
        }

        if (financialsToInsert.isNotEmpty()) {
            financialRecordDao.insertAll(financialsToInsert)
        }

        if (financialsToUpdate.isNotEmpty()) {
            financialRecordDao.updateAll(financialsToUpdate)
        }
    }
}
