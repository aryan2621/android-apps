package com.pause

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Notices when a guarded app comes to the front and puts the pause screen over it. It only reads
 * which package owns the new window, never anything inside it.
 */
class PauseService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var state: PauseState? = null

    /** The app the user is in. Overlays such as the keyboard or the notification shade don't change it. */
    private var current: String? = null
    private var passThrough: Set<String> = emptySet()
    private var lastPause: Pair<String, Long>? = null

    override fun onServiceConnected() {
        passThrough = buildSet {
            add(packageName)
            add("com.android.systemui")
            getSystemService(InputMethodManager::class.java)?.enabledInputMethodList?.forEach { add(it.packageName) }
        }
        scope.launch { PauseStore(this@PauseService).state.collect { state = it } }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val app = event.packageName?.toString() ?: return
        if (app in passThrough) return
        // Some apps open a second screen of their own right after launch, covering the pause.
        // Until the user decides, keep putting that same pause back on top.
        if (PauseActivity.showingFor == app) {
            showPause(app)
            return
        }
        // Until the saved settings have loaded there is nothing to compare against, so wait for the next change.
        val state = state ?: return
        if (app == current) return
        current = app
        if (app !in state.guarded || state.inGrace(app)) return
        // Apps and the launcher fire several window events while one app opens. One open gets one pause.
        val now = System.currentTimeMillis()
        lastPause?.let { (last, at) -> if (last == app && now - at < REPEAT_WINDOW_MILLIS) return }
        lastPause = app to now
        showPause(app)
    }

    private fun showPause(app: String) {
        startActivity(
            Intent(this, PauseActivity::class.java)
                .putExtra(PauseActivity.EXTRA_PACKAGE, app)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    override fun onInterrupt() = Unit

    private companion object {
        const val REPEAT_WINDOW_MILLIS = 3_000L
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
