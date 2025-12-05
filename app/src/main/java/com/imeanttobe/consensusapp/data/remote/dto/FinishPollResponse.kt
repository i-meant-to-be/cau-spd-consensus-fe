package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response type to finish a poll.
 * @param votes List of encrypted votes.
 */
data class FinishPollResponse(
    @SerializedName("votes")
    val votes: List<String>
)
