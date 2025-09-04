package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: Date?,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // 初始化时就显示对话框
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value?.time ?: System.currentTimeMillis()
    )
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value?.let { 
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            } ?: "",
            onValueChange = { /* 只读，不允许直接编辑 */ },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
        )
        
        
        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // 创建新的日期对象，保持时间部分不变，只更新日期部分
                                val selectedDate = Calendar.getInstance().apply {
                                    timeInMillis = millis
                                }
                                
                                val newDate = Calendar.getInstance().apply {
                                    time = value ?: Date()
                                    set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                                    set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                                }
                                
                                onDateSelected(newDate.time)
                            }
                            showDialog = false
                        }
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DatePickerFieldPreview() {
    DatePickerField(
        label = "演出日期",
        value = Date(),
        onDateSelected = {}
    )
}