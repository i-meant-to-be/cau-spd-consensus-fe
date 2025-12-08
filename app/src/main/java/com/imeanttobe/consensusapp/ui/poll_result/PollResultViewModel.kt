package com.imeanttobe.consensusapp.ui.poll_result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResultResponse
import com.imeanttobe.consensusapp.domain.repo.PollRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PollResultViewModel @Inject constructor(
    private val pollRepo: PollRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<GetPollResultResponse>>(UiState.Idle)
    val uiState: StateFlow<UiState<GetPollResultResponse>> = _uiState

    init {
        val id = savedStateHandle.get<Int>("id")
        if (id != null) {
            loadPollResult(id)
        } else {
            _uiState.value = UiState.Failure("Invalid ID")
        }
    }

    private fun loadPollResult(id: Int) {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            val response = pollRepo.getPollResult(id)
            response.fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }
}