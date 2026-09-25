package com.instashow.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instashow.R
import com.instashow.ui.theme.Ink

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val sections = listOf(
        R.string.about_source_title to R.string.about_source_body,
        R.string.about_window_title to R.string.about_window_body,
        R.string.about_permission_title to R.string.about_permission_body,
        R.string.about_steps_title to R.string.about_steps_body,
        R.string.about_distance_title to R.string.about_distance_body,
        R.string.about_workouts_title to R.string.about_workouts_body,
        R.string.about_instagram_title to R.string.about_instagram_body,
    )
    Scaffold(
        containerColor = Ink,
        topBar = {
            StoryTopBar(
                title = stringResource(R.string.about_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            sections.forEach { (title, body) ->
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
