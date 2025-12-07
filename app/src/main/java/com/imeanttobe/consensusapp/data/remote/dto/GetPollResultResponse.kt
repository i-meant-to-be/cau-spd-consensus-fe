package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response type for getting poll results
 * @param title Poll title
 * @param id Poll id
 * @param candidates Poll candidates
 * @param votes Decrypted poll votes
 */
data class GetPollResultResponse(
    @SerializedName("title")
    val title: String,

    @SerializedName("id")
    val id: Int,

    @SerializedName("candidates")
    val candidates: List<String>,

    @SerializedName("votes")
    val votes: List<Int>
) {
    companion object {
        fun getFakeData(): GetPollResultResponse {
            return GetPollResultResponse(
                title = "Title",
                id = 1,
                candidates = listOf("Candidate 1", "Candidate 2", "Candidate 3"),
                votes = listOf(1, 2, 3)
            )
        }
    }
}
