package com.imeanttobe.consensusapp.ui.create_poll.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OptionChip(
    option: String,
    onDelete: () -> Unit
) {
    InputChip(
        onClick = onDelete,
        label = { Text(text = option) },
        selected = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Localized description",
                modifier = Modifier.size(InputChipDefaults.AvatarSize)
            )
        },
    )
}

@Preview
@Composable
fun OptionChipPreview() {
    OptionChip(
        option = "선택지 1",
        onDelete = {}
    )
}