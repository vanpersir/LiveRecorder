package com.ds.liverecorder.presentation.common.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索演出"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "搜索"
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        value = "",
        onValueChange = {}
    )
}