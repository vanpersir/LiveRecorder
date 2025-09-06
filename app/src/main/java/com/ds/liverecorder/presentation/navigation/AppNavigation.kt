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
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.feature.add.screen.AddConcertScreen
import com.ds.liverecorder.presentation.feature.add.screen.ConcertDetailScreen
import com.ds.liverecorder.presentation.feature.add.screen.EditConcertScreen
import com.ds.liverecorder.presentation.feature.imprint.screen.ImprintScreen
import com.ds.liverecorder.presentation.feature.record.screen.RecordScreen
import com.ds.liverecorder.presentation.feature.settings.screen.SettingsScreen
import com.ds.liverecorder.presentation.feature.upcoming.screen.UpcomingScreen
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.viewmodel.AddConcertViewModel
import com.ds.liverecorder.presentation.viewmodel.ConcertDetailViewModel
import com.ds.liverecorder.presentation.viewmodel.ConcertListViewModel

@Composable
fun AppNavigation(
    navController: NavHostController, 
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (Concert) -> Unit = {},
    onNavigateToEdit: (Concert) -> Unit = {}
) {
    
    NavHost(
        navController = navController,
        startDestination = Screen.Record.route,
        modifier = Modifier.padding(contentPadding)
    ) {
        composable(Screen.Record.route) { 
            val viewModel: ConcertListViewModel = concertViewModel()
            RecordScreen(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail
            )
        }
        composable(Screen.Upcoming.route) { 
            val viewModel: ConcertListViewModel = concertViewModel()
            UpcomingScreen(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail
            )
        }
        composable(Screen.Add.route) { 
            val viewModel: AddConcertViewModel = concertViewModel()
            AddConcertScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSave = { concert ->
                    viewModel.addConcert(concert)
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Imprint.route) { 
            val viewModel: ConcertListViewModel = concertViewModel()
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
            val viewModel: ConcertDetailViewModel = concertViewModel()
            ConcertDetailScreen(
                concertId = concertId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = onNavigateToEdit,
                onDelete = { concertId ->
                    viewModel.deleteConcert(concertId)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.EditConcert.route,
            arguments = listOf(navArgument("concertId") { type = NavType.LongType })
        ) { backStackEntry ->
            val concertId = backStackEntry.arguments?.getLong("concertId") ?: 0L
            val detailViewModel: ConcertDetailViewModel = concertViewModel()
            val addViewModel: AddConcertViewModel = concertViewModel()
            EditConcertScreen(
                concertId = concertId,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onUpdate = { concert ->
                    addViewModel.updateConcert(concert)
                    navController.popBackStack()
                }
            )
        }
    }
}