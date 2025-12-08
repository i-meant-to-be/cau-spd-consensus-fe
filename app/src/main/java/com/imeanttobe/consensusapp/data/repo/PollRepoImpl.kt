package com.imeanttobe.consensusapp.data.repo

import com.imeanttobe.consensusapp.data.remote.api.PollApi
import com.imeanttobe.consensusapp.data.remote.dto.CreatePollRequest
import com.imeanttobe.consensusapp.data.remote.dto.CreatePollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResultResponse
import com.imeanttobe.consensusapp.data.remote.dto.GetPollStatusResponse
import com.imeanttobe.consensusapp.data.remote.dto.SubmitPollResultRequest
import com.imeanttobe.consensusapp.data.remote.dto.VoteRequest
import com.imeanttobe.consensusapp.domain.repo.PollRepo
import javax.inject.Inject

class PollRepoImpl @Inject constructor(
    private val pollApi: PollApi
): PollRepo {
    override suspend fun getPoll(id: Int): Result<GetPollResponse> {
        return try {
            val response = pollApi.getPoll(id)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun vote(id: Int, vote: String, code: String): Result<Unit> {
        return try {
            val request = VoteRequest(vote, code)
            val response = pollApi.votePoll(id, request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPollResult(id: Int): Result<GetPollResultResponse> {
        return try {
            val response = pollApi.getPollResult(id)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitPollResult(id: Int, votes: List<Int>): Result<Unit> {
        return try {
            val request = SubmitPollResultRequest(votes)
            val response = pollApi.submitPollResult(id, request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPoll(
        title: String,
        pk: String,
        candidates: List<String>
    ): Result<CreatePollResponse> {
        return try {
            val request = CreatePollRequest(title, pk, candidates)
            val response = pollApi.createPoll(request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPollStatus(id: Int): Result<GetPollStatusResponse> {
        return try {
            val response = pollApi.getPollStatus(id)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun finishPoll(id: Int): Result<Unit> {
        return try {
            val response = pollApi.finishPoll(id)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}