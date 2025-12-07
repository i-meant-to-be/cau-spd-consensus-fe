package com.imeanttobe.consensusapp.domain.repo

import com.imeanttobe.consensusapp.PollInfo
import com.imeanttobe.consensusapp.domain.model.PollUiModel

interface PollInfoItemsRepo {
    suspend fun getAllPollInfo(): Result<List<PollInfo>>
    suspend fun getAllPollUiInfo(): Result<List<PollUiModel>>
    suspend fun getPollInfo(id: Int): Result<PollInfo>
    suspend fun addPollInfo(pollInfo: PollInfo): Result<Unit>
    suspend fun removePollInfo(id: Int): Result<Unit>
}
