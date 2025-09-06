package com.ds.liverecorder.presentation.feature.settings.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.presentation.common.component.SettingItem

@Composable
fun SettingsScreen() {
    val isDarkMode = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 外观设置
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "外观",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SettingItem(
                    icon = Icons.Filled.Palette,
                    title = "深色模式",
                    description = if (isDarkMode.value) "已启用" else "已禁用",
                    onClick = { isDarkMode.value = !isDarkMode.value }
                )
            }
        }
        
        // 数据设置
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "数据",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SettingItem(
                    icon = Icons.Filled.Storage,
                    title = "备份数据",
                    description = "导出所有演出记录",
                    onClick = { /* TODO: 实现备份功能 */ }
                )
                
                SettingItem(
                    icon = Icons.Filled.Storage,
                    title = "恢复数据",
                    description = "从备份文件恢复",
                    onClick = { /* TODO: 实现恢复功能 */ }
                )
            }
        }
        
        // 关于
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SettingItem(
                    icon = Icons.Filled.Info,
                    title = "关于应用",
                    description = "版本信息和开发者信息",
                    onClick = { /* TODO: 显示关于信息 */ }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}