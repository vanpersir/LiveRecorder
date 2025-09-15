package com.ds.liverecorder.presentation.feature.detail.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.R
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.common.component.PerformerTag
import com.ds.liverecorder.presentation.common.component.PosterImage
import com.ds.liverecorder.presentation.common.component.RatingBar
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.viewmodel.ConcertDetailViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcertDetailScreen(
    concertId: Long,
    viewModel: ConcertDetailViewModel = concertViewModel(),
    onBack: () -> Unit = {},
    onEdit: (Concert) -> Unit = {},
    onDelete: (Long) -> Unit = {}
) {
    // 加载演出数据
    viewModel.loadConcert(concertId)
    val uiState by viewModel.uiState.collectAsState()
    
    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 处理删除操作
    val handleDelete = { concertId: Long ->
        uiState.concert?.posterPath?.let { posterPath ->
            try {
                // 删除海报文件
                val posterFile = File(posterPath)
                if (posterFile.exists()) {
                    posterFile.delete()
                }
            } catch (e: Exception) {
                // 忽略删除文件时的异常
                e.printStackTrace()
            }
        }
        onDelete(concertId)
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除演出") },
            text = { Text("确定要删除这场演出吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.concert?.id?.let { id ->
                            handleDelete(id)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = uiState.concert?.title ?: "演出详情",
                    modifier = Modifier.basicMarquee(),
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { uiState.concert?.let { onEdit(it) } }) {
                    Icon(Icons.Filled.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除")
                }
            }
        )
        
        uiState.concert?.let { 
            ConcertDetailContent(concert = it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun ConcertDetailContent(concert: Concert) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 海报
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            PosterImage(
                imageUrl = concert.posterPath,
                contentDescription = "演出海报",
                modifier = Modifier
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit // 完整展示海报
            )
        }
        
        // 演出标题
        Text(
            text = concert.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
        )
        
        // 日期和地点信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 日期
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                        contentDescription = "日期图标",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = SimpleDateFormat("yyyy-MM-dd HH:mm EEEE", Locale.getDefault()).format(concert.date),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // 地点
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                        contentDescription = "地点图标",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = concert.venue,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // 票价信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "票价信息",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 分开展示票价信息，使用更美观的格式
                DetailItem("票价：", "${formatPrice(concert.ticketPrice)} ${concert.ticketPriceCurrency}")
                DetailItem("实付：", "${formatPrice(concert.actualPaid)} ${concert.actualPaidCurrency}")
                DetailItem("其他：", "${formatPrice(concert.otherFees)} ${concert.otherFeesCurrency}")
            }
        }
        
        // 评分
        if (concert.rating > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "我的评价",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    RatingBar(
                        rating = concert.rating,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        
        // 阵容
        if (concert.performers.isNotEmpty()) {
            SectionTitle("阵容")

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                concert.performers.forEach { performer ->
                    PerformerTag(
                        name = performer,
                        modifier = Modifier
                            .padding(end = 4.dp, bottom = 4.dp)
                    )
                }
            }
        }
        
        // 嘉宾
        if (concert.guests.isNotEmpty()) {
            SectionTitle("嘉宾")

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                concert.guests.forEach { guest ->
                    PerformerTag(
                        name = guest,
                        modifier = Modifier
                            .padding(end = 4.dp, bottom = 4.dp)
                    )
                }
            }
        }
        
        // 备注
        if (concert.notes.isNotEmpty()) {
            SectionTitle("备注")
            
            DetailItem("备注", concert.notes)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConcertDetailScreenPreview() {
    val concert = Concert(
        id = 1,
        title = "演出名称",
        venue = "演出场地",
        date = Date(),
        notes = "这是一场精彩的演出",
        posterResId = 0,
        posterPath = "",
        ticketPrice = "100",
        ticketPriceCurrency = "CNY",
        actualPaid = "80",
        actualPaidCurrency = "CNY",
        otherFees = "10",
        otherFeesCurrency = "CNY",
        performers = listOf("表演者1", "表演者2"),
        guests = listOf("嘉宾1"),
        status = "待看",
        category = "演唱会",
        rating = 4
    )
    
    ConcertDetailContent(concert = concert)
}

// 添加价格格式化函数
fun formatPrice(price: String): String {
    return try {
        if (price.isBlank()) {
            "0.00"
        } else {
            val priceValue = price.toDoubleOrNull()
            if (priceValue == null) {
                "0.00"
            } else {
                "%.2f".format(priceValue)
            }
        }
    } catch (_: Exception) {
        "0.00"
    }
}