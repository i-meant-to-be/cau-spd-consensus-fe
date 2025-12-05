package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request type to submit poll result.
 * @param votes List of decrypted poll votes.
 */
data class SubmitPollResultRequest(
    @SerializedName("votes")
    val votes: List<Int>
)
