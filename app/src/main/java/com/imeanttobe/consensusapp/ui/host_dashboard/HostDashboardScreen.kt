package com.imeanttobe.consensusapp.ui.host_dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HostDashboardScreen(pollId: String) {
    Scaffold { innerPadding ->
        Text(
            text = "Host Dashboard Screen: $pollId",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
