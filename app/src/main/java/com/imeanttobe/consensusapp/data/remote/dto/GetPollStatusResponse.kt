package com.imeanttobe.consensusapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response type for getting poll results
 * @param title Poll title
 * @param id Poll id
 * @param candidates Poll candidates
 * @param votes Decrypted poll votes
 */
data class GetPollStatusResponse(
    @SerializedName("title")
    val title: String,

    @SerializedName("id")
    val id: Int,

    @SerializedName("candidates")
    val candidates: List<String>,

    @SerializedName("codes")
    val codes: List<String>,

    @SerializedName("usedCodes")
    val usedCodes: List<String>
) {
    companion object {
        fun getFakeData(): GetPollStatusResponse {
            return GetPollStatusResponse(
                title = "Title",
                id = 1,
                candidates = listOf("Candidate 1", "Candidate 2", "Candidate 3"),
                codes = listOf("ABCD1234", "EFGH5678", "IJKL9012"),
                usedCodes = listOf("ABCD1234", "EFGH5678")
            )
        }
    }
}
