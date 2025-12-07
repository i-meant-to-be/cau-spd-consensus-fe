package com.imeanttobe.consensusapp.ui.host_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.data.remote.dto.GetPollStatusResponse
import com.imeanttobe.consensusapp.domain.repo.PollRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostDashboardViewModel @Inject constructor(
    private val pollRepo: PollRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _pollStatusUiState = MutableStateFlow<UiState<GetPollStatusResponse>>(UiState.Idle)
    val pollStatusUiState: StateFlow<UiState<GetPollStatusResponse>> = _pollStatusUiState

    private val _finishPollUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val finishPollUiState: StateFlow<UiState<Unit>> = _finishPollUiState

    private val _finishStatusMessage = MutableStateFlow("")
    val finishStatusMessage: StateFlow<String> = _finishStatusMessage

    private val _dialogState = MutableStateFlow(false)
    val dialogState: StateFlow<Boolean> = _dialogState

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    init {
        val id = savedStateHandle.get<Int>("id") ?: -1
        loadPollStatus(id)
    }

    fun resetFinishPollUiState() {
        _finishPollUiState.value = UiState.Idle
    }

    fun setDialogState(state: Boolean) {
        _dialogState.value = state
    }

    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    fun finishPoll() {
        _finishPollUiState.value = UiState.Loading

        viewModelScope.launch {
            val responseBody = pollStatusUiState.value
            if (responseBody !is UiState.Success) {
                _finishPollUiState.value = UiState.Failure("Poll data is not loaded")
                return@launch
            }

            val result = pollRepo.finishPoll(responseBody.data.id)
            result.fold(
                onSuccess = { _finishPollUiState.value = UiState.Success(Unit) },
                onFailure = { _finishPollUiState.value = UiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }

    fun loadPollStatus(id: Int) {
        _pollStatusUiState.value = UiState.Loading

        viewModelScope.launch {
            val result = pollRepo.getPollStatus(id)

            result.fold(
                onSuccess = {
                    _pollStatusUiState.value = UiState.Success(it)
                    _progress.value = it.usedCodes.size.toFloat() / it.codes.size.toFloat()
                },
                onFailure = { _pollStatusUiState.value = UiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }
}