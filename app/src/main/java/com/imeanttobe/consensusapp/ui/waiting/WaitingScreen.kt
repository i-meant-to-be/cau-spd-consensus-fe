package com.imeanttobe.consensusapp.ui.waiting

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WaitingScreen(id: Int) {
    Scaffold { innerPadding ->
        Text(
            text = "Waiting Screen: $id",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
