package com.example.coollauncher

import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coollauncher.data.AppRepository
import com.example.coollauncher.data.GroupsRepository
import com.example.coollauncher.data.SettingsRepository
import com.example.coollauncher.ui.LauncherScreen
import com.example.coollauncher.ui.SettingsScreen
import com.example.coollauncher.ui.components.LauncherSplashScreen
import com.example.coollauncher.ui.theme.CoolLauncherTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CoolLauncher"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Log uncaught crashes with our tag so they're easy to find in Logcat
        val prevHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Log.e(TAG, "Uncaught exception in thread ${t.name}", e)
            prevHandler?.uncaughtException(t, e)
        }
        enableEdgeToEdge()

        val appRepository = AppRepository(applicationContext)
        val groupsRepository = GroupsRepository(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)
        val viewModelFactory = LauncherViewModelFactory(appRepository, groupsRepository)

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            var showSettings by remember { mutableStateOf(false) }
            var hasCompletedTour by remember { mutableStateOf(settingsRepository.hasCompletedTour) }
            var themeMode by remember { mutableStateOf(settingsRepository.themeMode) }
            var fontKey by remember { mutableStateOf(settingsRepository.fontKey) }
            var colorThemeKey by remember { mutableStateOf(settingsRepository.colorThemeKey) }
            var hardMinimalMode by remember { mutableStateOf(settingsRepository.hardMinimalMode) }

            val darkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            CoolLauncherTheme(darkTheme = darkTheme, fontKey = fontKey, colorThemeKey = colorThemeKey, hardMinimalMode = hardMinimalMode) {
                val viewModel = viewModel<LauncherViewModel>(factory = viewModelFactory)
                val uiState by viewModel.uiState.collectAsState()
                var splashAnimationDone by remember { mutableStateOf(false) }
                LaunchedEffect(splashAnimationDone, uiState.isLoading) {
                    if (splashAnimationDone && !uiState.isLoading) showSplash = false
                }
                if (showSplash) {
                    LauncherSplashScreen(
                        onAnimationEnd = { splashAnimationDone = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (showSettings) {
                    SettingsScreen(
                        onBack = { showSettings = false },
                        fontKey = fontKey,
                        colorThemeKey = colorThemeKey,
                        hardMinimalMode = hardMinimalMode,
                        onFontKeyChange = {
                            fontKey = it
                            settingsRepository.fontKey = it
                        },
                        onColorThemeChange = {
                            colorThemeKey = it
                            settingsRepository.colorThemeKey = it
                        },
                        onHardMinimalModeChange = {
                            hardMinimalMode = it
                            settingsRepository.hardMinimalMode = it
                        },
                        onRequestShowTour = {
                            hasCompletedTour = false
                            settingsRepository.hasCompletedTour = false
                            showSettings = false
                        },
                        onResetSettings = {
                            settingsRepository.resetToDefaults()
                            themeMode = settingsRepository.themeMode
                            fontKey = settingsRepository.fontKey
                            colorThemeKey = settingsRepository.colorThemeKey
                            hardMinimalMode = settingsRepository.hardMinimalMode
                            hasCompletedTour = settingsRepository.hasCompletedTour
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LauncherScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                        onOpenSettings = { showSettings = true },
                        showTour = !hasCompletedTour,
                        onTourComplete = {
                            hasCompletedTour = true
                            settingsRepository.hasCompletedTour = true
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Never open the default launcher chooser (MAIN+HOME) from here — it brings the system
        // chooser to the front and our UI goes blank. Default launcher is set only from
        // Settings (gear) -> "Set as default launcher" which opens system Default apps.
    }
}
