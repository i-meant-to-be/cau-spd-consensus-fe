package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response type for submitting a poll result.
 * @property id The ID of the poll.
 * @property candidates The list of candidate names.
 * @property votes The decrypted list of vote counts for each candidate.
 */
data class SubmitPollResultResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("candidates")
    val candidates: List<String>,

    @SerializedName("votes")
    val votes: List<Int>
)
