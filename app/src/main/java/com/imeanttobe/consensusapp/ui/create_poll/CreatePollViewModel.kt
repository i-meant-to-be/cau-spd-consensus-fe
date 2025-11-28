package com.imeanttobe.consensusapp.ui.create_poll

import androidx.lifecycle.ViewModel
import com.imeanttobe.consensusapp.core.Constants.MAX_OPTIONS
import com.imeanttobe.consensusapp.core.Constants.MAX_OPTION_LENGTH
import com.imeanttobe.consensusapp.core.Constants.MAX_PARTICIPANTS
import com.imeanttobe.consensusapp.core.Constants.MAX_TITLE_LENGTH
import com.imeanttobe.consensusapp.core.Constants.MIN_PARTICIPANTS
import com.imeanttobe.consensusapp.core.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CreatePollViewModel @Inject constructor() : ViewModel() {
    private val _title = MutableStateFlow<String>("")
    val title: StateFlow<String> = _title

    private val _options = MutableStateFlow<PersistentList<String>>(persistentListOf())
    val options: StateFlow<PersistentList<String>> = _options

    private val _newOption = MutableStateFlow<String>("")
    val newOption: StateFlow<String> = _newOption

    private val _numParticipants = MutableStateFlow<Int>(MIN_PARTICIPANTS)
    val numParticipants: StateFlow<Int> = _numParticipants

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    private val _dialogState = MutableStateFlow<Boolean>(false)
    val dialogState: StateFlow<Boolean> = _dialogState

    fun setDialogState(state: Boolean) {
        _dialogState.value = state
    }

    fun setNewOption(newOption: String) {
        _newOption.value = newOption.take(MAX_OPTION_LENGTH)

    }

    fun setTitle(title: String) {
        _title.value = title.take(MAX_TITLE_LENGTH)
    }

    fun setNumParticipants(numParticipants: Int) {
        _numParticipants.value = numParticipants.coerceIn(MIN_PARTICIPANTS, MAX_PARTICIPANTS)
    }

    fun appendOption(option: String) {
        if (option.isBlank()) {
            return
        }

        _options.update { currentList ->
            if (currentList.size < MAX_OPTIONS && !currentList.contains(option)) {
                currentList.add(option)
            } else {
                currentList
            }
        }
    }

    fun removeOption(index: Int) {
        _options.update { currentList ->
            if (index in currentList.indices) {
                currentList.removeAt(index)
            } else {
                currentList
            }
        }
    }

    fun removeOption(option: String) {
        _options.update { currentList ->
            currentList.remove(option)
        }
    }

    fun submit() {
        // TODO: Submit poll to server
    }
}