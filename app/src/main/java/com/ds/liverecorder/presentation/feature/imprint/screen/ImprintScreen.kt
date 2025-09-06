package com.ds.liverecorder.presentation.feature.imprint.screen

import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.data.repository.ExchangeRateRepositoryImpl
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.viewmodel.ConcertListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImprintScreen(
    viewModel: ConcertListViewModel = concertViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val concerts = uiState.concerts
    
    // 汇率数据状态
    var exchangeRates by remember { mutableStateOf<Map<String, Double>?>(null) }
    var isLoadingRates by remember { mutableStateOf(true) }
    // 货币单位选择状态，默认使用CNY
    var selectedCurrency by remember { mutableStateOf("CNY") }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    
    // 支持的货币单位列表
    val supportedCurrencies = listOf("CNY", "USD", "EUR", "JPY", "HKD")
    
    // 统计数据
    val totalConcerts = concerts.size
    // 已观看：状态为正常，演出时间在当前时间之前
    val watchedConcerts = concerts.count { 
        it.status == "正常" && it.date.before(Date()) 
    }
    // 未观看：状态为正常，演出时间在当前时间之后
    val upcomingConcerts = concerts.count { 
        it.status == "正常" && it.date.after(Date()) 
    }
    
    // 获取汇率数据
    LaunchedEffect(selectedCurrency) {
        isLoadingRates = true
        val repository = ExchangeRateRepositoryImpl.getInstance()
        // 获取所有支持的货币相对于selectedCurrency的汇率
        exchangeRates = try {
            repository.getLatestRates(selectedCurrency, null)
        } catch (e: Exception) {
            Log.e("ImprintScreen", "Error fetching exchange rates", e)
            emptyMap()
        }
        
        // 打印调试信息
        Log.d("ImprintScreen", "Selected currency: $selectedCurrency")
        Log.d("ImprintScreen", "Exchange rates: $exchangeRates")
        
        isLoadingRates = false
    }
    
    // 按年份统计
    val concertsByYear = concerts
        .filter { 
            it.status == "正常" && it.date.before(Date()) 
        }
        .groupBy { SimpleDateFormat("yyyy", Locale.getDefault()).format(it.date) }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.first }
    
    // 按分类统计
    val concertsByCategory = concerts
        .filter { 
            it.status == "正常" && it.date.before(Date()) 
        }
        .groupBy { it.category }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

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
                    Text("单位: $selectedCurrency")
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
                                selectedCurrency = currency
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
                if (isLoadingRates) {
                    StatItem("总花费", "计算中...")
                } else {
                    // 计算总花费（转换为统一货币单位）
                    val totalSpent = calculateTotalSpent(concerts, exchangeRates, selectedCurrency)
                    Log.d("ImprintScreen", "Total spent in $selectedCurrency: $totalSpent")
                    StatItem("总花费", "${getCurrencySymbol(selectedCurrency)}${String.format(Locale.getDefault(), "%.2f", totalSpent)}")
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

/**
 * 计算总花费并转换为统一货币单位
 * @param concerts 演出列表
 * @param exchangeRates 汇率数据
 * @param targetCurrency 目标货币单位
 * @return 转换后的总金额
 */
fun calculateTotalSpent(
    concerts: List<com.ds.liverecorder.domain.model.Concert>,
    exchangeRates: Map<String, Double>?,
    targetCurrency: String
): Double {
    Log.d("ImprintScreen", "Calculating total spent. Target currency: $targetCurrency")
    Log.d("ImprintScreen", "Exchange rates: $exchangeRates")
    
    return concerts
        .filter { 
            it.status == "正常" && it.date.before(Date()) 
        }
        .sumOf { concert ->
            val amount = concert.actualPaid.toDoubleOrNull() ?: 0.0
            val sourceCurrency = concert.actualPaidCurrency.uppercase()
            
            Log.d("ImprintScreen", "Concert: ${concert.title}, Amount: $amount, Source currency: $sourceCurrency")
            
            // 如果源货币和目标货币相同，直接返回
            if (sourceCurrency == targetCurrency) {
                Log.d("ImprintScreen", "Same currency, returning amount: $amount")
                amount
            } else {
                // 尝试使用汇率转换
                // exchangeRates是以targetCurrency为基准的汇率数据
                // 例如当targetCurrency为CNY时，rates["USD"]表示1 CNY = ? USD
                val rate = exchangeRates?.get(sourceCurrency)
                Log.d("ImprintScreen", "Rate from exchangeRates[$sourceCurrency]: $rate")
                
                if (rate != null && rate > 0) {
                    // 如果rate存在且大于0，说明可以直接使用
                    // 根据汇率定义：1 targetCurrency = rate sourceCurrency
                    // 所以 amount sourceCurrency = amount / rate targetCurrency
                    val convertedAmount = amount / rate
                    Log.d("ImprintScreen", "Converted amount: $amount / $rate = $convertedAmount")
                    convertedAmount
                } else {
                    // 如果没有找到直接汇率，按1:1计算
                    Log.d("ImprintScreen", "No rate found, using amount as is: $amount")
                    amount
                }
            }
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