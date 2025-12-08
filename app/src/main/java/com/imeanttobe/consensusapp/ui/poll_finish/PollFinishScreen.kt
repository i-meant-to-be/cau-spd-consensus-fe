package com.imeanttobe.consensusapp.ui.poll_finish

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PollFinishScreen(id: Int) {
    Scaffold() {
        Text(text = "Hello $id")
    }
}