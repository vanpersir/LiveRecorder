package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "CNY",
    onCurrencyChange: (String) -> Unit = {}
) {
    var amount by remember { mutableStateOf(value) }
    var currentCurrency by remember { mutableStateOf(currency) }
    var expanded by remember { mutableStateOf(false) }
    
    // 主流货币列表
    val currencies = listOf(
        "CNY", "USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "HKD", "SGD"
    )

    Column(modifier = modifier) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // 只允许输入数字和小数点
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        amount = newValue
                        onValueChange(newValue)
                    }
                },
                label = { Text("金额") },
                modifier = Modifier
                    .weight(3f) // 调整权重为3，确保价格输入框占据更多空间
                    .padding(end = 8.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField( // 改为OutlinedTextField以保持一致性
                    readOnly = true,
                    value = currentCurrency,
                    onValueChange = { },
                    label = { Text("货币") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .weight(1f) // 调整权重为1，减少货币选择框占用空间
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currencies.forEach { currencyItem ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(currencyItem) },
                            onClick = {
                                currentCurrency = currencyItem
                                onCurrencyChange(currencyItem)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyFieldPreview() {
    CurrencyField(
        label = "票价",
        value = "100",
        onValueChange = {}
    )
}