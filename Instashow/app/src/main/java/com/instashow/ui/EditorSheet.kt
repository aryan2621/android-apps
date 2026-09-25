package com.instashow.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instashow.R
import com.instashow.health.StatFormat
import com.instashow.render.StoryStyle
import com.instashow.story.StoryViewModel
import com.instashow.template.StoryTemplate
import com.instashow.template.headlineText
import com.instashow.ui.theme.Ink
import com.instashow.ui.theme.Lime
import com.instashow.ui.theme.Line
import com.instashow.ui.theme.SurfaceHigh
import com.instashow.ui.theme.TextMuted
import com.instashow.ui.theme.TextPrimary
import com.instashow.ui.theme.TextSecondary

private enum class EditorTab(val label: Int) {
    Text(R.string.tab_text),
    Size(R.string.tab_size),
    Color(R.string.tab_color),
    Frame(R.string.tab_frame),
}

@Composable
fun StoryEditorSheet(
    viewModel: StoryViewModel,
    style: StoryStyle,
    template: StoryTemplate?,
) {
    var tab by rememberSaveable { mutableStateOf(EditorTab.Text) }
    val tabs = EditorTab.entries
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 16.dp),
    ) {
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tabs) { entry ->
                Pill(text = stringResource(entry.label), selected = tab == entry, onClick = { tab = entry })
            }
        }
        Spacer(Modifier.height(20.dp))
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .heightIn(min = 140.dp),
        ) {
            when (tab) {
                EditorTab.Text -> TextTab(viewModel, style, template)
                EditorTab.Size -> SizeTab(viewModel, style)
                EditorTab.Color -> ColorTab(viewModel, style)
                EditorTab.Frame -> FrameTab(viewModel, style)
            }
        }
    }
}

@Composable
private fun TextTab(viewModel: StoryViewModel, style: StoryStyle, template: StoryTemplate?) {
    val focus = LocalFocusManager.current
    val data by viewModel.storyData.collectAsStateWithLifecycle()
    val defaultHeadline = template?.headlineText?.let { StatFormat.fill(it, data) }
    if (template == null || defaultHeadline == null) {
        Text(stringResource(R.string.no_headline), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        return
    }
    val quote = template.quote
    var headline by remember(template.id) { mutableStateOf(style.headline ?: defaultHeadline) }
    var caption by remember(template.id) { mutableStateOf(style.caption ?: quote?.caption?.let { StatFormat.fill(it, data) }.orEmpty()) }
    StoryTextField(
        value = headline,
        onValueChange = { headline = it.take(StoryViewModel.MAX_HEADLINE) },
        label = stringResource(R.string.headline),
        capitalization = KeyboardCapitalization.Characters,
        onDone = {
            viewModel.setHeadline(headline)
            focus.clearFocus()
        },
    )
    if (quote != null) {
        Spacer(Modifier.height(12.dp))
        StoryTextField(
            value = caption,
            onValueChange = { caption = it.take(StoryViewModel.MAX_CAPTION) },
            label = stringResource(R.string.caption),
            capitalization = KeyboardCapitalization.Sentences,
            onDone = {
                viewModel.setCaption(caption)
                focus.clearFocus()
            },
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.text_applies_all),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = {
            viewModel.setHeadline(null)
            viewModel.setCaption(null)
            headline = defaultHeadline
            caption = quote?.caption?.let { StatFormat.fill(it, data) }.orEmpty()
            focus.clearFocus()
        }) {
            Text(stringResource(R.string.reset), color = TextSecondary)
        }
        TextButton(onClick = {
            viewModel.setHeadline(headline)
            if (quote != null) viewModel.setCaption(caption)
            focus.clearFocus()
        }) {
            Text(stringResource(R.string.apply), color = Lime)
        }
    }
}

@Composable
private fun StoryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    capitalization: KeyboardCapitalization,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(AppIcons.Close, contentDescription = stringResource(R.string.clear), tint = TextSecondary)
                }
            }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Lime,
            unfocusedBorderColor = Line,
            focusedLabelColor = Lime,
            unfocusedLabelColor = TextSecondary,
            cursorColor = Lime,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SizeTab(viewModel: StoryViewModel, style: StoryStyle) {
    var slider by remember { mutableFloatStateOf(style.textScale) }
    LaunchedEffect(style.textScale) { slider = style.textScale }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { viewModel.setTextScale(style.textScale - 0.1f) },
            enabled = style.textScale > StoryViewModel.TEXT_SCALE_MIN,
        ) {
            Icon(AppIcons.SmallerText, contentDescription = stringResource(R.string.smaller_text), tint = TextPrimary)
        }
        AppSlider(
            value = slider,
            onValueChange = { slider = it },
            onValueChangeFinished = { viewModel.setTextScale(slider) },
            valueRange = StoryViewModel.TEXT_SCALE_MIN..StoryViewModel.TEXT_SCALE_MAX,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { viewModel.setTextScale(style.textScale + 0.1f) },
            enabled = style.textScale < StoryViewModel.TEXT_SCALE_MAX,
        ) {
            Icon(AppIcons.LargerText, contentDescription = stringResource(R.string.larger_text), tint = TextPrimary)
        }
    }
}

@Composable
private fun ColorTab(viewModel: StoryViewModel, style: StoryStyle) {
    var hue by remember { mutableFloatStateOf(0f) }
    SectionTitle(stringResource(R.string.text_color))
    Spacer(Modifier.height(10.dp))
    ColorPicker(
        selected = style.textColor,
        onPick = viewModel::setTextColor,
        hue = hue,
        onHue = { hue = it },
        onHueFinished = { viewModel.setTextColor(hueColor(hue)) },
        autoLabel = stringResource(R.string.color_auto),
    )
}

@Composable
private fun FrameTab(viewModel: StoryViewModel, style: StoryStyle) {
    var hue by remember { mutableFloatStateOf(0f) }
    var frameSlider by remember { mutableFloatStateOf(style.frameScale) }
    LaunchedEffect(style.frameScale) { frameSlider = style.frameScale }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.frame), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(stringResource(R.string.frame_body), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = style.backgroundColor != null,
            onCheckedChange = { enabled -> viewModel.setBackgroundColor(if (enabled) 0xFFFFFFFF.toInt() else null) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Ink,
                checkedTrackColor = Lime,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceHigh,
                uncheckedBorderColor = Line,
            ),
        )
    }
    if (style.backgroundColor != null) {
        Spacer(Modifier.height(16.dp))
        ColorPicker(
            selected = style.backgroundColor,
            onPick = { color -> viewModel.setBackgroundColor(color ?: 0xFFFFFFFF.toInt()) },
            hue = hue,
            onHue = { hue = it },
            onHueFinished = { viewModel.setBackgroundColor(hueColor(hue)) },
            autoLabel = null,
        )
        Spacer(Modifier.height(12.dp))
        SectionTitle(stringResource(R.string.image_size))
        AppSlider(
            value = frameSlider,
            onValueChange = { frameSlider = it },
            onValueChangeFinished = { viewModel.setFrameScale(frameSlider) },
            valueRange = StoryViewModel.FRAME_SCALE_MIN..StoryViewModel.FRAME_SCALE_MAX,
        )
    }
}
