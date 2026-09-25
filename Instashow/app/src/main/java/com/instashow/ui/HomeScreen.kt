package com.instashow.ui

import com.instashow.ui.theme.Line
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.FlowRow
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.contracts.ExerciseRouteRequestContract
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instashow.R
import com.instashow.auth.AuthViewModel
import com.instashow.auth.UserSession
import com.instashow.health.HealthAvailability
import com.instashow.health.HealthRepository
import com.instashow.health.HealthSnapshot
import com.instashow.health.StatFormat
import com.instashow.health.Workout
import com.instashow.health.WorkoutNames
import com.instashow.health.openHealthConnectInstall
import com.instashow.settings.Settings
import com.instashow.story.PhotoProblem
import com.instashow.story.StoryData
import com.instashow.story.StoryViewModel
import com.instashow.ui.theme.Ember
import com.instashow.ui.theme.Ink
import com.instashow.ui.theme.Lime
import com.instashow.ui.theme.PrimaryButton
import com.instashow.ui.theme.SecondaryButton
import com.instashow.ui.theme.Surface
import com.instashow.ui.theme.SurfaceHigh
import com.instashow.ui.theme.TextMuted
import com.instashow.ui.theme.TextPrimary
import com.instashow.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StoryViewModel,
    authViewModel: AuthViewModel,
    session: UserSession?,
    onAbout: () -> Unit,
    onPhotoReady: () -> Unit,
) {
    val health by viewModel.health.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val importing by viewModel.importing.collectAsStateWithLifecycle()
    val problem by viewModel.photoProblem.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalAppSnackbar.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraMissing = stringResource(R.string.photo_camera_missing)
    val photoUnreadable = stringResource(R.string.photo_unreadable)
    var refreshing by remember { mutableStateOf(false) }
    var wasImporting by remember { mutableStateOf(false) }
    var captureUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var photoSheetFor by rememberSaveable { mutableStateOf<String?>(null) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var pendingRouteId by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = captureUri
        if (saved && uri != null) viewModel.importPhoto(uri)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importPhoto(uri)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { viewModel.refreshHealth() }
    val scope = rememberCoroutineScope()
    val routeLauncher = rememberLauncherForActivityResult(ExerciseRouteRequestContract()) { route ->
        val id = pendingRouteId
        pendingRouteId = null
        if (id != null && route != null) viewModel.onRouteConsent(id, HealthRepository.simplify(route))
        if (id != null) photoSheetFor = id
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshHealth()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(health.loading) {
        if (!health.loading) refreshing = false
    }
    LaunchedEffect(problem) {
        val current = problem ?: return@LaunchedEffect
        snackbar.showSnackbar(if (current == PhotoProblem.CameraMissing) cameraMissing else photoUnreadable)
        viewModel.clearPhotoProblem()
    }
    LaunchedEffect(importing, problem) {
        val finishedImport = wasImporting && !importing && problem == null
        wasImporting = importing
        if (finishedImport) onPhotoReady()
    }

    fun startStory(workout: Workout?) {
        viewModel.selectWorkout(workout?.id)
        if (workout != null && workout.routeConsentNeeded) {
            pendingRouteId = workout.id
            try {
                routeLauncher.launch(workout.id)
                return
            } catch (_: ActivityNotFoundException) {
                pendingRouteId = null
            }
        }
        photoSheetFor = workout?.id ?: DAY_STORY
    }

    Scaffold(snackbarHost = { AppSnackbarHost() }, containerColor = Ink) { padding ->
        val pullState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                viewModel.refreshHealth()
            },
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding(),
                    isRefreshing = refreshing,
                    state = pullState,
                    color = Ink,
                    containerColor = Lime,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Header(
                        name = session?.displayName?.substringBefore(' ')?.takeIf { it.isNotBlank() },
                        sample = health.sample,
                        onSettings = { settingsOpen = true },
                    )
                }
                item {
                    TodayCard(
                        health = health,
                        settings = settings,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                item {
                    PrimaryButton(
                        onClick = { startStory(null) },
                        enabled = !importing,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    ) {
                        if (importing) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = Ink, strokeWidth = 2.dp, trackColor = Color.Transparent)
                        } else {
                            Icon(AppIcons.Sparkle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.create_today_story), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                item {
                    HealthNotice(
                        health = health,
                        onAllow = { permissionLauncher.launch(viewModel.requiredPermissions()) },
                        onInstall = { openHealthConnectInstall(context) },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                item {
                    WorkoutsSection(
                        health = health,
                        settings = settings,
                        enabled = !importing,
                        onPick = ::startStory,
                        onUnlock = { permissionLauncher.launch(viewModel.requiredPermissions()) },
                    )
                }
                item { MotivationLine(Modifier.padding(horizontal = 32.dp, vertical = 8.dp)) }
            }
        }
    }

    val sheetFor = photoSheetFor
    if (sheetFor != null) {
        val workout = health.workouts.firstOrNull { it.id == sheetFor }
        PhotoSourceSheet(
            subtitle = workout?.title ?: stringResource(R.string.story_for_today),
            onDismiss = { photoSheetFor = null },
            onCamera = {
                photoSheetFor = null
                val uri = viewModel.createCaptureUri()
                captureUri = uri
                try {
                    cameraLauncher.launch(uri)
                } catch (_: ActivityNotFoundException) {
                    viewModel.onCameraUnavailable()
                }
            },
            onGallery = {
                photoSheetFor = null
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onNoPhoto = {
                photoSheetFor = null
                viewModel.useNoPhoto()
                onPhotoReady()
            },
        )
    }
    if (settingsOpen) {
        SettingsSheet(
            settings = settings,
            session = session,
            onDismiss = { settingsOpen = false },
            onUnits = viewModel::setUnits,
            onGoal = viewModel::setStepGoal,
            onSampleData = viewModel::setSampleData,
            onHealthAccess = { permissionLauncher.launch(viewModel.requiredPermissions()) },
            onSignIn = { authViewModel.signIn(context) },
            onSignOut = authViewModel::signOut,
            onAbout = {
                settingsOpen = false
                onAbout()
            },
        )
    }
}

private const val DAY_STORY = "day"

@Composable
private fun Header(name: String?, sample: Boolean, onSettings: () -> Unit) {
    val hour = LocalTime.now().hour
    val greeting = stringResource(
        when (hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..16 -> R.string.greeting_afternoon
            in 17..21 -> R.string.greeting_evening
            else -> R.string.greeting_night
        },
    )
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = today.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (name != null) "$greeting, $name" else greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (sample) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.sample_data_on), style = MaterialTheme.typography.labelMedium, color = Ember)
            }
        }
        IconButton(onClick = onSettings) {
            Icon(AppIcons.Settings, contentDescription = stringResource(R.string.settings), tint = TextPrimary)
        }
    }
}

@Composable
private fun TodayCard(health: HealthSnapshot, settings: Settings, modifier: Modifier = Modifier) {
    val stats = health.stats
    val steps = stats.steps
    // Only what this phone recorded today, so the card never shows zeros or dashes for data it doesn't track.
    val data = StoryData(day = stats, units = settings.units)
    val recorded = TodayStats.map { StatFormat.value(it, data) }.filter { it.available }
    val beside = recorded.take(3).ifEmpty { listOf(StatFormat.value("distance", data)) }
    val below = recorded.drop(3)
    SurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(136.dp), contentAlignment = Alignment.Center) {
                    GoalRing(
                        progress = (steps ?: 0L).toFloat() / settings.stepGoal,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (health.loading && steps == null) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = Lime, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = steps?.let(StatFormat::formatCount) ?: StatFormat.UNAVAILABLE,
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                maxLines = 1,
                            )
                        }
                        Text(
                            text = stringResource(R.string.of_goal, StatFormat.formatCount(settings.stepGoal)),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    beside.forEach { MiniStat(it.label, it.text) }
                }
            }
            if (below.isNotEmpty()) {
                HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 20.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    below.forEach { MiniStat(it.label, it.text) }
                }
            }
        }
    }
}

private val TodayStats = listOf(
    "distance", "calories", "exercise", "totalcalories", "sleep", "floors", "heartrate", "restinghr", "elevation",
)

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = TextPrimary, maxLines = 1)
    }
}

@Composable
private fun WorkoutsSection(
    health: HealthSnapshot,
    settings: Settings,
    enabled: Boolean,
    onPick: (Workout) -> Unit,
    onUnlock: () -> Unit,
) {
    Column {
        SectionTitle(stringResource(R.string.todays_workouts), Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(10.dp))
        when {
            health.workouts.isNotEmpty() -> LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(health.workouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        data = StoryData(workout = workout, units = settings.units),
                        enabled = enabled,
                        onClick = { onPick(workout) },
                    )
                }
            }
            health.permissionsGranted && health.extrasMissing -> SurfaceCard(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                onClick = onUnlock,
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.unlock_stats), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.unlock_stats_body), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Icon(AppIcons.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }
            else -> Text(
                text = stringResource(R.string.no_workouts),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun WorkoutCard(workout: Workout, data: StoryData, enabled: Boolean, onClick: () -> Unit) {
    val kind = WorkoutNames.kind(workout.type)
    val hero = StatFormat.value("hero", data)
    val second = StatFormat.value("second", data)
    SurfaceCard(
        modifier = Modifier.width(232.dp),
        onClick = if (enabled) onClick else null,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.workout(kind), contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                when {
                    workout.route.size >= 2 -> RouteThumbnail(workout.route, Modifier.size(width = 64.dp, height = 40.dp))
                    workout.routeConsentNeeded -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.Map, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.add_route), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(workout.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(whenText(workout), style = MaterialTheme.typography.labelMedium, color = TextSecondary, maxLines = 1)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(hero.number, style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                if (hero.unit.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(hero.unit, style = MaterialTheme.typography.titleSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                if (second.available) {
                    Text(
                        second.text,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }
}

private fun whenText(workout: Workout): String {
    val time = workout.start.atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    return "$time · ${StatFormat.durationShort(workout.durationSeconds)}"
}

@Composable
private fun HealthNotice(
    health: HealthSnapshot,
    onAllow: () -> Unit,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (health.loading || health.sample) return
    val (message, action, onAction) = when {
        health.failed -> Triple(stringResource(R.string.health_error), null, null)
        health.availability == HealthAvailability.Unavailable -> Triple(stringResource(R.string.health_unavailable), null, null)
        health.availability == HealthAvailability.UpdateRequired ->
            Triple(stringResource(R.string.health_update), stringResource(R.string.health_install), onInstall)
        !health.permissionsGranted ->
            Triple(stringResource(R.string.health_permission), stringResource(R.string.health_allow), onAllow)
        else -> return
    }
    SurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(AppIcons.Info, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
            if (action != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                SecondaryButton(onClick = onAction) { Text(action, style = MaterialTheme.typography.titleSmall) }
            }
        }
    }
}

@Composable
private fun MotivationLine(modifier: Modifier = Modifier) {
    val lines = stringArrayResource(R.array.motivation)
    val line = remember { lines.randomOrNull() } ?: return
    Text(
        text = "“$line”",
        style = MaterialTheme.typography.bodyMedium,
        color = TextMuted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoSourceSheet(
    subtitle: String,
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onNoPhoto: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(stringResource(R.string.photo_title), style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(20.dp))
            SourceRow(AppIcons.Camera, stringResource(R.string.take_photo), stringResource(R.string.take_photo_body), onCamera)
            SourceRow(AppIcons.Gallery, stringResource(R.string.choose_photo), stringResource(R.string.choose_photo_body), onGallery)
            SourceRow(AppIcons.Sparkle, stringResource(R.string.no_photo), stringResource(R.string.no_photo_body), onNoPhoto)
        }
    }
}

@Composable
private fun SourceRow(icon: ImageVector, title: String, body: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(AppIcons.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}
