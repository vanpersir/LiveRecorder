package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: Date?,
    onTimeSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // 从传入的Date对象中提取小时和分钟
    val calendar = Calendar.getInstance().apply { 
        time = value ?: Date() 
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value?.let { 
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: "",
            onValueChange = { /* 只读，不允许直接编辑 */ },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
        )
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val initialTime = LocalTime.of(
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE)
                            )
                            
                            // 计算合适的宽度，基于屏幕宽度的70%
                            val configuration = LocalConfiguration.current
                            val pickerWidth = (configuration.screenWidthDp * 0.7).dp
                            
                            WheelTimePicker(
                                startTime = initialTime,
                                timeFormat = TimeFormat.HOUR_24,
                                modifier = Modifier.width(pickerWidth)
                            ) { snappedTime ->
                                // 滚轮停止时更新时间，但不关闭对话框
                                val newCalendar = Calendar.getInstance().apply {
                                    time = value ?: Date()
                                    set(Calendar.HOUR_OF_DAY, snappedTime.hour)
                                    set(Calendar.MINUTE, snappedTime.minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onTimeSelected(newCalendar.time)
                            }
                        }
                    }
                },
                confirmButton = {
                    // 移除确认按钮
                },
                dismissButton = {
                    // 移除取消按钮
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimePickerFieldPreview() {
    TimePickerField(
        label = "演出时间",
        value = Date(),
        onTimeSelected = {}
    )
}