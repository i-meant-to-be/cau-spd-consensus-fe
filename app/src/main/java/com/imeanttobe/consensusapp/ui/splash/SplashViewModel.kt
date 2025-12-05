package com.imeanttobe.consensusapp.ui.splash

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.domain.repo.IdRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val idRepo: IdRepo,
) : ViewModel() {
    private val _splashState = mutableStateOf<UiState<Unit>>(UiState.Idle)
    val splashState: State<UiState<Unit>> = _splashState

    private val _dialogState = mutableStateOf<Boolean>(false)
    val dialogState: State<Boolean> = _dialogState

    init {
        checkAndGenerateId()
    }

    fun setDialogState(state: Boolean) {
        _dialogState.value = state
    }

    fun checkAndGenerateId() {
       viewModelScope.launch {
           _splashState.value = UiState.Loading

           try {
               val isExist = idRepo.isExist()

               if (isExist.isSuccess) {
                   if (isExist.getOrDefault(false)) {
                       _splashState.value = UiState.Success(Unit)
                   } else {
                       val id = UUID.randomUUID().toString()
                       val result = idRepo.setId(id)

                       if (result.isSuccess) {
                           _splashState.value = UiState.Success(Unit)
                       } else {
                           _splashState.value = UiState.Failure(result.exceptionOrNull()?.message ?: "새로 생성된 ID를 쓰던 중 오류 발생")
                       }
                   }
               } else {
                   _splashState.value = UiState.Failure(isExist.exceptionOrNull()?.message ?: "ID를 확인하는 중 오류 발생")
               }
           } catch (e: Exception) {
               _splashState.value = UiState.Failure(e.message ?: "ID를 설정하는 중 오류 발생")
           }
       }
    }
}