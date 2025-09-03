package com.ds.liverecorder.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.presentation.screen.AddConcertScreen
import com.ds.liverecorder.presentation.screen.ConcertDetailScreen
import com.ds.liverecorder.presentation.screen.EditConcertScreen
import com.ds.liverecorder.presentation.screen.ImprintScreen
import com.ds.liverecorder.presentation.screen.RecordScreen
import com.ds.liverecorder.presentation.screen.SettingsScreen
import com.ds.liverecorder.presentation.screen.UpcomingScreen
import com.ds.liverecorder.presentation.component.concertViewModel
import com.ds.liverecorder.data.viewmodel.ConcertViewModel

@Composable
fun AppNavigation(
    navController: NavHostController, 
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (ConcertEntity) -> Unit = {},
    onNavigateToEdit: (ConcertEntity) -> Unit = {},
    viewModel: ConcertViewModel = concertViewModel()
) {
    
    NavHost(
        navController = navController,
        startDestination = Screen.Record.route,
        modifier = Modifier.padding(contentPadding)
    ) {
        composable(Screen.Record.route) { 
            RecordScreen(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail
            )
        }
        composable(Screen.Upcoming.route) { 
            UpcomingScreen(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail
            )
        }
        composable(Screen.Add.route) { 
            AddConcertScreen(
                onBack = { navController.popBackStack() },
                onSave = { concert ->
                    viewModel.insertConcert(concert)
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Imprint.route) { 
            ImprintScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) { 
            SettingsScreen()
        }
        composable(
            route = Screen.ConcertDetail.route,
            arguments = listOf(navArgument("concertId") { type = NavType.LongType })
        ) { backStackEntry ->
            val concertId = backStackEntry.arguments?.getLong("concertId") ?: 0L
            ConcertDetailScreen(
                concertId = concertId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = onNavigateToEdit,
                onDelete = { concertId ->
                    viewModel.deleteConcertById(concertId)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.EditConcert.route,
            arguments = listOf(navArgument("concertId") { type = NavType.LongType })
        ) { backStackEntry ->
            val concertId = backStackEntry.arguments?.getLong("concertId") ?: 0L
            EditConcertScreen(
                concertId = concertId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onUpdate = { concert ->
                    viewModel.updateConcert(concert)
                    navController.popBackStack()
                }
            )
        }
    }
}