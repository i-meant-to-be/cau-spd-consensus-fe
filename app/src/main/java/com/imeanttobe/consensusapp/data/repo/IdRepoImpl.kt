package com.imeanttobe.consensusapp.data.repo

import androidx.datastore.core.DataStore
import com.imeanttobe.consensusapp.Id
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class IdRepoImpl @Inject constructor(
    private val idDataStore: DataStore<Id>
) : IdRepo {
    private val mutex = Mutex()
    @Volatile private var cachedId: String? = null

    override suspend fun getId(): Result<String> {
        try {
            val id = idDataStore.data.first().id
            if (id.isEmpty() || id.isBlank()) {
                return Result.failure(Exception("Id is empty or blank"))
            }
            mutex.withLock {
                cachedId = id
            }
            
            return Result.success(id)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun setId(id: String): Result<Boolean> {
        try {
            idDataStore.updateData {
                it.toBuilder().setId(id).build()
            }
            mutex.withLock {
                cachedId = id
            }

            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun isExist(): Result<Boolean> {
        try {
            val id = idDataStore.data.first().id
            val exists = id.isNotEmpty() && id.isNotBlank()
            mutex.withLock {
                cachedId = if (exists) id else null
            }

            return Result.success(exists)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun getCachedIdOrEmpty(): String {
        return cachedId ?: ""
    }
}