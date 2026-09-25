package com.pause

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first

/** "Opens stopped today" on the home screen. Tapping it opens Pause. */
class StatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stopped = PauseStore(context).state.first().today.stopped
        val label = context.getString(R.string.widget_label)
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Night)
                    .cornerRadius(24.dp)
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stopped.toString(),
                    style = TextStyle(color = ColorProvider(Mint), fontSize = 44.sp, fontWeight = FontWeight.Bold),
                )
                Text(
                    text = label,
                    style = TextStyle(color = ColorProvider(Color(0xFF98A2B3)), fontSize = 13.sp),
                )
            }
        }
    }

    companion object {
        suspend fun refresh(context: Context) {
            runCatching { StatsWidget().updateAll(context) }
        }
    }
}

class StatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatsWidget()
}
