package com.imeanttobe.consensusapp.data.repo

import androidx.datastore.core.DataStore
import com.imeanttobe.consensusapp.Id
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IdRepoImpl @Inject constructor(
    private val idDataStore: DataStore<Id>
) : IdRepo {

    override suspend fun getId(): String {
        return idDataStore.data.first().id
    }

    override suspend fun setId(id: String) {
        idDataStore.updateData {
            it.toBuilder().setId(id).build()
        }
    }

    override suspend fun isExist(): Boolean {
        val id = idDataStore.data.first().id
        return id.isNotEmpty() && id.isNotBlank()
    }
}