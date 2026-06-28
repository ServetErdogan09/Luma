package com.serveterdogan.lume.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate

private data class BottomNavItem(
    val route: String,
    val baseRoute: String,
    val label: String,
    val icon: ImageVector
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val todayDate = LocalDate.now().toString()



    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.Timeline.route,
            baseRoute = Screen.Timeline.route,
            label = stringResource(id = com.serveterdogan.lume.R.string.nav_timeline),
            icon = Icons.Outlined.Image
        ),
        BottomNavItem(
            route = Screen.Search.route,
            baseRoute = Screen.Search.route,
            label = stringResource(id = com.serveterdogan.lume.R.string.nav_search),
            icon = Icons.Outlined.Search
        ),
        BottomNavItem(
            route = Screen.DailyStory.createRoute(todayDate),
            baseRoute = Screen.DailyStory.route,
            label = stringResource(id = com.serveterdogan.lume.R.string.nav_daily_story),
            icon = Icons.Outlined.EventAvailable
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            baseRoute = Screen.Settings.route,
            label = stringResource(id = com.serveterdogan.lume.R.string.nav_settings),
            icon = Icons.Default.Settings
        )
    )

    Scaffold(
        bottomBar = {
            val isBottomBarScreen = bottomNavItems.any { it.baseRoute == currentRoute }
            if (isBottomBarScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                ) {
                    Spacer(modifier = Modifier.weight(0.2f))
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.baseRoute
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
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    softWrap = false
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.2f))
                }
            }
        }
    ) { innerPadding ->
        LumeNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}