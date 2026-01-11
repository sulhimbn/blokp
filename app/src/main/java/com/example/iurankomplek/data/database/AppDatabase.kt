package com.example.iurankomplek.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.iurankomplek.data.DataTypeConverters
import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.payment.WebhookEvent
import com.example.iurankomplek.payment.WebhookEventDao
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [UserEntity::class, FinancialRecordEntity::class, Transaction::class, WebhookEvent::class],
    version = 20,
    exportSchema = true
)
@TypeConverters(DataTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun financialRecordDao(): FinancialRecordDao
    abstract fun transactionDao(): TransactionDao
    abstract fun webhookEventDao(): WebhookEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val migrations = arrayOf(
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4, Migration4Down,
            Migration5, Migration5Down, Migration6, Migration6Down,
            Migration7, Migration7Down, Migration8, Migration8Down,
            Migration9, Migration9Down, Migration10, Migration10Down,
            Migration11(), Migration11Down, Migration12(), Migration12Down,
            Migration13(), Migration13Down, Migration14(), Migration14Down,
            Migration15(), Migration15Down, Migration16(), Migration16Down,
            Migration17(), Migration17Down, Migration18(), Migration18Down,
            Migration19(), Migration19Down, Migration20(), Migration20Down
        )

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iuran_komplek_database"
                )
                    .addMigrations(*migrations)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
