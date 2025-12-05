package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request type to create a new poll.
 * @param title The title of the poll.
 * @param pk The public key of the poll.
 * @param candidates The list of candidates for the poll.
 */
data class CreatePollRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("pk")
    val pk: String,

    @SerializedName("candidates")
    val candidates: List<String>
)