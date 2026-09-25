package com.pause

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PauseTheme { HomeScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen() {
    val context = LocalContext.current
    val store = remember { PauseStore(context) }
    val state by store.state.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var serviceOn by remember { mutableStateOf(InstalledApps.isServiceEnabled(context)) }
    var adding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val installed = withContext(Dispatchers.IO) { InstalledApps.load(context) }
        store.ensureDefaults(installed.map { it.packageName }.toSet())
        apps = installed
    }
    // The user turns the service on in system settings, so check again whenever they come back.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) serviceOn = InstalledApps.isServiceEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val current = state ?: return
    val loaded = apps
    val guarded = loaded.orEmpty().filter { it.packageName in current.guarded }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Night),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(Modifier.statusBarsPadding().padding(top = 20.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
        if (!serviceOn) {
            item {
                Card {
                    Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.setup_body), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Night),
                    ) { Text(stringResource(R.string.setup_action)) }
                }
            }
        }
        item { TodayCard(current.today) }
        item {
            SectionTitle(stringResource(R.string.guarded_apps))
        }
        if (loaded != null && guarded.isEmpty()) {
            item { Text(stringResource(R.string.no_guarded), style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
        }
        items(guarded, key = { it.packageName }) { app ->
            AppRow(
                app = app,
                detail = pluralStringResource(R.plurals.opens_today, current.today.opens[app.packageName] ?: 0, current.today.opens[app.packageName] ?: 0),
                action = stringResource(R.string.remove),
                onAction = { scope.launch { store.setGuarded(app.packageName, false) } },
            )
        }
        item {
            OutlinedButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.add_apps), color = TextPrimary)
            }
        }
        item {
            Card {
                Text(stringResource(R.string.daily_limit), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    pluralStringResource(R.plurals.daily_limit_body, current.dailyLimit, current.dailyLimit, PauseStore.LONG_PAUSE_SECONDS),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepButton("−", stringResource(R.string.decrease), current.dailyLimit > PauseStore.MIN_DAILY_LIMIT) {
                        scope.launch { store.setDailyLimit(current.dailyLimit - 1) }
                    }
                    Text(
                        current.dailyLimit.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    StepButton("+", stringResource(R.string.increase), current.dailyLimit < PauseStore.MAX_DAILY_LIMIT) {
                        scope.launch { store.setDailyLimit(current.dailyLimit + 1) }
                    }
                }
            }
        }
    }

    if (adding) {
        ModalBottomSheet(
            onDismissRequest = { adding = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Surface,
        ) {
            val others = loaded.orEmpty().filter { it.packageName !in current.guarded }
            LazyColumn(
                modifier = Modifier.navigationBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item { SectionTitle(stringResource(R.string.add_apps)) }
                items(others, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        detail = null,
                        action = stringResource(R.string.add),
                        onAction = { scope.launch { store.setGuarded(app.packageName, true) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayCard(today: Today) {
    Card {
        Text(
            today.stopped.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = Mint,
        )
        Text(stringResource(R.string.stopped_today), style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        val reasons = today.reasons.filterValues { it > 0 }.mapKeys { (reason, _) -> reasonLabel(reason) }
        if (reasons.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.reasons_title).uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Text(
                reasons.entries.joinToString("  ·  ") { (label, count) -> "$count $label" },
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun reasonLabel(reason: Reason): String = stringResource(
    when (reason) {
        Reason.Bored -> R.string.reason_bored
        Reason.Message -> R.string.reason_message
        Reason.Purpose -> R.string.reason_purpose
    },
).lowercase()

@Composable
private fun AppRow(app: InstalledApp, detail: String?, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(app.icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        TextButton(onClick = onAction) { Text(action, color = Mint) }
    }
}

@Composable
private fun StepButton(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(44.dp),
    ) {
        Text(symbol, fontSize = 20.sp, color = if (enabled) TextPrimary else TextSecondary)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface)
            .border(1.dp, Line, shape)
            .padding(20.dp),
    ) {
        Column { content() }
    }
}
