package com.imeanttobe.consensusapp.ui.vote

import android.util.Base64
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResponse
import com.imeanttobe.consensusapp.domain.repo.PollRepo
import com.imeanttobe.consensusapp.seal.NativeLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val pollRepo: PollRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _pollResponseUiState = MutableStateFlow<UiState<GetPollResponse>>(UiState.Idle)
    val pollResponseUiState: StateFlow<UiState<GetPollResponse>> = _pollResponseUiState

    private val _voteRequestUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val voteRequestUiState: StateFlow<UiState<Unit>> = _voteRequestUiState

    private val _voteCode = MutableStateFlow("")
    val voteCode: StateFlow<String> = _voteCode

    private val _voteStatusMessage = MutableStateFlow("")
    val voteStatusMessage: StateFlow<String> = _voteStatusMessage

    private val _dialogState = MutableStateFlow(false)
    val dialogState: StateFlow<Boolean> = _dialogState

    private val _selectedCandidate = MutableStateFlow("")
    val selectedCandidate: StateFlow<String> = _selectedCandidate

    init {
        val id = savedStateHandle.get<Int>("id") ?: -1
        loadPollData(id)
    }

    fun setDialogState(state: Boolean) {
        _dialogState.value = state
    }

    fun setVoteCode(code: String) {
        _voteCode.value = code
    }

    fun setSelectedCandidate(candidate: String) {
        _selectedCandidate.value = candidate
    }

    fun vote() {
        viewModelScope.launch {
            _voteRequestUiState.value = UiState.Loading

            // Get data
            _voteStatusMessage.value = "데이터 준비 중..."
            val pollState = pollResponseUiState.value
            if (pollState !is UiState.Success) {
                _voteRequestUiState.value = UiState.Failure("Poll data is not loaded")
                return@launch
            }
            val poll = pollState.data

            // Prepare vector
            val plaintext = LongArray(poll.candidates.size) { 0 }
            poll.candidates.forEachIndexed { index, candidate ->
                if (candidate == selectedCandidate.value) {
                    plaintext[index] = 1
                }
            }

            // Encrypt vector and encode with base64
            _voteStatusMessage.value = "투표 값 암호화 중..."
            val pk = Base64.decode(poll.pk, Base64.NO_WRAP)
            val ciphertext = withContext(Dispatchers.IO) {
                NativeLib.encrypt(plaintext, pk)
            }
            if (ciphertext == null) {
                _voteRequestUiState.value = UiState.Failure("Encryption failed")
                return@launch
            }
            val encodedCiphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

            // Send it
            _voteStatusMessage.value = "투표 값 서버에 전송 중..."
            val result = pollRepo.vote(id = poll.id, code = voteCode.value, vote = encodedCiphertext)
            if (result.isSuccess) {
                _voteRequestUiState.value = UiState.Success(Unit)
            } else {
                _voteRequestUiState.value = UiState.Failure(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    private fun loadPollData(id: Int) {
        viewModelScope.launch {
            _pollResponseUiState.value = UiState.Loading

            delay(500)

            val result = pollRepo.getPoll(id)
            if (result.isSuccess) {
                val response = result.getOrNull()

                if (response != null) {
                    val firstCandidate = response.candidates.firstOrNull()
                    if (firstCandidate != null) {
                        _selectedCandidate.value = firstCandidate
                        _pollResponseUiState.value = UiState.Success(response)
                    } else {
                        _pollResponseUiState.value = UiState.Failure("No candidates found")
                    }
                } else {
                    _pollResponseUiState.value = UiState.Failure("Response is null")
                }
            } else {
                _pollResponseUiState.value = UiState.Failure(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}