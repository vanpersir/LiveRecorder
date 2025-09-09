package com.ds.liverecorder.presentation.feature.imprint.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.viewmodel.ImprintViewModel
import java.util.Date
import java.util.Locale

@Composable
fun ImprintScreen(
    viewModel: ImprintViewModel = concertViewModel()
) {
    val concerts by viewModel.concerts.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState
    
    // 货币单位选择状态
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    
    // 支持的货币单位列表
    val supportedCurrencies = listOf("CNY", "USD", "EUR", "JPY", "HKD")
    
    // 统计数据
    val totalConcerts = concerts.size
    // 已观看：状态为正常，演出时间在当前时间之前
    val watchedConcerts = concerts.count { concert: Concert -> 
        concert.status == "正常" && concert.date.before(Date()) 
    }
    // 未观看：状态为正常，演出时间在当前时间之后
    val upcomingConcerts = concerts.count { concert: Concert -> 
        concert.status == "正常" && concert.date.after(Date()) 
    }
    
    // 当选中货币改变时更新ViewModel
    LaunchedEffect(uiState.selectedCurrency) {
        viewModel.updateSelectedCurrency(uiState.selectedCurrency)
    }
    
    // 页面加载时计算总花费
    LaunchedEffect(concerts) {
        if (concerts.isNotEmpty()) {
            viewModel.updateSelectedCurrency(uiState.selectedCurrency)
        }
    }
    
    // 按年份统计
    val concertsByYear = viewModel.getConcertsByYear()
    
    // 按分类统计
    val concertsByCategory = viewModel.getConcertsByCategory()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和货币选择区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "印记",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // 货币单位选择按钮
            Column {
                TextButton(
                    onClick = { showCurrencyDropdown = true }
                ) {
                    Text("单位: ${uiState.selectedCurrency}")
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "选择货币单位"
                    )
                }
                
                DropdownMenu(
                    expanded = showCurrencyDropdown,
                    onDismissRequest = { showCurrencyDropdown = false }
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
        
        // 数据概览卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "数据概览",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                StatItem("总观演次数", "$totalConcerts")
                StatItem("已观看", "$watchedConcerts")
                StatItem("待观看", "$upcomingConcerts")
                if (uiState.isLoadingRates) {
                    StatItem("总花费", "计算中...")
                } else {
                    // 显示计算后的总花费
                    StatItem("总花费", "${getCurrencySymbol(uiState.selectedCurrency)}${String.format(Locale.getDefault(), "%.2f", uiState.totalSpent)}")
                }
            }
        }
        
        // 年度统计卡片
        if (concertsByYear.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "年度统计",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    concertsByYear.forEach { (year, count) ->
                        StatItem("${year}年", "${count}场")
                    }
                }
            }
        }
        
        // 分类统计卡片
        if (concertsByCategory.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "分类统计",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    concertsByCategory.forEach { (category, count) ->
                        StatItem(category, "${count}场")
                    }
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

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImprintScreenPreview() {
    ImprintScreen()
}