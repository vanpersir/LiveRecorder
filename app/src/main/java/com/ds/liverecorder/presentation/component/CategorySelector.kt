package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    options: List<String> = listOf("现场", "演唱会", "音乐节", "话剧", "音乐剧", "相声", "脱口秀", "其他")
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySelectorPreview() {
    CategorySelector(
        label = "分类",
        value = "现场",
        onValueChange = {}
    )
}