package com.imeanttobe.consensusapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.imeanttobe.consensusapp.Id
import com.imeanttobe.consensusapp.data.repo.IdRepo
import com.imeanttobe.consensusapp.data.repo.IdRepoImpl
import com.imeanttobe.consensusapp.data.serializer.idDataStore
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
    }

    @Binds
    @Singleton
    abstract fun bindIdRepo(idRepoImpl: IdRepoImpl): IdRepo
}