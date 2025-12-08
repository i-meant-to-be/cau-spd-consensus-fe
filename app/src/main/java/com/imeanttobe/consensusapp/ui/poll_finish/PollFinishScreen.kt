package com.imeanttobe.consensusapp.ui.poll_finish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imeanttobe.consensusapp.core.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PollFinishScreen(
    modifier: Modifier = Modifier,
    viewModel: PollFinishViewModel = hiltViewModel()
) {
    val finishPollUiState = viewModel.finishPollUiState.collectAsStateWithLifecycle()
    val submitPollResultUiState = viewModel.submitPollResultUiState.collectAsStateWithLifecycle()
    val statusMessage = viewModel.statusMessage.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = finishPollUiState.value) {
        when (val state = finishPollUiState.value) {
            is UiState.Success -> {

            }
            is UiState.Failure -> {

            }
            else -> {}
        }
    }

    LaunchedEffect(key1 = submitPollResultUiState.value) {
        when (val state = submitPollResultUiState.value) {
            is UiState.Success -> {

            }
            is UiState.Failure -> {

            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Loading indicator
            CircularWavyProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))

            // Loading message
            Text(
                text = statusMessage.value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

}