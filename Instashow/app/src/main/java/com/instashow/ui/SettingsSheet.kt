package com.instashow.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instashow.BuildConfig
import com.instashow.R
import com.instashow.auth.UserSession
import com.instashow.health.StatFormat
import com.instashow.settings.MAX_STEP_GOAL
import com.instashow.settings.MIN_STEP_GOAL
import com.instashow.settings.Settings
import com.instashow.settings.Units
import com.instashow.ui.theme.Ink
import com.instashow.ui.theme.Lime
import com.instashow.ui.theme.Line
import com.instashow.ui.theme.Surface
import com.instashow.ui.theme.SurfaceHigh
import com.instashow.ui.theme.TextPrimary
import com.instashow.ui.theme.TextSecondary
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: Settings,
    session: UserSession?,
    onDismiss: () -> Unit,
    onUnits: (Units) -> Unit,
    onGoal: (Long) -> Unit,
    onSampleData: (Boolean) -> Unit,
    onHealthAccess: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onAbout: () -> Unit,
) {
    var goal by remember { mutableFloatStateOf(settings.stepGoal.toFloat()) }
    LaunchedEffect(settings.stepGoal) { goal = settings.stepGoal.toFloat() }
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
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Spacer(Modifier.height(20.dp))

            SectionTitle(stringResource(R.string.units))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(stringResource(R.string.kilometers), settings.units == Units.Metric, { onUnits(Units.Metric) })
                Pill(stringResource(R.string.miles), settings.units == Units.Imperial, { onUnits(Units.Imperial) })
            }
            Spacer(Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.step_goal)) {
                Text(
                    StatFormat.formatCount(roundGoal(goal)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Lime,
                )
            }
            AppSlider(
                value = goal,
                onValueChange = { goal = it },
                onValueChangeFinished = { onGoal(roundGoal(goal)) },
                valueRange = MIN_STEP_GOAL.toFloat()..MAX_STEP_GOAL.toFloat(),
            )
            Text(stringResource(R.string.step_goal_body), style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            if (BuildConfig.DEBUG) {
                Spacer(Modifier.height(20.dp))
                ToggleRow(
                    title = stringResource(R.string.sample_data),
                    body = stringResource(R.string.sample_data_body),
                    checked = settings.sampleData,
                    onCheckedChange = onSampleData,
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Line)
            SettingsRow(
                title = if (session != null) session.displayName.ifBlank { session.email } else stringResource(R.string.sign_in_button),
                body = if (session != null) stringResource(R.string.signed_in_body) else stringResource(R.string.sign_in_optional),
                action = if (session != null) stringResource(R.string.sign_out) else null,
                onClick = if (session != null) onSignOut else onSignIn,
            )
            HorizontalDivider(color = Line)
            SettingsRow(
                title = stringResource(R.string.health_access),
                body = stringResource(R.string.health_access_body),
                action = null,
                onClick = onHealthAccess,
            )
            HorizontalDivider(color = Line)
            SettingsRow(
                title = stringResource(R.string.about_title),
                body = stringResource(R.string.about_row_body),
                action = null,
                onClick = onAbout,
            )
        }
    }
}

@Composable
private fun ToggleRow(title: String, body: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Ink,
                checkedTrackColor = Lime,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceHigh,
                uncheckedBorderColor = Line,
            ),
        )
    }
}

private fun roundGoal(value: Float): Long = ((value / 500f).roundToLong() * 500L).coerceIn(MIN_STEP_GOAL, MAX_STEP_GOAL)

@Composable
private fun SettingsRow(title: String, body: String, action: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        if (action != null) {
            Text(action, style = MaterialTheme.typography.labelLarge, color = Lime)
        } else {
            Icon(AppIcons.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}
