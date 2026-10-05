package com.example.iurankomplek.di

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiConfig.getApiService()
}