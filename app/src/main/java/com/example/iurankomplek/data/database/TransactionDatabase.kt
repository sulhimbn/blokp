package com.example.iurankomplek.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.DataTypeConverters
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus

@Database(
    entities = [Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DataTypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
