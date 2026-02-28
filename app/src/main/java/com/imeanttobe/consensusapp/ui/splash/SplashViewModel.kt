package com.imeanttobe.consensusapp.ui.splash

import android.util.Base64
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.BuildConfig
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
                    if (BuildConfig.IS_BENCHMARK_ENABLED) {
                        _loadingMessage.value = "벤치마킹 데이터를 준비하는 중..."

                        try {
                            initBenchmark()

                            _splashState.value = UiState.Success(Unit)
                            _loadingMessage.value = "준비 완료!"
                        } catch (e: Exception) {
                            _splashState.value = UiState.Failure(e.message ?: "알 수 없는 오류")
                            _loadingMessage.value = "오류 발생"
                        }
                    } else {
                        _splashState.value = UiState.Success(Unit)
                        _loadingMessage.value = "준비 완료!"
                    }
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

    private suspend fun initBenchmark() {
        withContext(Dispatchers.Default) {
            // 2. 키 생성 (PK, SK, RK 획득)
            val keys = NativeLib.generateKeys()
            requireNotNull(keys) { "키 생성에 실패했습니다." }

            // 3. 더미 데이터 암호화 (예: 길이가 1인 배열에 값 1과 2를 넣음)
            // 실제 BFV 스킴에서는 배치 인코딩을 쓰므로 전체 벡터를 넣어도 됩니다.
            val dummyInput1 = longArrayOf(1L)
            val dummyInput2 = longArrayOf(2L)

            val ct1Bytes = NativeLib.encrypt(dummyInput1, keys.pk)
            val ct2Bytes = NativeLib.encrypt(dummyInput2, keys.pk)

            requireNotNull(ct1Bytes) { "첫 번째 데이터 암호화 실패" }
            requireNotNull(ct2Bytes) { "두 번째 데이터 암호화 실패" }

            // 4. C++ 벤치마크 메모리에 적재 (이전에 만든 함수 호출)
            val isLoaded = NativeLib.loadBenchmarkData(
                ct1Bytes = ct1Bytes,
                ct2Bytes = ct2Bytes,
                rkBytes = keys.rk
            )

            if (!isLoaded) {
                throw IllegalStateException("C++ 벤치마크 메모리에 적재 실패")
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