package com.serveterdogan.lume.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.serveterdogan.lume.ui.screen.MemoryViewModel
import com.serveterdogan.lume.ui.screen.search.SearchViewModel
import com.serveterdogan.lume.ui.screen.add_edit.AddEditMemoryScreen
import com.serveterdogan.lume.ui.screen.detail.MemoryDetailScreen
import com.serveterdogan.lume.ui.screen.gallery.TagGalleryScreen
import com.serveterdogan.lume.ui.screen.search.SearchScreen
import com.serveterdogan.lume.ui.screen.settings.SettingsScreen
import com.serveterdogan.lume.ui.screen.story.DailyStoryScreen
import com.serveterdogan.lume.ui.screen.archive.ArchiveScreen
import com.serveterdogan.lume.ui.screen.timeline.TimelineScreen
import com.serveterdogan.lume.ui.screen.home.HomeScreen

sealed class Screen(val route: String) {
    // 0. Splash ve Onboarding
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")

    // 1. Ana Ekran
    object Home : Screen("home_screen")

    // 2. Yeni Anı Ekleme / Düzenleme Ekranı
    object AddMemory : Screen("add_memory_screen")
    object EditMemory : Screen("edit_memory_screen/{memoryId}") {
        fun createRoute(memoryId: Int) = "edit_memory_screen/$memoryId"
    }

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
    
    // 6. Arşiv Ekranı
    object Timeline : Screen("timeline_screen")
    
    // 7. Aylık Arşiv (Timeline) Ekranı
    object MonthlyArchive : Screen("monthly_archive_screen/{monthYear}") {
        fun createRoute(monthYear: String) = "monthly_archive_screen/$monthYear"
    }

    // 8. Etiket Galerisi (tüm benzer anılar, sınırsız)
    object TagGallery : Screen("tag_gallery_screen/{memoryId}") {
        fun createRoute(memoryId: Int) = "tag_gallery_screen/$memoryId"
    }

    // 9. Timeline Gallery Screen (Bottom Navigation'ı gizlemek için yeni sayfa)
    object TimelineGallery : Screen("timeline_gallery_screen/{filterType}/{filterValue}") {
        fun createRoute(filterType: String, filterValue: String) = "timeline_gallery_screen/$filterType/$filterValue"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LumeNavGraph(
    navController: NavHostController,
    settingsRepository: com.serveterdogan.lume.domain.repository.SettingsRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ){
        composable(Screen.Splash.route) {
            com.serveterdogan.lume.ui.screen.splash.SplashScreen(
                navController = navController,
                settingsRepository = settingsRepository
            )
        }
        composable(Screen.Onboarding.route) {
            com.serveterdogan.lume.ui.screen.onboarding.OnboardingScreen(
                navController = navController,
                settingsRepository = settingsRepository
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.AddMemory.route) {
            AddEditMemoryScreen(navController = navController)
        }
        composable(
            route = Screen.EditMemory.route,
            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getInt("memoryId")
            AddEditMemoryScreen(
                navController = navController,
                memoryId = memoryId
            )
        }
        composable(route = Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(
            route = Screen.MemoryDetail.route,
            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getInt("memoryId") ?: 0
            val viewModel: MemoryViewModel = hiltViewModel()
            MemoryDetailScreen(
                navController = navController,
                memoryId = memoryId,
                viewModel = viewModel
            )
        }
        composable(
            route = Screen.DailyStory.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            DailyStoryScreen(
                navController = navController,
                dateStr = date
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Timeline.route) {
            TimelineScreen(navController = navController)
        }
        composable(Screen.MonthlyArchive.route) { backStackEntry ->
            val monthYear = backStackEntry.arguments?.getString("monthYear") ?: ""
            com.serveterdogan.lume.ui.screen.archive.MonthlyArchiveScreen(
                navController = navController,
                monthYear = monthYear
            )
        }
        composable(
            route = Screen.TagGallery.route,
            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getInt("memoryId") ?: -1
            TagGalleryScreen(
                memoryId = memoryId,
                navController = navController
            )
        }
        composable(
            route = Screen.TimelineGallery.route,
            arguments = listOf(
                navArgument("filterType") { type = NavType.StringType },
                navArgument("filterValue") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val filterType = backStackEntry.arguments?.getString("filterType") ?: ""
            val filterValue = backStackEntry.arguments?.getString("filterValue") ?: ""
            com.serveterdogan.lume.ui.screen.timeline.TimelineGalleryScreen(
                navController = navController,
                filterType = filterType,
                filterValue = filterValue
            )
        }
    }
}