package com.imeanttobe.consensusapp.data.repo

import androidx.datastore.core.DataStore
import com.imeanttobe.consensusapp.PollInfo
import com.imeanttobe.consensusapp.PollInfoItems
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PollInfoItemsRepoImpl @Inject constructor(
    private val pollInfoItemsDataStore: DataStore<PollInfoItems>
) : PollInfoItemsRepo {
    override suspend fun getAllPollInfo(): Result<List<PollInfo>> {
        try {
            val pollInfoItems = pollInfoItemsDataStore.data.first()

            return Result.success(pollInfoItems.itemsList)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun getPollInfo(id: Int): Result<PollInfo> {
        try {
            val pollInfoItems = pollInfoItemsDataStore.data.first()
            val pollInfo = pollInfoItems.itemsList.find { it.id == id }

            return if (pollInfo != null) {
                Result.success(pollInfo)
            } else {
                Result.failure(Exception("PollInfo not found"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun addPollInfo(keyBundle: PollInfo): Result<Boolean> {
        try {
            val pollInfoItems = pollInfoItemsDataStore.data.first()
            val newPollInfoItems = pollInfoItems.toBuilder()
                .addItems(keyBundle)
                .build()

            pollInfoItemsDataStore.updateData {
                newPollInfoItems
            }

            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun removePollInfo(id: Int): Result<Boolean> {
        try {
            val pollInfoItems = pollInfoItemsDataStore.data.first()
            val newPollInfoItems = PollInfoItems.newBuilder()


            pollInfoItems.itemsList.forEach {
                if (it.id != id) {
                    newPollInfoItems.addItems(it)
                }
            }
            pollInfoItemsDataStore.updateData {
                newPollInfoItems.build()
            }

            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}