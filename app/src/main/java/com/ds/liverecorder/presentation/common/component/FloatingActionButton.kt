package com.ds.liverecorder.presentation.common.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AddConcertFloatingActionButton(
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Filled.Add, contentDescription = "添加演出") },
        text = { Text("添加演出") }
    )
}

@Preview(showBackground = true)
@Composable
fun AddConcertFloatingActionButtonPreview() {
    AddConcertFloatingActionButton(onClick = {})
}