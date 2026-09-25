package com.instashow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.instashow.AppContainer
import com.instashow.R
import com.instashow.auth.AuthViewModel
import com.instashow.auth.UserSession
import com.instashow.settings.Settings
import com.instashow.story.StoryViewModel
import com.instashow.ui.theme.Ink
import com.instashow.ui.theme.Lime
import kotlinx.coroutines.launch

object Routes {
    const val StoryGraph = "story"
    const val Home = "home"
    const val Preview = "preview"
    const val About = "about"
}

@Composable
fun InstashowApp(container: AppContainer) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(container.authRepository))
    val ready by authViewModel.ready.collectAsStateWithLifecycle()
    val session by authViewModel.session.collectAsStateWithLifecycle()
    val signedIn by authViewModel.signedInMessage.collectAsStateWithLifecycle()
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val signedInText = stringResource(R.string.signed_in)

    CompositionLocalProvider(LocalAppSnackbar provides snackbar) {
        LaunchedEffect(signedIn) {
            if (signedIn) {
                snackbar.showSnackbar(signedInText)
                authViewModel.consumeSignedInMessage()
            }
        }
        val currentSettings: Settings? = settings
        when {
            !ready || currentSettings == null -> SessionLoader()
            session != null || currentSettings.onboarded -> StoryNavHost(
                container = container,
                authViewModel = authViewModel,
                session = session,
            )
            else -> WelcomeScreen(
                viewModel = authViewModel,
                onGetStarted = { scope.launch { container.settingsRepository.setOnboarded(true) } },
            )
        }
    }
}

@Composable
private fun SessionLoader() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Lime)
    }
}

@Composable
private fun StoryNavHost(
    container: AppContainer,
    authViewModel: AuthViewModel,
    session: UserSession?,
) {
    val navController = rememberNavController()
    val factory = StoryViewModel.factory(
        healthRepository = container.healthRepository,
        templateCatalog = container.templateCatalog,
        photoStore = container.photoStore,
        storyRenderer = container.storyRenderer,
        settingsRepository = container.settingsRepository,
    )
    NavHost(navController = navController, startDestination = Routes.StoryGraph) {
        navigation(startDestination = Routes.Home, route = Routes.StoryGraph) {
            composable(Routes.Home) {
                HomeScreen(
                    viewModel = storyViewModel(navController, factory),
                    authViewModel = authViewModel,
                    session = session,
                    onAbout = { navController.navigate(Routes.About) },
                    onPhotoReady = { navController.navigate(Routes.Preview) { launchSingleTop = true } },
                )
            }
            composable(Routes.About) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Preview) {
                PreviewScreen(
                    viewModel = storyViewModel(navController, factory),
                    storyShare = container.storyShare,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun storyViewModel(
    navController: NavHostController,
    factory: androidx.lifecycle.ViewModelProvider.Factory,
): StoryViewModel {
    val owner = remember(navController) { navController.getBackStackEntry(Routes.StoryGraph) }
    return viewModel(viewModelStoreOwner = owner, factory = factory)
}
