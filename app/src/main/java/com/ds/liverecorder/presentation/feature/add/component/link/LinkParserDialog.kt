package com.ds.liverecorder.presentation.feature.add.component.link

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkParserDialog(
    onDismissRequest: () -> Unit,
    onConcertParsed: (String) -> Unit,
    isParsing: Boolean = false,
    errorMessage: String? = null
) {
    var link by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("解析链接")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = link,
                    onValueChange = { 
                        link = it
                    },
                    label = { Text("请输入演出链接") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    leadingIcon = {
                        Icon(Icons.Filled.Link, contentDescription = null)
                    },
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { 
                            Text(it)
                        }
                    }
                )
                
                Text(
                    text = "支持秀动、大麦等平台的分享链接",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // 显示加载状态
                if (isParsing) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "正在解析链接...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (link.isBlank()) {
                        return@Button
                    }
                    
                    onConcertParsed(link)
                },
                enabled = !isParsing
            ) {
                Text(if (isParsing) "解析中..." else "解析")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest,
                enabled = !isParsing
            ) {
                Text("取消")
            }
        }
    )
}