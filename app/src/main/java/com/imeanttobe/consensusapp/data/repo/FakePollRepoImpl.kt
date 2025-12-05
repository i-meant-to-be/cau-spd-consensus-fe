package com.imeanttobe.consensusapp.data.repo

import com.imeanttobe.consensusapp.data.remote.dto.CreatePollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResultResponse
import com.imeanttobe.consensusapp.domain.repo.PollRepo

class FakePollRepoImpl : PollRepo {
    override suspend fun createPoll(
        title: String,
        candidates: List<String>
    ): Result<CreatePollResponse> {
        return Result.success(CreatePollResponse.getFakeData())
    }

    override suspend fun getPoll(id: Int): Result<GetPollResponse> {
        return Result.success(GetPollResponse.getFakeData())
    }

    override suspend fun vote(
        id: Int,
        vote: String,
        code: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun finishPoll(id: Int): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getPollResult(id: Int): Result<GetPollResultResponse> {
        return Result.success(GetPollResultResponse.getFakeData())
    }

    override suspend fun submitPollResult(
        id: Int,
        votes: List<Int>
    ): Result<Unit> {
        return Result.success(Unit)
    }
}