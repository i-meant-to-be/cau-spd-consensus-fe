package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response type to finish a poll.
 * @param votes List of encrypted votes.
 */
data class FinishPollResponse(
    @SerializedName("votes")
    val votes: List<String>
) {
    companion object {
        fun getFakeData(): FinishPollResponse {
            return FinishPollResponse(
                listOf(
                    "encrypted_vote_1",
                    "encrypted_vote_2",
                    "encrypted_vote_3"
                )
            )
        }
    }
}
