package com.imeanttobe.consensusapp.ui.result

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ResultScreen(id: Int) {
    Scaffold { innerPadding ->
        Text(
            text = "Result Screen: $id",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
