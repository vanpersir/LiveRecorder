package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun CurrencyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "CNY"
) {
    var amount by remember { mutableStateOf(value) }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
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
                label = { Text(label) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )
            
            Text(
                text = currency,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
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