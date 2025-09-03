package com.ds.liverecorder.presentation.navigation

sealed class Screen(val route: String) {
    object Record : Screen("record")
    object Upcoming : Screen("upcoming")
    object Add : Screen("add")
    object Imprint : Screen("imprint")
    object Settings : Screen("settings")
    object ConcertDetail : Screen("concert_detail/{concertId}") {
        fun createRoute(concertId: Long) = "concert_detail/$concertId"
    }
    object EditConcert : Screen("edit_concert/{concertId}") {
        fun createRoute(concertId: Long) = "edit_concert/$concertId"
    }
}