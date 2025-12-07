package com.imeanttobe.consensusapp.ui.vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.data.remote.dto.GetPollResponse
import com.imeanttobe.consensusapp.navigation.Route
import com.imeanttobe.consensusapp.ui.common.InputItemDescription
import com.imeanttobe.consensusapp.ui.vote.components.CandidateCard
import com.imeanttobe.consensusapp.ui.vote.components.VoteConfirmDialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VoteScreen(
    id: Int,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: VoteViewModel = hiltViewModel()
) {
    val voteCode = viewModel.voteCode.collectAsStateWithLifecycle()
    val pollResponseUiState = viewModel.pollResponseUiState.collectAsStateWithLifecycle()
    val voteRequestUiState = viewModel.voteRequestUiState.collectAsStateWithLifecycle()
    val selectedCandidate = viewModel.selectedCandidate.collectAsStateWithLifecycle()
    val dialogState = viewModel.dialogState.collectAsStateWithLifecycle()
    val voteStatusMessage = viewModel.voteStatusMessage.collectAsStateWithLifecycle()

    val handleOpenDialog: () -> Unit = { viewModel.setDialogState(true) }
    val handleDismiss: () -> Unit = { viewModel.setDialogState(false) }
    val handleVote: () -> Unit = {
        viewModel.setDialogState(false)
        viewModel.vote()
    }

    val isSubmitButtonEnabled = voteCode.value.isNotEmpty()
            && selectedCandidate.value.isNotBlank()
            && pollResponseUiState.value is UiState.Success
            && voteRequestUiState.value !is UiState.Loading

    LaunchedEffect(key1 = voteRequestUiState.value) {
        when (voteRequestUiState.value) {
            is UiState.Success -> {
                navController.navigate(
                    Route.VoteResultScreen(
                        isVoteSuccess = true,
                        errorMessage = ""
                    )
                )
            }
            is UiState.Failure -> {
                navController.navigate(
                    Route.VoteResultScreen(
                        isVoteSuccess = false,
                        errorMessage = (voteRequestUiState.value as UiState.Failure).message
                    )
                )
            }
            else -> {}
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        when (pollResponseUiState.value) {
            is UiState.Success -> {
                val response = (pollResponseUiState.value as UiState.Success<GetPollResponse>).data

                Box(
                    modifier = Modifier
                        .padding(paddingValues = innerPadding)
                        .fillMaxSize()
                ) {
                    // Main content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Text(
                                    text = response.title,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = "#$id",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        InputItemDescription(
                            title = "투표 코드",
                            icon = Icons.Outlined.Password,
                            description = "투표 주최자에게 제공받은 투표 코드를 입력하세요.",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = voteCode.value,
                            onValueChange = viewModel::setVoteCode,
                            label = { Text(text = "투표 코드") },
                            singleLine = true,
                            maxLines = 1,
                            placeholder = { Text(text = "ABCD1234")},
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                        )

                        InputItemDescription(
                            title = "선택",
                            icon = Icons.Outlined.HowToVote,
                            description = "투표하고 싶은 후보를 1개 골라주세요.",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            response.candidates.forEach { candidate ->
                                CandidateCard(
                                    candidate = candidate,
                                    isSelected = selectedCandidate.value == candidate,
                                    onClick = { viewModel.setSelectedCandidate(candidate) }
                                )
                            }
                        }
                    }

                    // Submit button
                    Button(
                        onClick = handleOpenDialog,
                        enabled = isSubmitButtonEnabled,
                        contentPadding = ButtonDefaults.MediumContentPadding,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
                    ) {
                        if (voteRequestUiState.value is UiState.Loading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularWavyProgressIndicator(modifier = Modifier.size(ButtonDefaults.MediumIconSize))
                                Text(
                                    text = voteStatusMessage.value,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Text(text = "투표하기")
                        }
                    }
                }
            }
            is UiState.Failure -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(paddingValues = innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                    )
                    Text(
                        text = "투표 데이터를 불러오지 못했어요: ${(pollResponseUiState.value as UiState.Failure).message}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(paddingValues = innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CircularWavyProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                    Text(
                        text = "투표 데이터를 불러오는 중...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    if (dialogState.value) {
        VoteConfirmDialog(
            onConfirm = handleVote,
            onDismiss = handleDismiss
        )
    }
}
