package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response type for getting a poll data.
 * @property title The title of the poll.
 * @property id The ID of the poll.
 * @property candidates The list of candidates for the poll.
 * @property pk The public key of the poll.
 */
data class GetPollResponse(
    @SerializedName("title")
    val title: String,

    @SerializedName("id")
    val id: Int,

    @SerializedName("candidates")
    val candidates: List<String>,

    @SerializedName("pk")
    val pk: String
)
