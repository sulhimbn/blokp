package com.example.iurankomplek.di

import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserRepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        sessionManager: UserSessionManager
    ): UserRepository {
        return UserRepositoryImpl(apiService, sessionManager)
    }
}
