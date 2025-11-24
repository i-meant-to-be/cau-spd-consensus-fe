package com.imeanttobe.consensusapp.ui.result

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ResultScreen(pollId: String) {
    Scaffold { innerPadding ->
        Text(
            text = "Result Screen: $pollId",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
