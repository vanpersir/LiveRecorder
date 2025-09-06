package com.ds.liverecorder.presentation.feature.imprint.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.viewmodel.ConcertListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ImprintScreen(viewModel: ConcertListViewModel = concertViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val concerts = uiState.concerts
    
    // 统计数据
    val totalConcerts = concerts.size
    val watchedConcerts = concerts.count { it.status == "已看" }
    val upcomingConcerts = concerts.count { it.status == "待看" }
    val totalSpent = concerts.filter { it.status == "已看" }
        .sumOf { it.actualPaid.toDoubleOrNull() ?: 0.0 }
    
    // 按年份统计
    val concertsByYear = concerts.filter { it.status == "已看" }
        .groupBy { SimpleDateFormat("yyyy", Locale.getDefault()).format(it.date) }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.first }
    
    // 按分类统计
    val concertsByCategory = concerts.filter { it.status == "已看" }
        .groupBy { it.category }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "印记",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
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
                StatItem("总花费", "¥${String.format("%.2f", totalSpent)}")
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

@Composable
fun StatItem(label: String, value: String) {
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