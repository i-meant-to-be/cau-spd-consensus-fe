package com.imeanttobe.consensusapp.core

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Failure(val message: String) : UiState<Nothing>
}