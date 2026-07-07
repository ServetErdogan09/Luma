package com.serveterdogan.lume

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.serveterdogan.lume.domain.repository.DailySummaryRepository
import com.serveterdogan.lume.domain.repository.SettingsRepository
import com.serveterdogan.lume.navigation.MainScreen
import com.serveterdogan.lume.theme.LumeTheme
import com.serveterdogan.lume.util.scheduleStoryNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dailySummaryRepository: DailySummaryRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    /** Android 13+ bildirim izni talep et */
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                scheduleStoryNotification(this)
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Temizlik: Bugünden önceki eski özetleri sil
        lifecycleScope.launch {
            dailySummaryRepository.deleteOldSummaries(LocalDate.now())
        }

        // Bildirim izni iste (Android 13+)
        requestNotificationPermissionIfNeeded()

        setContent {
            val isDarkMode by settingsRepository.themeModeFlow.collectAsState(initial = true)

            androidx.compose.runtime.LaunchedEffect(isDarkMode) {
                val surfaceColor = if (isDarkMode) {
                    com.serveterdogan.lume.theme.DarkSurface.toArgb()
                } else {
                    com.serveterdogan.lume.theme.LightSurface.toArgb()
                }
                
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
                        androidx.activity.SystemBarStyle.dark(surfaceColor)
                    } else {
                        androidx.activity.SystemBarStyle.light(surfaceColor, surfaceColor)
                    },
                    navigationBarStyle = if (isDarkMode) {
                        androidx.activity.SystemBarStyle.dark(surfaceColor)
                    } else {
                        androidx.activity.SystemBarStyle.light(surfaceColor, surfaceColor)
                    }
                )
            }

            LumeTheme(darkTheme = isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        settingsRepository = settingsRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    scheduleStoryNotification(this)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        } else {
            // Android 13 öncesi izin gerekmez
            scheduleStoryNotification(this)
        }
    }
}
