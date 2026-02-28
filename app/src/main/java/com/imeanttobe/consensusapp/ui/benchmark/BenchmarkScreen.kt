package com.imeanttobe.consensusapp.ui.benchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fitInside
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.domain.benchmark.BenchmarkMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BenchmarkScreen(viewModel: BenchmarkViewModel = hiltViewModel()) {
    val options = listOf(
        BenchmarkMode.KOTLIN_DEFAULT,
        BenchmarkMode.KOTLIN_IO,
        BenchmarkMode.KOTLIN_CUSTOM_POOL,
        BenchmarkMode.CPP_NATIVE_THREAD
    )

    val benchmarkState = viewModel.benchmarkState.collectAsStateWithLifecycle()
    val benchmarkMode = viewModel.benchmarkMode.collectAsStateWithLifecycle()
    val totalIterations = viewModel.totalIterations.collectAsStateWithLifecycle()
    val threadCount = viewModel.threadCount.collectAsStateWithLifecycle()

    val handleModeChange = { mode: BenchmarkMode ->
        viewModel.setBenchmarkMode(mode)
    }
    val handleTotalIterationsChange = { iterations: String ->
        viewModel.setTotalIterations(iterations.filter { it.isDigit() })
    }
    val handleThreadCountChange = { count: String ->
        viewModel.setThreadCount(count.filter { it.isDigit() })
    }

    val resultText = when (val state = benchmarkState.value) {
        is UiState.Success -> "${state.data} ms"
        is UiState.Failure -> "오류 발생: ${state.message}"
        is UiState.Loading -> "로딩 중..."
        else -> "대기 중"
    }

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Benchmark Mode")
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = benchmarkMode.value == option,
                        onCheckedChange = { handleModeChange(option) }
                    )
                    Text(text = option.name)
                }
            }

            Text(text = "Total Iterations")
            TextField(
                value = totalIterations.value,
                onValueChange = { handleTotalIterationsChange(it) },
                label = { Text("Iterations") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(text = "Thread Count")
            TextField(
                value = threadCount.value,
                onValueChange = { handleThreadCountChange(it) },
                label = { Text("Threads") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = { viewModel.runBenchmark() },
                enabled = benchmarkState.value !is UiState.Loading
            ) {
                if (benchmarkState.value is UiState.Loading) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(ButtonDefaults.MediumIconSize))
                } else {
                    Text(text = "Run Benchmark")
                }
            }

            Text(text = "Result")
            Text(
                text = resultText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}