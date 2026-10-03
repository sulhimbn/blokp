package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTransactionDatabase(@ApplicationContext context: Context): TransactionDatabase =
        TransactionDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideTransactionDao(database: TransactionDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}