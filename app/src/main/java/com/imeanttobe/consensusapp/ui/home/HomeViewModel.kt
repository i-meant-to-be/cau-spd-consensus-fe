package com.imeanttobe.consensusapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.PollInfo
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo
import com.imeanttobe.consensusapp.pollInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pollInfoItemsRepo: PollInfoItemsRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    private val _recentPollItems = MutableStateFlow<List<PollInfo>>(emptyList())
    val recentPollItems: StateFlow<List<PollInfo>> = _recentPollItems

    init {
        loadPollUiModel()
    }

    fun loadPollUiModel() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            pollInfoItemsRepo.getAllPollInfo().onSuccess {
                _recentPollItems.value = it
                _uiState.value = UiState.Success(Unit)
            }.onFailure {
                _recentPollItems.value = emptyList()
                _uiState.value = UiState.Failure(it.message ?: "Unknown error")
            }
        }
    }
}