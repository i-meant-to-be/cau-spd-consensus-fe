package com.imeanttobe.consensusapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.domain.model.PollUiModel
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pollInfoItemsRepo: PollInfoItemsRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<PollUiModel>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<PollUiModel>>> = _uiState

    init {
        loadPollUiModel()
    }

    fun loadPollUiModel() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            pollInfoItemsRepo.getAllPollInfo()
                .map { items -> items.map { PollUiModel(it.id, it.title) }}
                .fold(
                    onSuccess = { _uiState.value = UiState.Success(it) },
                    onFailure = { _uiState.value = UiState.Failure(it.message ?: "Unknown error") }
                )
        }
    }
}