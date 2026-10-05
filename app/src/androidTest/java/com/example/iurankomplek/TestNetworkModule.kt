package com.example.iurankomplek

import com.example.iurankomplek.di.NetworkModule
import com.example.iurankomplek.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object TestNetworkModule {

    @Provides
    @Singleton
    fun provideApiService(bound: BoundApiService): ApiService = bound.delegate
}
