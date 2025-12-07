package com.imeanttobe.consensusapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.imeanttobe.consensusapp.BuildConfig
import com.imeanttobe.consensusapp.Id
import com.imeanttobe.consensusapp.PollInfoItems
import com.imeanttobe.consensusapp.domain.repo.IdRepo
import com.imeanttobe.consensusapp.data.repo.IdRepoImpl
import com.imeanttobe.consensusapp.data.local.serializer.idDataStore
import com.imeanttobe.consensusapp.data.local.serializer.pollInfoItemsDataStore
import com.imeanttobe.consensusapp.data.repo.FakePollInfoItemsRepoImpl
import com.imeanttobe.consensusapp.data.repo.PollInfoItemsRepoImpl
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {
    companion object {
        @Provides
        @Singleton
        fun provideIdDataStore(@ApplicationContext context: Context): DataStore<Id> {
            return context.idDataStore
        }

        @Provides
        @Singleton
        fun providePollInfoItemsDataStore(@ApplicationContext context: Context): DataStore<PollInfoItems> {
            return context.pollInfoItemsDataStore
        }

        @Provides
        @Singleton
        fun providePollInfoItemsRepo(pollInfoItemsDataStore: DataStore<PollInfoItems>): PollInfoItemsRepo {
            return if (BuildConfig.IS_MOCK_ENABLED) {
                FakePollInfoItemsRepoImpl()
            } else {
                PollInfoItemsRepoImpl(pollInfoItemsDataStore)
            }
        }
    }

    @Binds
    @Singleton
    abstract fun bindIdRepo(idRepo: IdRepoImpl): IdRepo
}