package com.imeanttobe.consensusapp.ui.benchmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.domain.benchmark.BenchmarkMode
import com.imeanttobe.consensusapp.domain.benchmark.RunBenchmarkUseCase
import com.imeanttobe.consensusapp.seal.NativeLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BenchmarkViewModel @Inject constructor(
    private val runBenchmarkUseCase: RunBenchmarkUseCase
) : ViewModel() {

    private val _benchmarkState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val benchmarkState: StateFlow<UiState<Long>> = _benchmarkState

    private val _benchmarkMode = MutableStateFlow(BenchmarkMode.KOTLIN_DEFAULT)
    val benchmarkMode: StateFlow<BenchmarkMode> = _benchmarkMode

    private val _totalIterations = MutableStateFlow("1000")
    val totalIterations: StateFlow<String> = _totalIterations

    private val _threadCount = MutableStateFlow("1")
    val threadCount: StateFlow<String> = _threadCount

    fun setBenchmarkMode(mode: BenchmarkMode) {
        _benchmarkMode.value = mode
    }

    fun setTotalIterations(iterations: String) {
        _totalIterations.value = iterations.filter { it.isDigit() }
    }

    fun setThreadCount(count: String) {
        _threadCount.value = count.filter { it.isDigit() }
    }

    fun runBenchmark() {
        // Prevent multiple runs
        if (_benchmarkState.value is UiState.Loading) return

        // Reset state
        _benchmarkState.value = UiState.Loading

        // Run
        viewModelScope.launch {
            try {
                val parsedTotalIterations = totalIterations.value.toIntOrNull()?.coerceIn(1..1000000) ?: 0
                val parsedThreadCount = threadCount.value.toIntOrNull()?.coerceIn(1..32) ?: 0

                val result = runBenchmarkUseCase(
                    mode = benchmarkMode.value,
                    totalIterations = parsedTotalIterations,
                    threadCount = parsedThreadCount,
                )

                _benchmarkState.value = UiState.Success(result)
            } catch (e: Exception) {
                _benchmarkState.value = UiState.Failure(e.message ?: "Unknown error")
            }
        }
    }
}