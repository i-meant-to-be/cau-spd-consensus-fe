package com.imeanttobe.consensusapp.ui.create_poll

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CreatePollScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "Create Poll Screen",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
