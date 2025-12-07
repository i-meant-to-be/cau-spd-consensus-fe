package com.imeanttobe.consensusapp.di

import com.imeanttobe.consensusapp.BuildConfig
import com.imeanttobe.consensusapp.data.remote.api.PollApi
import com.imeanttobe.consensusapp.data.remote.interceptor.AuthInterceptor
import com.imeanttobe.consensusapp.data.remote.interceptor.LoggingInterceptor
import com.imeanttobe.consensusapp.data.repo.FakePollRepoImpl
import com.imeanttobe.consensusapp.data.repo.PollRepoImpl
import com.imeanttobe.consensusapp.domain.repo.PollRepo
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

    @Provides
    @Singleton
    fun providePollApi(retrofit: Retrofit): PollApi {
        return retrofit.create(PollApi::class.java)
    }

    @Provides
    @Singleton
    fun providePollRepo(pollApi: PollApi): PollRepo {
        return if (BuildConfig.IS_MOCK_ENABLED) {
            FakePollRepoImpl()
        } else {
            PollRepoImpl(pollApi)
        }
    }
}