package com.imeanttobe.consensusapp.ui.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.domain.repo.IdRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val idRepo: IdRepo,
) : ViewModel() {
    private val _splashState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val splashState: StateFlow<UiState<Unit>> = _splashState

    private val _dialogState = MutableStateFlow<Boolean>(false)
    val dialogState: StateFlow<Boolean> = _dialogState

    init {
        checkAndGenerateId()
    }

    fun setDialogState(state: Boolean) {
        _dialogState.value = state
    }

    fun checkAndGenerateId() {
       viewModelScope.launch {
           _splashState.value = UiState.Loading
           Log.d("Consensus", "# checkAndGenerateId called.")

           try {
               val isExist = idRepo.isExist()

               if (isExist.isSuccess) {
                   if (isExist.getOrDefault(false)) {
                       Log.d("Consensus", "# ID exists.")
                       _splashState.value = UiState.Success(Unit)
                   } else {
                       Log.d("Consensus", "# ID does not exists.")
                       val id = UUID.randomUUID().toString()
                       val result = idRepo.setId(id)

                       if (result.isSuccess) {
                           Log.d("Consensus", "# Successfully created new id.")
                           _splashState.value = UiState.Success(Unit)
                       } else {
                           Log.e("Consensus", "# Failed to create new id.")
                           _splashState.value = UiState.Failure(result.exceptionOrNull()?.message ?: "새로 생성된 ID를 쓰던 중 오류 발생")
                       }
                   }
               } else {
                   Log.e("Consensus", "# Failed to check whether the id is exists.")
                   _splashState.value = UiState.Failure(isExist.exceptionOrNull()?.message ?: "ID를 확인하는 중 오류 발생")
               }
           } catch (e: Exception) {
               Log.e("Consensus", "# Failed to check while generating id.")
               _splashState.value = UiState.Failure(e.message ?: "ID를 설정하는 중 오류 발생")
           }
       }
    }
}