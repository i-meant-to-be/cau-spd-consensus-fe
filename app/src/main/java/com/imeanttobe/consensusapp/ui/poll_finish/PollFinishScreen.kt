package com.imeanttobe.consensusapp.ui.poll_finish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.navigation.Route

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PollFinishScreen(
    id: Int,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PollFinishViewModel = hiltViewModel()
) {
    val submitPollResultUiState = viewModel.submitPollResultUiState.collectAsStateWithLifecycle()
    val statusMessage = viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = submitPollResultUiState.value) {
        when (val state = submitPollResultUiState.value) {
            is UiState.Success -> {
                navController.navigate(Route.PollResultScreen(id = id)) {
                    popUpTo(Route.HostDashboardScreen) { inclusive = true }
                }
            }
            is UiState.Failure -> {
                val message = "투표 완료 처리 실패. 원인: ${state.message}"
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                navController.popBackStack()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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