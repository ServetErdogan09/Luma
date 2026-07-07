package com.serveterdogan.lume.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate

private data class BottomNavItem(
    val route: String,
    val baseRoute: String,
    val label: String,
    val icon: ImageVector,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    settingsRepository: com.serveterdogan.lume.domain.repository.SettingsRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val todayDate = LocalDate.now().toString()

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.Home.route,
            baseRoute = Screen.Home.route,
            label = "Ana Sayfa",
            icon = Icons.Outlined.Home,
        ),
        BottomNavItem(
            route = Screen.Timeline.route,
            baseRoute = Screen.Timeline.route,
            label = "Zaman Tüneli",
            icon = Icons.Outlined.Timeline,
        ),
        BottomNavItem(
            route = Screen.AddMemory.route,
            baseRoute = Screen.AddMemory.route,
            label = "Ekle",
            icon = Icons.Default.Add
        ),
        BottomNavItem(
            route = Screen.DailyStory.createRoute(todayDate),
            baseRoute = Screen.DailyStory.route,
            label = stringResource(id = com.serveterdogan.lume.R.string.nav_daily_story),
            icon = Icons.Outlined.AutoStories
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            baseRoute = Screen.Settings.route,
            label = "Ayarlar",
            icon = Icons.Default.Settings,
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val isBottomBarScreen =
                bottomNavItems.any { currentRoute?.startsWith(it.baseRoute) == true }
            if (isBottomBarScreen) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            thickness = 0.8.dp
                        )
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            bottomNavItems.forEach { item ->
                                val isSelected =
                                    currentRoute?.startsWith(item.baseRoute) == true
                                val isAddButton = item.baseRoute == Screen.AddMemory.route

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (currentRoute != item.baseRoute) {
                                            navController.navigate(item.route) {
                                                popUpTo(Screen.Timeline.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        if (isAddButton) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.label,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label
                                            )
                                        }
                                    },
                                    label = if (isAddButton) null else {
                                        {
                                            Text(
                                                text = item.label,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                softWrap = false,
                                                fontSize = 10.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = if (isAddButton) Color.White else MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = if (isAddButton) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        ),
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        ),
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }


                } // this closes the Box
            }
        }
    ) { innerPadding ->
        LumeNavGraph(
            navController = navController,
            settingsRepository = settingsRepository,
            modifier = Modifier.padding(innerPadding)
        )
    }
}