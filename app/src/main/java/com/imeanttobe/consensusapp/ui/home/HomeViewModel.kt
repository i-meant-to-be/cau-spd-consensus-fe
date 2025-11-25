package com.imeanttobe.consensusapp.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _dialogState = mutableStateOf(false)
    val dialogState: State<Boolean> = _dialogState

    // Temp code, will be going to be developed later
    private val _recentPollItems = mutableStateListOf(1, 2, 3, 4, 5)
    val recentPollItems: List<Int> = _recentPollItems

    fun setDialogState(value: Boolean) {
        _dialogState.value = value
    }
}