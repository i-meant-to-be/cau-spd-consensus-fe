package com.imeanttobe.consensusapp.ui.create_poll

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.kotlin.toByteString
import com.imeanttobe.consensusapp.PollInfo
import com.imeanttobe.consensusapp.core.Constants.MAX_OPTIONS
import com.imeanttobe.consensusapp.core.Constants.MAX_OPTION_LENGTH
import com.imeanttobe.consensusapp.core.Constants.MAX_PARTICIPANTS
import com.imeanttobe.consensusapp.core.Constants.MAX_TITLE_LENGTH
import com.imeanttobe.consensusapp.core.Constants.MIN_PARTICIPANTS
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.core.crypto.CryptoManager
import com.imeanttobe.consensusapp.data.remote.dto.CreatePollRequest
import com.imeanttobe.consensusapp.domain.repo.PollInfoItemsRepo
import com.imeanttobe.consensusapp.domain.repo.PollRepo
import com.imeanttobe.consensusapp.seal.NativeLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreatePollViewModel @Inject constructor(
    private val pollRepo: PollRepo,
    private val pollInfoItemsRepo: PollInfoItemsRepo,
    private val cryptoManager: CryptoManager
) : ViewModel() {
    private val _title = MutableStateFlow<String>("")
    val title: StateFlow<String> = _title

    private val _options = MutableStateFlow<PersistentList<String>>(persistentListOf())
    val options: StateFlow<PersistentList<String>> = _options

    private val _newOption = MutableStateFlow<String>("")
    val newOption: StateFlow<String> = _newOption

    private val _numParticipants = MutableStateFlow<Int>(MIN_PARTICIPANTS)
    val numParticipants: StateFlow<Int> = _numParticipants

    private val _uiState = MutableStateFlow<UiState<PollInfo>>(UiState.Idle)
    val uiState: StateFlow<UiState<PollInfo>> = _uiState

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

    fun createPoll() {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            // Prepare PK and SK
            val sealKeys = withContext(Dispatchers.IO) {
                NativeLib.generateKeys()
            }
            if (sealKeys == null) {
                _uiState.value = UiState.Failure("Failed to generate keys")
                return@launch
            }

            // Create request body
            val encodedPk = Base64.encodeToString(sealKeys.pk, Base64.NO_WRAP)

            // Send request
            val response = pollRepo.createPoll(
                title = title.value,
                pk = encodedPk,
                candidates = options.value.toList()
            )

            // Handle response
            if (response.isSuccess) {
                val responseBody = response.getOrNull()
                if (responseBody == null) {
                    _uiState.value = UiState.Failure("Response body is null")
                    return@launch
                } else {
                    // Save poll info to local storage
                    val encryptionResult = cryptoManager.encrypt(sealKeys.sk)
                    val newPollInfo = PollInfo.newBuilder()
                        .setTitle(title.value)
                        .setId(responseBody.id)
                        .setPk(sealKeys.pk.toByteString())
                        .setEncryptedSk(encryptionResult.ciphertext.toByteString())
                        .setIv(encryptionResult.iv.toByteString())
                        .build()
                    pollInfoItemsRepo.addPollInfo(newPollInfo)

                    _uiState.value = UiState.Success(newPollInfo)
                }
            } else {
                _uiState.value = UiState.Failure(response.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}