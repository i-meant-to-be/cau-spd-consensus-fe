package com.imeanttobe.consensusapp.data.remote.dto

import android.util.Base64
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.imeanttobe.consensusapp.seal.NativeLib

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
    val pk: String,

    @SerializedName("isDone")
    val isDone: Boolean
) {
    companion object {
        fun getFakeData(): GetPollResponse {
            val pk = NativeLib.generateKeys()?.pk
            val encodedPk =
                if (pk != null) {
                    Base64.encodeToString(pk, Base64.NO_WRAP)
                } else {
                    Log.e("SEAL", "PK is null")
                    ""
                }

            return GetPollResponse(
                title = "Title",
                id = 1,
                candidates = listOf("Candidate 1", "Candidate 2", "Candidate 3"),
                pk = encodedPk,
                isDone = false
            )
        }
    }
}
