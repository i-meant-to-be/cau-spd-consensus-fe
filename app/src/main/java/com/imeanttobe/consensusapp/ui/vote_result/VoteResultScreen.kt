package com.imeanttobe.consensusapp.ui.vote_result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.imeanttobe.consensusapp.core.findActivity

@Composable
fun VoteResultScreen(
    isVoteSuccess: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconImageVector =
        if (isVoteSuccess) Icons.Outlined.CheckCircle
        else Icons.Outlined.ErrorOutline
    val message =
        if (isVoteSuccess) "투표에 성공했어요!"
        else "투표에 실패했어요. 오류 내용: ${errorMessage ?: "알 수 없음"}"

    val handleTerminate: () -> Unit = {
        context.findActivity()?.finish()
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Icon(
                imageVector = iconImageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = handleTerminate,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "종료")
            }
        }
    }
}