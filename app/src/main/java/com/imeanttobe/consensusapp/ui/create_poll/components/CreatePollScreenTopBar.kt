package com.imeanttobe.consensusapp.ui.create_poll.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollScreenTopBar(
    onBackClicked: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(text = "투표 생성") },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Create Poll"
                )
            }
        },
    )
}