package com.imeanttobe.consensusapp.di

import com.imeanttobe.consensusapp.data.interceptors.AuthInterceptor
import com.imeanttobe.consensusapp.data.interceptors.LoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val isHttpLoggingEnabled = true

        return OkHttpClient.Builder()
            .apply {
                // Attach auth interceptor
                addInterceptor(authInterceptor)

                // Attach http logging interceptor when it is enabled
                if (isHttpLoggingEnabled) {
                    addInterceptor(LoggingInterceptor())
                }
            }
            .build()
    }
}