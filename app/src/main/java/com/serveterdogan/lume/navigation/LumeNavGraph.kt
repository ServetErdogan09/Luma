package com.serveterdogan.lume.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.serveterdogan.lume.ui.screen.add_edit.AddEditMemoryScreen
import com.serveterdogan.lume.ui.screen.detail.MemoryDetailScreen
import com.serveterdogan.lume.ui.screen.search.SearchScreen
import com.serveterdogan.lume.ui.screen.story.DailyStoryScreen
import com.serveterdogan.lume.ui.screen.timeline.TimelineScreen
import com.serveterdogan.lume.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String) {
    // 1. Ana Ekran
    object Timeline : Screen("timeline_screen")

    // 2. Yeni Anı Ekleme Ekranı
    object AddMemory : Screen("add_memory_screen")

    // 3. Arama ve Galeri Ekranı
    object Search : Screen("search_screen")

    // 4. Detay Ekranı
    object MemoryDetail : Screen("memory_detail_screen/{memoryId}") {
        fun createRoute(memoryId: Int) = "memory_detail_screen/$memoryId"
    }
    //
    object DailyStory : Screen("daily_story_screen/{date}") {
        fun createRoute(date: String) = "daily_story_screen/$date"
    }

    // 5. Ayarlar Ekranı
    object Settings : Screen("settings_screen")
}

@Composable
fun LumeNavGraph(navController: NavHostController , modifier: Modifier = Modifier) {

    NavHost(
        navController = navController,
        startDestination = Screen.Timeline.route
    ){
        composable(Screen.Timeline.route) {
            TimelineScreen(navController = navController)
        }
        composable(Screen.AddMemory.route) {
            AddEditMemoryScreen(navController = navController)
        }
        composable(Screen.Search.route) {
             SearchScreen(navController = navController)
        }
        composable(
            route = Screen.MemoryDetail.route,
            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
        ) {
            MemoryDetailScreen(navController = navController)
        }
        composable(
            route = Screen.DailyStory.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) {
            DailyStoryScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}