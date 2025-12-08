package com.imeanttobe.consensusapp.ui.host_dashboard.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun FinishConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null
            )
        },
        onDismissRequest = onDismiss,
        title = { Text(text = "투표 마감") },
        text = { Text(text = "이 작업은 되돌릴 수 없어요. 투표를 마감하시겠어요?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "마감")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
    )
}
