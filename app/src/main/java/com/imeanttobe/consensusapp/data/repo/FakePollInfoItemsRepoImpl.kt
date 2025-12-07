package com.imeanttobe.consensusapp.data.repo

import com.imeanttobe.consensusapp.PollInfo
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo

class FakePollInfoItemsRepoImpl : PollInfoItemsRepo {
    override suspend fun getAllPollInfo(): Result<List<PollInfo>> {
        val fakePollItem = PollInfo.newBuilder()
            .setId(1)
            .setTitle("Fake Poll")
            .build()
        return Result.success(listOf(fakePollItem, fakePollItem, fakePollItem))
    }

    override suspend fun getPollInfo(id: Int): Result<PollInfo> {
        return Result.success(PollInfo.getDefaultInstance())
    }

    override suspend fun addPollInfo(pollInfo: PollInfo): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun removePollInfo(id: Int): Result<Unit> {
        return Result.success(Unit)
    }
}