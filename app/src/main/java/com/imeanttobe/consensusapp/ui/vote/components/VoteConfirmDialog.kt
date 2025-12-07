package com.imeanttobe.consensusapp.ui.vote.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun VoteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.HowToVote,
                contentDescription = null
            )
        },
        onDismissRequest = onDismiss,
        title = { Text(text = "최종 확인") },
        text = { Text(text = "확인을 누르면 선택을 되돌릴 수 없어요. 투표를 완료하시겠어요?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
    )
}
