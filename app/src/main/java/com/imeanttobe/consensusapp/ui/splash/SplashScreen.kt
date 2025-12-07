package com.imeanttobe.consensusapp.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.core.findActivity
import com.imeanttobe.consensusapp.navigation.Route

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val splashState = viewModel.splashState.collectAsStateWithLifecycle()
    val dialogState = viewModel.dialogState.collectAsStateWithLifecycle()
    val loadingMessage = viewModel.loadingMessage.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = splashState.value) {
        if (splashState.value is UiState.Success) {
            navController.navigate(Route.DevRoute) {
                popUpTo(Route.SplashScreen) {
                    inclusive = true
                }
            }
        } else if (splashState.value is UiState.Failure) {
            viewModel.setDialogState(true)
        }
    }

    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Title
            Text(
                text = "Consensus",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge,
            )

            // Loading indicator
            CircularWavyProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))

            // Loading message
            Text(
                text = loadingMessage.value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (dialogState.value) {
        AlertDialog(
            onDismissRequest = {  },
            icon = { Icon(imageVector = Icons.Outlined.Warning, contentDescription = null) },
            title = { Text("오류 발생") },
            text = { Text("초기화 중 오류가 발생하여 앱을 종료합니다. 다시 실행해주세요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        context.findActivity()?.finish()
                    }
                ) {
                    Text(text = "종료")
                }
            }
        )
    }
}