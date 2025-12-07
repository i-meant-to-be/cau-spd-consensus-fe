package com.imeanttobe.consensusapp.domain.repo

import com.imeanttobe.consensusapp.data.remote.dto.CreatePollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResultResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollStatusResponse

interface PollRepo {
    suspend fun getPoll(id: Int): Result<GetPollResponse>
    suspend fun vote(id: Int, vote: String, code: String): Result<Unit>
    suspend fun finishPoll(id: Int): Result<Unit>
    suspend fun getPollResult(id: Int): Result<GetPollResultResponse>
    suspend fun submitPollResult(id: Int, votes: List<Int>): Result<Unit>
    // Public key will be given in actual impl
    suspend fun createPoll(title: String, candidates: List<String>): Result<CreatePollResponse>
    suspend fun getPollStatus(id: Int): Result<GetPollStatusResponse>
}