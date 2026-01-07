package com.example.iurankomplek.data.cache

import android.content.Context
import androidx.room.Room
import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object CacheManager {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Volatile
    private var database: AppDatabase? = null
    
    private var CACHE_FRESHNESS_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes
    
    fun initialize(context: Context) {
        if (database == null) {
            synchronized(this) {
                if (database == null) {
                    database = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "iuran_komplek_database"
                    )
                        .fallbackToDestructiveMigrationOnDowngrade()
                        .build()
                }
            }
        }
    }
    
    fun getDatabase(): AppDatabase {
        return database ?: throw IllegalStateException("CacheManager not initialized. Call initialize() first.")
    }
    
    fun getUserDao(): UserDao = getDatabase().userDao()
    
    fun getFinancialRecordDao(): FinancialRecordDao = getDatabase().financialRecordDao()
    
    suspend fun clearAllCaches() {
        getDatabase().userDao().deleteAll()
        getDatabase().financialRecordDao().deleteAll()
    }
    
    suspend fun isCacheFresh(lastUpdatedTimestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastUpdatedTimestamp) < CACHE_FRESHNESS_THRESHOLD_MS
    }
    
    fun setCacheFreshnessThreshold(thresholdMs: Long) {
        this.CACHE_FRESHNESS_THRESHOLD_MS = thresholdMs
    }
    
    fun getCacheFreshnessThreshold(): Long = CACHE_FRESHNESS_THRESHOLD_MS
}
