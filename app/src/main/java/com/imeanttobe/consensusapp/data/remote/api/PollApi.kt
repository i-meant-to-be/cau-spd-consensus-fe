package com.imeanttobe.consensusapp.data.remote.api

import com.imeanttobe.consensusapp.data.remote.dto.CreatePollRequest
import com.imeanttobe.consensusapp.data.remote.dto.CreatePollResponse
import com.imeanttobe.consensusapp.data.remote.dto.FinishPollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResultResponse
import com.imeanttobe.consensusapp.data.remote.dto.SubmitPollResultRequest
import com.imeanttobe.consensusapp.data.remote.dto.SubmitPollResultResponse
import com.imeanttobe.consensusapp.data.remote.dto.VoteRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PollApi {
    @POST("api/polls")
    suspend fun createPoll(
        @Body request: CreatePollRequest
    ): Result<CreatePollResponse>

    @PATCH("api/polls/{id}/finish")
    suspend fun finishPoll(
        @Path("id") id: String
    ): Result<FinishPollResponse>

    @PATCH("api/polls/{id}/vote")
    suspend fun votePoll(
        @Path("id") id: String,
        @Body request: VoteRequest
    ): Result<Unit>

    @PATCH("api/polls/{id}/submit")
    suspend fun submitPollResult(
        @Path("id") id: String,
        @Body request: SubmitPollResultRequest
    ): Result<SubmitPollResultResponse>

    @GET("api/polls/{id}")
    suspend fun getPoll(
        @Path("id") id: String
    ): Result<GetPollResponse>

    @GET("api/polls/{id}/result")
    suspend fun getPollResult(
        @Path("id") id: String
    ): Result<GetPollResultResponse>
}