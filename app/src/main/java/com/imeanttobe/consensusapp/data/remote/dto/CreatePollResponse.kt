package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response type for creating a poll.
 * @property id The id of the poll.
 * @property title The title of the poll.
 * @property candidates The list of candidates for the poll.
 * @property codes The list of codes for the poll.
 * @property deepLink The deep link for the poll.
 */
data class CreatePollResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("candidates")
    val candidates: List<String>,

    @SerializedName("codes")
    val codes: List<String>,

    @SerializedName("deepLink")
    val deepLink: String
) {
    companion object {
        fun getFakeData(): CreatePollResponse {
            return CreatePollResponse(
                id = 1,
                title = "Test Poll",
                candidates = listOf(
                    "Candidate 1",
                    "Candidate 2",
                    "Candidate 3"
                ),
                codes = listOf("ABCD", "EFGH", "IJKL"),
                deepLink = "consensus://poll/1"
            )
        }
    }
}
