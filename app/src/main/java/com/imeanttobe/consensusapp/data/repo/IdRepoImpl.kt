package com.imeanttobe.consensusapp.data.repo

import androidx.datastore.core.DataStore
import com.imeanttobe.consensusapp.Id
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IdRepoImpl @Inject constructor(
    private val idDataStore: DataStore<Id>
) : IdRepo {

    override suspend fun getId(): Result<String> {
        try {
            val id = idDataStore.data.first().id
            if (id.isEmpty() || id.isBlank()) {
                return Result.failure(Exception("Id is empty or blank"))
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
            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun isExist(): Result<Boolean> {
        try {
            val id = idDataStore.data.first().id
            return Result.success(id.isNotEmpty() && id.isNotBlank())
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}