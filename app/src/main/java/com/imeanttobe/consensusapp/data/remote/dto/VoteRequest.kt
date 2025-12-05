package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request type to send vote to server.
 * @param vote The encrypted value of the vote.
 * @param code The code for the vote.
 */
data class VoteRequest(
    @SerializedName("vote")
    val vote: String,

    @SerializedName("code")
    val code: String
)
