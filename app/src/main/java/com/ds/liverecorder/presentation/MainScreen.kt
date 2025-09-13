package com.ds.liverecorder.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.navigation.AppNavigation
import com.ds.liverecorder.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Record.route

    // 导航回调函数
    val onNavigateToDetail: (Concert) -> Unit = { concert ->
        navController.navigate(Screen.ConcertDetail.createRoute(concert.id))
    }
    
    val onNavigateToEdit: (Concert) -> Unit = { concert ->
        navController.navigate(Screen.EditConcert.createRoute(concert.id))
    }
    
    val onNavigateToAdd: () -> Unit = {
        navController.navigate(Screen.Add.route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }
    
    // 根据当前路由确定选中的导航项
    var selectedItem by rememberSaveable { 
        mutableIntStateOf(
            when (currentRoute) {
                Screen.Record.route -> 0
                Screen.Upcoming.route -> 1
                Screen.Add.route -> 2
                Screen.Imprint.route -> 3
                Screen.Settings.route -> 4
                else -> 0
            }
        ) 
    }
    
    // 当路由改变时更新选中项
    selectedItem = when (currentRoute) {
        Screen.Record.route -> 0
        Screen.Upcoming.route -> 1
        Screen.Add.route -> 2
        Screen.Imprint.route -> 3
        Screen.Settings.route -> 4
        else -> selectedItem
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("记录现场") },
                    selected = selectedItem == 0,
                    onClick = { 
                        navController.navigate(Screen.Record.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                    label = { Text("待看") },
                    selected = selectedItem == 1,
                    onClick = { 
                        navController.navigate(Screen.Upcoming.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    label = { Text("添加") },
                    selected = selectedItem == 2,
                    onClick = { 
                        selectedItem = 2
                        onNavigateToAdd()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    label = { Text("印记") },
                    selected = selectedItem == 3,
                    onClick = { 
                        navController.navigate(Screen.Imprint.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("设置") },
                    selected = selectedItem == 4,
                    onClick = { 
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
    ) { innerPadding ->
        // 使用导航系统并应用内边距
        AppNavigation(
            navController = navController,
            contentPadding = innerPadding,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToEdit = onNavigateToEdit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}