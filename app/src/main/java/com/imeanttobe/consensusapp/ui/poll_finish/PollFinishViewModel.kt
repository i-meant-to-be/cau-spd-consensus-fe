package com.imeanttobe.consensusapp.ui.poll_finish

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.core.crypto.CryptoManager
import com.imeanttobe.consensusapp.data.remote.dto.FinishPollResponse
import com.imeanttobe.consensusapp.data.remote.dto.SubmitPollResultResponse
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo
import com.imeanttobe.consensusapp.domain.repo.PollRepo
import com.imeanttobe.consensusapp.seal.NativeLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.annotation.Native
import javax.inject.Inject

@HiltViewModel
class PollFinishViewModel @Inject constructor(
    private val pollRepo: PollRepo,
    private val pollInfoItemsRepo: PollInfoItemsRepo,
    private val cryptoManager: CryptoManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _finishPollUiState = MutableStateFlow<UiState<FinishPollResponse>>(UiState.Idle)
    val finishPollUiState: StateFlow<UiState<FinishPollResponse>> = _finishPollUiState

    private val _submitPollResultUiState = MutableStateFlow<UiState<SubmitPollResultResponse>>(UiState.Idle)
    val submitPollResultUiState: StateFlow<UiState<SubmitPollResultResponse>> = _submitPollResultUiState

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    init {
        val id = savedStateHandle.get<Int>("id") ?: -1

        if (id != -1) {
            finishPoll(id = id)
        } else {
            _finishPollUiState.value = UiState.Failure("Invalid poll id")
        }
    }

    private fun finishPoll(id: Int) {
        _finishPollUiState.value = UiState.Loading

        viewModelScope.launch {
            val response = pollRepo.finishPoll(id = id)
            response.fold(
                onSuccess = { responseBody ->
                    _finishPollUiState.value = UiState.Success(responseBody)
                    submitPollResult(id = id, encryptedVotes = responseBody.votes)
                },
                onFailure = { _finishPollUiState.value = UiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }

    private fun submitPollResult(id: Int, encryptedVotes: List<String>) {
        _submitPollResultUiState.value = UiState.Loading

        viewModelScope.launch {
            // Get sk from repo
            val request = pollInfoItemsRepo.getPollInfo(id)
            if (request.isSuccess) {
                val pollInfo = request.getOrNull()
                if (pollInfo != null) {
                    // Decrypt sk
                    val decryptedSk = cryptoManager.decrypt(
                        ciphertext = pollInfo.encryptedSk.toByteArray(),
                        iv = pollInfo.iv.toByteArray()
                    )

                    // Decode Base64 votes
                    val decodedVotes = encryptedVotes.map { Base64.decode(it, Base64.NO_WRAP) }

                    // Sum all votes
                    var sum = decodedVotes.first()
                    for (i in 1 until decodedVotes.size) {
                        val addValue = NativeLib.addCiphertexts(sum, decodedVotes[i])
                        if (addValue != null) {
                            sum = addValue
                        } else {
                            _submitPollResultUiState.value = UiState.Failure("Failed to add votes")
                            return@launch
                        }
                    }

                    // Decrypt sum
                    val decryptedSum = NativeLib.decrypt(sum)
                    if (decryptedSum != null) {
                        // Send result to server
                        val result = pollRepo.submitPollResult(id = id, votes = decryptedSum.map { it.toInt() })
                        result.fold(
                            onSuccess = { responseBody ->
                                _submitPollResultUiState.value = UiState.Success(responseBody)
                                _statusMessage.value = "투표가 완료되었습니다."
                            },
                            onFailure = { _submitPollResultUiState.value = UiState.Failure(it.message ?: "Unknown error") }
                        )
                    } else {
                        _submitPollResultUiState.value = UiState.Failure("Failed to decrypt sum")
                        return@launch
                    }
                } else {
                    _submitPollResultUiState.value = UiState.Failure("Poll info not found")
                    return@launch
                }
            } else {
                _submitPollResultUiState.value = UiState.Failure(request.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}