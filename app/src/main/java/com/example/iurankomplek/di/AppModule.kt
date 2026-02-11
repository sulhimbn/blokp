package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
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
    fun provideApiService(): ApiService {
        return ApiConfig.getApiService()
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        return UserRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}