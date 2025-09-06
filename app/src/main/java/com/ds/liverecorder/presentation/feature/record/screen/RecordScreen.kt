package com.ds.liverecorder.presentation.feature.record.screen

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.ds.liverecorder.presentation.common.component.ConcertCard
import com.ds.liverecorder.presentation.common.component.ConcertPoster
import com.ds.liverecorder.presentation.common.component.ConcertTimelineItem
import com.ds.liverecorder.presentation.common.component.SearchBar
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.viewmodel.ConcertListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: ConcertListViewModel = concertViewModel(),
    onNavigateToDetail: (Concert) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var displayMode by rememberSaveable { mutableStateOf("card") } // card, timeline, poster
    var filterStatus by rememberSaveable { mutableStateOf("全部") } // 全部, 正常, 待开票, 已取消, 未赴约
    var searchQuery by rememberSaveable { mutableStateOf("") } // 搜索关键词
    var showModeDropdown by rememberSaveable { mutableStateOf(false) } // 控制下拉框显示状态
    var showSearchBar by rememberSaveable { mutableStateOf(false) } // 控制搜索栏显示状态
    
    // 根据筛选条件过滤演出列表
    val filteredConcerts = uiState.concerts
        .filter { 
            // 状态筛选
            (filterStatus == "全部" || it.status == filterStatus) &&
            // 搜索筛选
            (searchQuery.isEmpty() || 
             it.title.contains(searchQuery, ignoreCase = true) || 
             it.venue.contains(searchQuery, ignoreCase = true) ||
             it.category.contains(searchQuery, ignoreCase = true) ||
             it.performers.any { performer -> performer.contains(searchQuery, ignoreCase = true) } ||
             it.guests.any { guest -> guest.contains(searchQuery, ignoreCase = true) })
        }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("演出记录") },
            actions = {
                // 搜索图标按钮
                IconButton(onClick = { showSearchBar = !showSearchBar }) {
                    Icon(Icons.Filled.Search, contentDescription = "搜索")
                }
                
                IconButton(onClick = { 
                    filterStatus = when (filterStatus) {
                        "全部" -> "正常"
                        "正常" -> "待开票"
                        "待开票" -> "已取消"
                        "已取消" -> "未赴约"
                        "未赴约" -> "全部"
                        else -> "全部"
                    }
                }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "筛选")
                }
                
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
            // 下拉式搜索栏
            if (showSearchBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    SearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "搜索演出、场地、演出者..."
                    )
                }
            }
            
            // 显示当前筛选状态
            if (filterStatus != "全部") {
                Text(
                    text = "筛选: $filterStatus",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            
            if (uiState.isLoading) {
                Text(
                    text = "加载中...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else if (filteredConcerts.isEmpty()) {
                Text(
                    text = "暂无演出记录",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                when (displayMode) {
                    "card" -> {
                        LazyColumn {
                            items(filteredConcerts) { concert ->
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
                    "timeline" -> {
                        LazyColumn {
                            items(filteredConcerts) { concert ->
                                ConcertTimelineItem(
                                    title = concert.title,
                                    venue = concert.venue,
                                    date = concert.date,
                                    onClick = { onNavigateToDetail(concert) }
                                )
                            }
                        }
                    }
                    "poster" -> {
                        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                            items(filteredConcerts) { concert ->
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
fun RecordScreenPreview() {
    RecordScreen()
}