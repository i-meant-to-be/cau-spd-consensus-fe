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
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    private val _recentPollItems = MutableStateFlow<List<PollUiModel>>(emptyList())
    val recentPollItems: StateFlow<List<PollUiModel>> = _recentPollItems

    init {
        loadPollUiModel()
    }

    fun loadPollUiModel() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            pollInfoItemsRepo.getAllPollInfo().onSuccess { items ->
                _recentPollItems.value = items.map { item -> PollUiModel(item.id, item.title) }
                _uiState.value = UiState.Success(Unit)
            }.onFailure {
                _recentPollItems.value = emptyList()
                _uiState.value = UiState.Failure(it.message ?: "Unknown error")
            }
        }
    }
}