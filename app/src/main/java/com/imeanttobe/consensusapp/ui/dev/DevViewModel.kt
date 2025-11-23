package com.imeanttobe.consensusapp.ui.dev

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.data.repo.IdRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevViewModel @Inject constructor(private val idRepo: IdRepo) : ViewModel() {
    private val _userId = mutableStateOf("")
    val userId: State<String> = _userId

    init {
        loadId()
    }

    private fun loadId() {
        viewModelScope.launch {
            _userId.value = idRepo.getId()
        }
    }

    fun setId(id: String) {
        viewModelScope.launch {
            idRepo.setId(id)
            _userId.value = id
        }
    }
}