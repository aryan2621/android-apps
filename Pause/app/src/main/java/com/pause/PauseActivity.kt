package com.pause

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos

/** The breathing screen that sits between the user and a guarded app. */
class PauseActivity : ComponentActivity() {
    private val store by lazy { PauseStore(this) }

    /** The app being paused and when this pause began. Changing it starts the screen over. */
    private var session by mutableStateOf<Session?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!handle(intent)) return finish()
        setContent {
            PauseTheme {
                val current = session ?: return@PauseTheme
                var opens by remember { mutableIntStateOf(0) }
                var limit by remember { mutableIntStateOf(PauseStore.DEFAULT_DAILY_LIMIT) }
                LaunchedEffect(current.packageName) {
                    store.state.collect { state ->
                        limit = state.dailyLimit
                        opens = state.today.opens[current.packageName] ?: 0
                    }
                }
                if (opens > 0) {
                    key(current) {
                        PauseScreen(
                            startedAt = current.startedAt,
                            label = current.label,
                            icon = current.icon,
                            opens = opens,
                            overLimit = opens > limit,
                            onClose = ::closeApp,
                            onOpen = { reason -> openApp(current.packageName, reason) },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handle(intent)
    }

    /**
     * Brought back on top by the service. The same undecided pause carries on, unless the user
     * walked away from it for a while: then coming back gets a fresh breath and counts as a new open.
     */
    private fun handle(intent: Intent): Boolean {
        val target = intent.getStringExtra(EXTRA_PACKAGE) ?: return false
        val now = System.currentTimeMillis()
        val stale = leftAt > 0L && now - leftAt > STALE_MILLIS
        if (showingFor != target || stale || session == null && startedAt == 0L) {
            showingFor = target
            startedAt = now
            lifecycleScope.launch { store.recordOpen(target) }
        }
        leftAt = 0L
        if (session?.packageName != target || session?.startedAt != startedAt) {
            session = Session(target, InstalledApps.label(this, target), InstalledApps.icon(this, target), startedAt)
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing && showingFor != null) leftAt = System.currentTimeMillis()
    }

    override fun onDestroy() {
        if (isFinishing && showingFor == session?.packageName) showingFor = null
        super.onDestroy()
    }

    private fun decided() {
        showingFor = null
        startedAt = 0L
    }

    private fun closeApp() {
        decided()
        lifecycleScope.launch {
            store.recordStopped()
            StatsWidget.refresh(this@PauseActivity)
        }
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    private fun openApp(target: String, reason: Reason) {
        decided()
        lifecycleScope.launch {
            store.recordOpenAnyway(target, reason)
            // The guarded app is still underneath; stepping aside reveals it.
            finish()
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "package"

        /** The app whose pause is waiting for a decision, so the service never stacks a second one. */
        @Volatile
        var showingFor: String? = null
        private var startedAt = 0L
        private var leftAt = 0L
        private const val STALE_MILLIS = 30_000L
    }
}

private data class Session(
    val packageName: String,
    val label: String,
    val icon: Bitmap?,
    val startedAt: Long,
)

private enum class Step { Breathing, Choice, Reason }

@Composable
private fun PauseScreen(
    startedAt: Long,
    label: String,
    icon: Bitmap?,
    opens: Int,
    overLimit: Boolean,
    onClose: () -> Unit,
    onOpen: (Reason) -> Unit,
) {
    val seconds = if (overLimit) PauseStore.LONG_PAUSE_SECONDS else PauseStore.PAUSE_SECONDS
    var elapsed by remember { mutableLongStateOf(0L) }
    var step by remember { mutableStateOf(Step.Breathing) }
    LaunchedEffect(Unit) {
        while (true) {
            elapsed = System.currentTimeMillis() - startedAt
            if (step == Step.Breathing && elapsed >= seconds * 1000L) step = Step.Choice
            delay(16)
        }
    }
    BackHandler { if (step == Step.Reason) step = Step.Choice else onClose() }

    // One breath is eight seconds: four in, four out.
    val cycle = (elapsed % BREATH_MILLIS) / BREATH_MILLIS.toFloat()
    val breath = 0.5f - 0.5f * cos(2f * PI.toFloat() * cycle)
    val inhaling = cycle < 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
            .safeDrawingPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        if (icon != null) {
            Image(icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.height(12.dp))
        }
        Text(
            stringResource(R.string.about_to_open, label),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            pluralStringResource(R.plurals.opened_times, opens, opens),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        if (overLimit) {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.over_limit), style = MaterialTheme.typography.bodySmall, color = Mint, textAlign = TextAlign.Center)
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            BreathingCircle(breath)
            if (step == Step.Breathing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(if (inhaling) R.string.breathe_in else R.string.breathe_out),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    val left = (seconds - (elapsed / 1000L).toInt()).coerceAtLeast(1)
                    Text("$left", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                }
            }
        }

        AnimatedContent(targetState = step, label = "choice", modifier = Modifier.fillMaxWidth()) { current ->
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                when (current) {
                    Step.Breathing -> Spacer(Modifier.height(112.dp))
                    Step.Choice -> {
                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Night),
                        ) { Text(stringResource(R.string.close_app, label), style = MaterialTheme.typography.titleMedium) }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { step = Step.Reason }, modifier = Modifier.height(48.dp)) {
                            Text(stringResource(R.string.open_anyway), color = TextSecondary)
                        }
                    }
                    Step.Reason -> {
                        Text(stringResource(R.string.why_now), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            ReasonButton(R.string.reason_bored) { onOpen(Reason.Bored) }
                            ReasonButton(R.string.reason_message) { onOpen(Reason.Message) }
                            ReasonButton(R.string.reason_purpose) { onOpen(Reason.Purpose) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasonButton(label: Int, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(48.dp)) {
        Text(stringResource(label), color = TextPrimary)
    }
}

@Composable
private fun BreathingCircle(breath: Float) {
    Canvas(
        Modifier
            .size(260.dp)
            .scale(0.55f + 0.45f * breath),
    ) {
        val radius = size.minDimension / 2f
        drawCircle(color = Mint.copy(alpha = 0.10f + 0.12f * breath), radius = radius)
        drawCircle(color = Mint.copy(alpha = 0.55f), radius = radius - 3.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
        drawCircle(color = Mint.copy(alpha = 0.18f), radius = radius * 0.55f, center = Offset(size.width / 2f, size.height / 2f))
    }
}

private const val BREATH_MILLIS = 8_000L
