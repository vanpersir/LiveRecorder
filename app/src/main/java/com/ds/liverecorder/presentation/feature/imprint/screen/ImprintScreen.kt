package com.ds.liverecorder.presentation.feature.imprint.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ds.liverecorder.presentation.common.component.StatItem
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.feature.imprint.component.DataOverviewCard
import com.ds.liverecorder.presentation.viewmodel.ImprintViewModel
import java.util.Date

// 定义展示模式枚举
enum class ImprintDisplayMode {
    CALENDAR,
    MAP,
    STATISTICS
}

@Composable
fun ImprintScreen(
    viewModel: ImprintViewModel = concertViewModel()
) {
    val concerts by viewModel.concerts.collectAsStateWithLifecycle()
    viewModel.uiState
    
    // 当前展示模式状态
    var displayMode by remember { mutableStateOf(ImprintDisplayMode.STATISTICS) }
    
    // 货币单位选择状态

    // 支持的货币单位列表
    listOf("CNY", "USD", "EUR", "JPY", "HKD")
    
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
    LaunchedEffect(Unit) {
        viewModel.updateSelectedCurrency(viewModel.uiState.selectedCurrency)
    }
    
    // 页面加载时计算总花费
    LaunchedEffect(concerts) {
        if (concerts.isNotEmpty()) {
            viewModel.updateSelectedCurrency(viewModel.uiState.selectedCurrency)
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
        // 标题和模式选择区域
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
            
            // 模式选择按钮
            Row {
                IconButton(
                    onClick = { displayMode = ImprintDisplayMode.CALENDAR }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "日历模式",
                        tint = if (displayMode == ImprintDisplayMode.CALENDAR) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { displayMode = ImprintDisplayMode.MAP }
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "地图模式",
                        tint = if (displayMode == ImprintDisplayMode.MAP) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { displayMode = ImprintDisplayMode.STATISTICS }
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "统计模式",
                        tint = if (displayMode == ImprintDisplayMode.STATISTICS) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // 根据不同模式显示不同内容
        when (displayMode) {
            ImprintDisplayMode.CALENDAR -> {
                // TODO: 实现日历视图
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "日历模式",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text("日历视图内容待实现")
                    }
                }
            }
            
            ImprintDisplayMode.MAP -> {
                // TODO: 实现地图视图
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "地图模式",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text("地图视图内容待实现")
                    }
                }
            }
            
            ImprintDisplayMode.STATISTICS -> {
                // 数据概览卡片
                DataOverviewCard(
                    viewModel = viewModel,
                    totalConcerts = totalConcerts,
                    watchedConcerts = watchedConcerts,
                    upcomingConcerts = upcomingConcerts
                )
                
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
    }
}

@Preview(showBackground = true)
@Composable
fun ImprintScreenPreview() {
    ImprintScreen()
}