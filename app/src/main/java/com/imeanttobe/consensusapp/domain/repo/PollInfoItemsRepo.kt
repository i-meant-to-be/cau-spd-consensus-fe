package com.imeanttobe.consensusapp.domain.repo

import com.imeanttobe.consensusapp.PollInfo

interface PollInfoItemsRepo {
    suspend fun getAllPollInfo(): Result<List<PollInfo>>
    suspend fun getPollInfo(id: Int): Result<PollInfo>
    suspend fun addPollInfo(keyBundle: PollInfo): Result<Boolean>
    suspend fun removePollInfo(id: Int): Result<Boolean>
}
