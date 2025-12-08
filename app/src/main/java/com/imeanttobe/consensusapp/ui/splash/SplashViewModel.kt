package com.imeanttobe.consensusapp.ui.splash

import android.util.Base64
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.domain.repo.IdRepo
import com.imeanttobe.consensusapp.seal.NativeLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val idRepo: IdRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _splashState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val splashState: StateFlow<UiState<Unit>> = _splashState

    private val _dialogState = MutableStateFlow<Boolean>(false)
    val dialogState: StateFlow<Boolean> = _dialogState

    private val _loadingMessage = MutableStateFlow<String>("대기")
    val loadingMessage: StateFlow<String> = _loadingMessage

    var pendingPollId: Int? = null

    init {
        initApp()
    }

    fun setDialogState(state: Boolean) {
        _dialogState.value = state
    }

    fun initApp() {
        viewModelScope.launch {
            _splashState.value = UiState.Loading

            try {
                // 1. Prepare SEAL
                _loadingMessage.value = "보안 환경을 준비하는 중..."
                val isSealReady = withContext(Dispatchers.IO) {
                    NativeLib.initContext()
                }
                if (!isSealReady) {
                    _splashState.value = UiState.Failure("보안 환경 구성에 실패했습니다.")
                    _loadingMessage.value = "오류 발생"
                    return@launch
                }

                delay(800)
                
                // 2. Prepare id
                _loadingMessage.value = "기기 정보를 확인하는 중..."
                val idResult = withContext(Dispatchers.IO) {
                    processIdCheck()
                }

                delay(800)

                if (idResult.isSuccess) {
                    _splashState.value = UiState.Success(Unit)
                    _loadingMessage.value = "준비 완료!"
                } else {
                    _loadingMessage.value = "오류 발생"
                    _splashState.value = UiState.Failure(idResult.exceptionOrNull()?.message ?: "알 수 없는 오류")
                }
            } catch (e: Exception) {
                _loadingMessage.value = "오류 발생"
                _splashState.value = UiState.Failure(e.message ?: "알 수 없는 오류")
            }
        }
    }

    private suspend fun processIdCheck(): Result<Unit> {
        return try {
            val isExistResult = idRepo.isExist()

            if (isExistResult.isSuccess) {
                val isExist = isExistResult.getOrDefault(false)

                if (isExist) {
                    Result.success(Unit)
                } else {
                    val newId = UUID.randomUUID().toString()
                    val createResult = idRepo.setId(newId)

                    if (createResult.isSuccess) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(createResult.exceptionOrNull()?.message))
                    }
                }
            } else {
                return Result.failure(Exception(isExistResult.exceptionOrNull()?.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}