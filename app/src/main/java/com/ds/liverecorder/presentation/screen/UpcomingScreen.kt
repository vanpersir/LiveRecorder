package com.ds.liverecorder.presentation.screen

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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.viewmodel.ConcertViewModel
import com.ds.liverecorder.presentation.component.ConcertCard
import com.ds.liverecorder.presentation.component.ConcertPoster
import com.ds.liverecorder.presentation.component.ConcertTimelineItem
import com.ds.liverecorder.presentation.component.concertViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    viewModel: ConcertViewModel = concertViewModel(),
    onNavigateToDetail: (ConcertEntity) -> Unit = {}
) {
    val upcomingConcerts by viewModel.getUpcomingConcerts().collectAsState(initial = emptyList())

    var displayMode by remember { mutableStateOf("card") } // timeline, card, poster - 统一默认为卡片视图
    var showModeDropdown by remember { mutableStateOf(false) } // 控制下拉框显示状态
    
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
                        "poster" -> Icon(Icons.Filled.List, contentDescription = "海报视图")
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
                            displayMode = "card"
                            showModeDropdown = false
                        },
                        leadingIcon = { Icon(Icons.Filled.GridView, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("时间线视图") },
                        onClick = { 
                            displayMode = "timeline"
                            showModeDropdown = false
                        },
                        leadingIcon = { Icon(Icons.Filled.ViewModule, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("海报视图") },
                        onClick = { 
                            displayMode = "poster"
                            showModeDropdown = false
                        },
                        leadingIcon = { Icon(Icons.Filled.List, contentDescription = null) }
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (upcomingConcerts.isEmpty()) {
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
                                    status = concert.status,
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
                                    status = concert.status,
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
                                    status = concert.status,
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