package com.ds.liverecorder.presentation.feature.imprint.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.presentation.common.component.StatItem
import com.ds.liverecorder.presentation.viewmodel.ImprintViewModel
import java.util.Locale

@Composable
fun DataOverviewCard(
    viewModel: ImprintViewModel,
    totalConcerts: Int,
    watchedConcerts: Int,
    upcomingConcerts: Int
) {
    val uiState = viewModel.uiState
    
    // 货币单位选择状态
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    
    // 支持的货币单位列表
    val supportedCurrencies = listOf("CNY", "USD", "EUR", "JPY", "HKD")

    // 数据概览卡片
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 数据概览标题行，包含标题和货币选择按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "数据概览",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // 货币单位选择器
                Box {
                    // 货币单位选择按钮
                    TextButton(
                        onClick = { showCurrencyDropdown = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = uiState.selectedCurrency,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showCurrencyDropdown,
                        onDismissRequest = { showCurrencyDropdown = false },
                        modifier = Modifier
                    ) {
                        supportedCurrencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    viewModel.updateSelectedCurrency(currency)
                                    showCurrencyDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            StatItem("总观演次数", "$totalConcerts")
            StatItem("已观看", "$watchedConcerts")
            StatItem("待观看", "$upcomingConcerts")

            // 总花费行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "总花费",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (uiState.isLoadingRates) {
                    Text(
                        text = "计算中...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "${getCurrencySymbol(uiState.selectedCurrency)}${
                            String.format(
                                Locale.getDefault(),
                                "%.2f",
                                uiState.totalSpent
                            )
                        }",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 获取货币符号
 * @param currency 货币代码
 * @return 货币符号
 */
fun getCurrencySymbol(currency: String): String {
    return when (currency) {
        "CNY" -> "¥"
        "USD" -> "$"
        "EUR" -> "€"
        "JPY" -> "¥"
        "HKD" -> "HK$"
        else -> currency
    }
}