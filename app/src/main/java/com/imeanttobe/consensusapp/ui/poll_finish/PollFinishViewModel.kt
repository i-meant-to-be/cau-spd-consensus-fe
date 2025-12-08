package com.imeanttobe.consensusapp.ui.poll_finish

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.core.crypto.CryptoManager
import com.imeanttobe.consensusapp.data.remote.dto.FinishPollResponse
import com.imeanttobe.consensusapp.data.remote.dto.SubmitPollResultResponse
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo
import com.imeanttobe.consensusapp.domain.repo.PollRepo
import com.imeanttobe.consensusapp.seal.NativeLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PollFinishViewModel @Inject constructor(
    private val pollRepo: PollRepo,
    private val pollInfoItemsRepo: PollInfoItemsRepo,
    private val cryptoManager: CryptoManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _finishPollUiState = MutableStateFlow<UiState<FinishPollResponse>>(UiState.Idle)
    val finishPollUiState: StateFlow<UiState<FinishPollResponse>> = _finishPollUiState

    private val _submitPollResultUiState = MutableStateFlow<UiState<SubmitPollResultResponse>>(UiState.Idle)
    val submitPollResultUiState: StateFlow<UiState<SubmitPollResultResponse>> = _submitPollResultUiState

    private val _statusMessage = MutableStateFlow("투표 마감 준비 중...")
    val statusMessage: StateFlow<String> = _statusMessage

    init {
        val id = savedStateHandle.get<Int>("id") ?: -1

        if (id != -1) {
            finishPoll(id = id)
        } else {
            _finishPollUiState.value = UiState.Failure("Invalid poll id")
            _submitPollResultUiState.value = UiState.Failure("Invalid poll id")
        }
    }

    /**
     * # 동작 플로우
     * 1. 투표가 마감됐음을 서버에 알린다.
     * 2. 응답으로 모든 암호화/인코딩된 투표 값 리스트(List<Base64 String>)를 받는다.
     * 3. 인코딩된 리스트를 디코딩한다.
     * 4. PollInfoItemsRepo에서 투표 ID에 맞는 암호화된 SK, IV를 꺼낸다.
     * 5. SK는 CryptoManager로 복호화한다.
     * 6. 인코딩된 리스트를 암호화된 상태로 모두 더한다.
     * 7. 더한 결과를 SK로 복호화하여 최종 투표 결과를 계산한다.
     * 8. 최종 투표 결과를 서버에 전송한다.
     * 9. 전송이 성공적으로 완료되었다면 투표 결과 화면 PollResultScreen으로 이동한다.
     */

    /**
     * 투표 마감 함수. 투표가 마감되었음을 서버에 알립니다.
     * 1. 투표가 마감됐음을 서버에 알린다.
     * 2. 응답으로 모든 암호화/인코딩된 투표 값 리스트(List<Base64 String>)를 받는다.
     * @param id 마감할 투표의 ID
     */
    private fun finishPoll(id: Int) {
        _finishPollUiState.value = UiState.Loading
        _statusMessage.value = "투표 마감 요청 중..."

        viewModelScope.launch {
            val response = pollRepo.finishPoll(id = id)
            response.fold(
                onSuccess = { responseBody ->
                    _finishPollUiState.value = UiState.Success(responseBody)
                    submitPollResult(id = id, encryptedVotes = responseBody.votes)
                },
                onFailure = { _finishPollUiState.value = UiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }

    /**
     * 투표 결과 취합 및 계산 함수. 암호화된 투표 값을 모두 더하고 복호화한 후, 이를 서버에 전송합니다.
     * 3. 인코딩된 리스트를 디코딩한다.
     * 4. PollInfoItemsRepo에서 투표 ID에 맞는 암호화된 SK, IV를 꺼낸다.
     * 5. SK는 CryptoManager로 복호화한다.
     * 6. 인코딩된 리스트를 암호화된 상태로 모두 더한다.
     * 7. 더한 결과를 SK로 복호화하여 최종 투표 결과를 계산한다.
     * 8. 최종 투표 결과를 서버에 전송한다.
     * 9. 전송이 성공적으로 완료되었다면 최근 투표 기록에서 해당 투표를 지운다.
     * 10. 투표 결과 화면 PollResultScreen으로 이동한다.
     * @param id 투표의 ID
     * @param encryptedVotes 암호화된 투표 값 리스트
     */
    private fun submitPollResult(id: Int, encryptedVotes: List<String>) {
        _submitPollResultUiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.Default) {
            try {
                // 2-1. Check whether votes are empty
                if (encryptedVotes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _statusMessage.value = "투표자가 없습니다."
                        _submitPollResultUiState.value = UiState.Failure("No votes found")
                    }
                    return@launch
                }

                // 3. Decode given list
                _statusMessage.value = "투표 집계 준비 중..."
                val decodedEncryptedVotes = encryptedVotes.map { Base64.decode(it, Base64.NO_WRAP) }

                // 4. Get encrypted SK and IV from PollInfoItemsRepo
                pollInfoItemsRepo.getPollInfo(id).fold(
                    onSuccess = { pollInfo ->
                        // 5. Decrypt SK with CryptoManager
                        val decryptedSk = cryptoManager.decrypt(
                            ciphertext = pollInfo.encryptedSk.toByteArray(),
                            iv = pollInfo.iv.toByteArray()
                        )

                        // 6. Sum all encoded and encrypted list with SEAL
                        _statusMessage.value = "암호화된 투표 값 합산 중..."
                        var sum = decodedEncryptedVotes.first()

                        for (i in 1 until decodedEncryptedVotes.size) {
                            val addValue = NativeLib.addCiphertexts(sum, decodedEncryptedVotes[i])
                            if (addValue == null) {
                                _submitPollResultUiState.value = UiState.Failure("Failed to add votes")
                                return@launch
                            }
                            sum = addValue
                        }

                        // 7. Decrypt sum with SEAL
                        _statusMessage.value = "결과 복호화 중..."
                        val decryptedSum = NativeLib.decrypt(cipherBytes = sum, secretKeyBytes = decryptedSk)
                        if (decryptedSum == null) {
                            _submitPollResultUiState.value = UiState.Failure("Failed to decrypt sum")
                            return@launch
                        }
                        val payload = decryptedSum.map { it.toInt() }

                        // 8. Send result to server
                        val submitPollResultRequest = pollRepo.submitPollResult(id = id, votes = payload)
                        withContext(Dispatchers.Main) {
                            submitPollResultRequest.fold(
                                onSuccess = { responseBody ->
                                    // 9. Delete poll from PollInfoItemsRepo
                                    val removeRequest = pollInfoItemsRepo.removePollInfo(id)
                                    if (removeRequest.isFailure) {
                                        _submitPollResultUiState.value = UiState.Failure(removeRequest.exceptionOrNull()?.message ?: "Unknown error")
                                        return@withContext
                                    }

                                    // 10. Move to result screen
                                    _submitPollResultUiState.value = UiState.Success(responseBody)
                                    _statusMessage.value = "투표 마감 완료!"
                                },
                                onFailure = {
                                    _submitPollResultUiState.value = UiState.Failure(it.message ?: "Unknown error")
                                }
                            )
                        }
                    },
                    onFailure = {
                        _submitPollResultUiState.value = UiState.Failure(it.message ?: "Unknown error")
                        return@launch
                    }
                )
            } catch (e: Exception) {
                _submitPollResultUiState.value = UiState.Failure(e.message ?: "Unknown error")
            }
        }
    }
}