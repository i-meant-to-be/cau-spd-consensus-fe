package com.imeanttobe.consensusapp.di

import com.imeanttobe.consensusapp.BuildConfig
import com.imeanttobe.consensusapp.data.interceptors.AuthInterceptor
import com.imeanttobe.consensusapp.data.interceptors.LoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val isHttpLoggingEnabled = BuildConfig.IS_HTTP_LOGGING_ENABLED

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

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}