package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.session.EncryptedSessionStore
import com.example.iurankomplek.session.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore =
        EncryptedSessionStore(context)
}