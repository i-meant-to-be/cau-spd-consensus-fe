package com.imeanttobe.consensusapp.data.repo

import androidx.datastore.core.DataStore
import com.imeanttobe.consensusapp.PollInfo
import com.imeanttobe.consensusapp.PollInfoItems
import com.imeanttobe.consensusapp.domain.model.PollUiModel
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

    override suspend fun getAllPollUiInfo(): Result<List<PollUiModel>> {
        try {
            val pollInfoItems = pollInfoItemsDataStore.data.first()
            val pollUiModels = pollInfoItems.itemsList.map {
                PollUiModel(it.id, it.title)
            }

            return Result.success(pollUiModels)
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

    override suspend fun addPollInfo(pollInfo: PollInfo): Result<Unit> {
        try {
            pollInfoItemsDataStore.updateData { current ->
                current.toBuilder()
                    .addItems(pollInfo)
                    .build()
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun removePollInfo(id: Int): Result<Unit> {
        try {
            pollInfoItemsDataStore.updateData { current ->
                val newPollInfoItems = PollInfoItems.newBuilder()
                current.itemsList.forEach {
                    if (it.id != id) {
                        newPollInfoItems.addItems(it)
                    }
                }
                newPollInfoItems.build()
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}