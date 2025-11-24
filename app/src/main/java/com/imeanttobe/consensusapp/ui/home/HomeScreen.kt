package com.imeanttobe.consensusapp.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "Home Screen ",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
