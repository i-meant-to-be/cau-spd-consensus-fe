package com.imeanttobe.consensusapp.ui.dev

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.util.UUID

@Composable
fun DevScreen(viewModel: DevViewModel = hiltViewModel()) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(text = viewModel.userId.value)

            Button(
                onClick = {
                    viewModel.setId(UUID.randomUUID().toString())
                }
            ) {
                Text(text = "Set ID")
            }
        }
    }
}