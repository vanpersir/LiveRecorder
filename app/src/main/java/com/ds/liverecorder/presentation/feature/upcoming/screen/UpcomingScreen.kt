package com.ds.liverecorder.presentation.feature.upcoming.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.feature.record.component.ConcertCard
import com.ds.liverecorder.presentation.feature.record.component.ConcertPoster
import com.ds.liverecorder.presentation.feature.record.component.ConcertTimelineItem
import com.ds.liverecorder.presentation.viewmodel.ConcertListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    viewModel: ConcertListViewModel = concertViewModel(),
    onNavigateToDetail: (Concert) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val displayMode by viewModel.upcomingDisplayMode.collectAsState() // 从 ViewModel 获取 upcomingDisplayMode
    
    // 只显示即将到来的演出
    val upcomingConcerts = uiState.concerts.filter { concert ->
        concert.date.after(java.util.Date())
    }

    var showModeDropdown by rememberSaveable { mutableStateOf(false) } // 控制下拉框显示状态
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("待看演出") },
            actions = {
                IconButton(onClick = { showModeDropdown = true }) {
                    when (displayMode) {
                        "card" -> Icon(Icons.Filled.GridView, contentDescription = "卡片视图")
                        "timeline" -> Icon(Icons.Filled.ViewModule, contentDescription = "时间线视图")
                        "poster" -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = "海报视图")
                        else -> Icon(Icons.Filled.GridView, contentDescription = "卡片视图")
                    }
                }
                
                // 下拉菜单
                DropdownMenu(
                    expanded = showModeDropdown,
                    onDismissRequest = { showModeDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("卡片视图") },
                        onClick = { 
                            viewModel.setUpcomingDisplayMode("card") // 通过 ViewModel 设置 upcomingDisplayMode
                            showModeDropdown = false
                        },
                        leadingIcon = { Icon(Icons.Filled.GridView, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("时间线视图") },
                        onClick = { 
                            viewModel.setUpcomingDisplayMode("timeline") // 通过 ViewModel 设置 upcomingDisplayMode
                            showModeDropdown = false
                        },
                        leadingIcon = { Icon(Icons.Filled.ViewModule, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("海报视图") },
                        onClick = { 
                            viewModel.setUpcomingDisplayMode("poster") // 通过 ViewModel 设置 upcomingDisplayMode
                            showModeDropdown = false
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Text(
                    text = "加载中...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else if (upcomingConcerts.isEmpty()) {
                Text(
                    text = "暂无待看演出",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                when (displayMode) {
                    "timeline" -> {
                        LazyColumn {
                            items(upcomingConcerts) { concert ->
                                ConcertTimelineItem(
                                    title = concert.title,
                                    venue = concert.venue,
                                    date = concert.date,
                                    onClick = { onNavigateToDetail(concert) }
                                )
                            }
                        }
                    }
                    "card" -> {
                        LazyColumn {
                            items(upcomingConcerts) { concert ->
                                ConcertCard(
                                    title = concert.title,
                                    venue = concert.venue,
                                    date = concert.date,
                                    posterPath = concert.posterPath,
                                    onClick = { onNavigateToDetail(concert) }
                                )
                            }
                        }
                    }
                    "poster" -> {
                        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                            items(upcomingConcerts) { concert ->
                                ConcertPoster(
                                    title = concert.title,
                                    date = concert.date,
                                    venue = concert.venue,
                                    posterPath = concert.posterPath,
                                    onClick = { onNavigateToDetail(concert) }
                                )
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
fun UpcomingScreenPreview() {
    UpcomingScreen()
}