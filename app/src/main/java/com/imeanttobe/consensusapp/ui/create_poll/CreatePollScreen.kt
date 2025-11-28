package com.imeanttobe.consensusapp.ui.create_poll

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.imeanttobe.consensusapp.core.Constants.MAX_OPTIONS
import com.imeanttobe.consensusapp.core.Constants.MAX_PARTICIPANTS
import com.imeanttobe.consensusapp.core.Constants.MAX_TITLE_LENGTH
import com.imeanttobe.consensusapp.core.Constants.MIN_OPTIONS
import com.imeanttobe.consensusapp.core.Constants.MIN_PARTICIPANTS
import com.imeanttobe.consensusapp.core.UiState
import com.imeanttobe.consensusapp.ui.create_poll.components.AddOptionDialog
import com.imeanttobe.consensusapp.ui.create_poll.components.CreatePollScreenTopBar
import com.imeanttobe.consensusapp.ui.create_poll.components.InputItemDescription
import com.imeanttobe.consensusapp.ui.create_poll.components.OptionChip

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreatePollScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: CreatePollViewModel = hiltViewModel()
) {
    val title = viewModel.title.collectAsStateWithLifecycle()
    val numParticipants = viewModel.numParticipants.collectAsStateWithLifecycle()
    val options = viewModel.options.collectAsStateWithLifecycle()
    val newOption = viewModel.newOption.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState = viewModel.dialogState.collectAsStateWithLifecycle()

    val handleBackClick: () -> Unit = { navController.popBackStack() }
    val handleSliderChange: (Float) -> Unit = { sliderValue -> viewModel.setNumParticipants(sliderValue.toInt()) }
    val handleSubmit: () -> Unit = { viewModel.submit() }
    val handleOpenDialog: () -> Unit = { viewModel.setDialogState(true) }
    val handleDialogConfirm: () -> Unit = {
        viewModel.appendOption(newOption.value)
        viewModel.setDialogState(false)
        viewModel.setNewOption("")
    }
    val handleDialogDismiss: () -> Unit = {
        viewModel.setDialogState(false)
        viewModel.setNewOption("")
    }

    val isSubmitButtonEnabled = title.value.isNotEmpty()
            && options.value.size >= MIN_OPTIONS
            && options.value.size <= MAX_OPTIONS
            && uiState.value != UiState.Loading
    val isNewOptionButtonEnabled = options.value.size < MAX_OPTIONS
    val textLengthColor =
        if (title.value.length < MAX_TITLE_LENGTH) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.error

    Scaffold(
        topBar = { CreatePollScreenTopBar(onBackClicked = handleBackClick) },
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Title
                InputItemDescription(
                    title = "투표 제목",
                    icon = Icons.Outlined.Edit,
                    description = "투표의 제목을 입력해주세요.",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = title.value,
                    onValueChange = viewModel::setTitle,
                    label = { Text(text = "투표 제목") },
                    singleLine = true,
                    maxLines = 1,
                    placeholder = { Text(text = "2025년 반장 선거")},
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Text(
                    text = "${title.value.length} / $MAX_TITLE_LENGTH",
                    color = textLengthColor,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 32.dp)
                )

                // Participants
                InputItemDescription(
                    title = "참여 인원",
                    icon = Icons.Outlined.Person,
                    description = "참여 인원을 정해주세요. 최소 3명, 최대 10명까지 가능해요.",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = numParticipants.value.toFloat(),
                    onValueChange = handleSliderChange,
                    valueRange = MIN_PARTICIPANTS.toFloat()..MAX_PARTICIPANTS.toFloat(),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(8.dp).padding(bottom = 32.dp)
                ) {
                    for (i in MIN_PARTICIPANTS..MAX_PARTICIPANTS) {
                        Text(text = i.toString(), color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Options
                InputItemDescription(
                    title = "후보",
                    icon = Icons.Outlined.HowToVote,
                    description = "후보를 입력해주세요. 최소 2개, 최대 3개까지 가능해요.",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (option in options.value) {
                        OptionChip(
                            option = option,
                            onDelete = { viewModel.removeOption(option) }
                        )
                    }
                    Button(
                        enabled = isNewOptionButtonEnabled,
                        onClick = handleOpenDialog
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Option"
                        )
                    }
                }
            }

            // Submit button
            Button(
                onClick = handleSubmit,
                enabled = isSubmitButtonEnabled,
                contentPadding = ButtonDefaults.MediumContentPadding,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
            ) {
                if (uiState.value is UiState.Loading) {
                    CircularWavyProgressIndicator()
                } else {
                    Text(text = "투표 개최하기")
                }
            }
        }
    }

    if (dialogState.value) {
        AddOptionDialog(
            newOption = newOption.value,
            onNewOptionChange = viewModel::setNewOption,
            onConfirm = handleDialogConfirm,
            onDismiss = handleDialogDismiss
        )
    }
}
