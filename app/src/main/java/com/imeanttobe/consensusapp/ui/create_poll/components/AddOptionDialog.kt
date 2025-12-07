package com.imeanttobe.consensusapp.ui.create_poll.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imeanttobe.consensusapp.core.Constants.MAX_OPTION_LENGTH

@Composable
fun AddOptionDialog(
    newOption: String,
    onNewOptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isConfirmButtonEnabled = newOption.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "새 후보 추가") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "새로 추가할 후보를 입력해주세요."
                )
                OutlinedTextField(
                    value = newOption,
                    onValueChange = onNewOptionChange,
                    label = { Text(text = "후보") },
                    singleLine = true,
                    maxLines = 1,
                    placeholder = { Text(text = "홍길동") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${newOption.length} / $MAX_OPTION_LENGTH",
                    color = if (newOption.length < MAX_OPTION_LENGTH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isConfirmButtonEnabled,
                onClick = onConfirm
            ) {
                Text(text = "추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        }
    )
}