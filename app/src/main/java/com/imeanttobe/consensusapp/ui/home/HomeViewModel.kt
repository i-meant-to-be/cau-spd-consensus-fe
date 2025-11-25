package com.imeanttobe.consensusapp.ui.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    // Temp code, will be going to be developed later
    private val _recentPollItems = MutableStateFlow<List<Int>>(listOf(1, 2, 3, 4, 5))
    val recentPollItems: StateFlow<List<Int>> = _recentPollItems
}