package com.instashow.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instashow.R
import com.instashow.share.StoryShare
import com.instashow.story.StoryViewModel
import com.instashow.template.StoryTemplate
import com.instashow.ui.theme.Ink
import com.instashow.ui.theme.Lime
import com.instashow.ui.theme.Line
import com.instashow.ui.theme.Surface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Busy { Saving, Sharing, Instagram }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: StoryViewModel,
    storyShare: StoryShare,
    onBack: () -> Unit,
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    val renderFailed by viewModel.renderFailed.collectAsStateWithLifecycle()
    val catalogError by viewModel.catalogError.collectAsStateWithLifecycle()
    val data by viewModel.storyData.collectAsStateWithLifecycle()
    val style by viewModel.style.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalAppSnackbar.current
    var busy by remember { mutableStateOf<Busy?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    val hasPhoto by viewModel.hasPhoto.collectAsStateWithLifecycle()
    val instagramInstalled = remember { storyShare.isInstagramInstalled() }
    val renderFailedText = stringResource(R.string.render_failed)
    val catalogEmpty = stringResource(R.string.catalog_empty)
    val shareFailedText = stringResource(R.string.share_failed)
    val saveFailedText = stringResource(R.string.save_failed)
    val savedText = stringResource(R.string.saved_to_gallery)
    val storagePermissionText = stringResource(R.string.storage_permission)
    val instagramFailedText = stringResource(R.string.instagram_failed)

    val pagerState = rememberPagerState(pageCount = { templates.size })
    val currentTemplate = templates.getOrNull(pagerState.currentPage)

    LaunchedEffect(catalogError, renderFailed) {
        when {
            catalogError -> snackbar.showSnackbar(catalogEmpty)
            renderFailed -> snackbar.showSnackbar(renderFailedText)
        }
    }

    /** Renders the full-size story, hands it to [use], then frees it. */
    fun withFullStory(kind: Busy, failure: String, use: suspend (Bitmap) -> String?) {
        val template = currentTemplate ?: return
        if (busy != null) return
        scope.launch {
            busy = kind
            try {
                val bitmap = viewModel.renderFull(template.id)
                try {
                    use(bitmap)?.let { snackbar.showSnackbar(it) }
                } finally {
                    bitmap.recycle()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                snackbar.showSnackbar(failure)
            } finally {
                busy = null
            }
        }
    }

    fun save() = withFullStory(Busy.Saving, saveFailedText) { bitmap ->
        withContext(Dispatchers.IO) { storyShare.saveToGallery(bitmap) }
        savedText
    }

    val savePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) save() else scope.launch { snackbar.showSnackbar(storagePermissionText) }
    }

    fun requestSave() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            savePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            save()
        }
    }

    fun share() {
        val activity = context as? Activity ?: return
        withFullStory(Busy.Sharing, shareFailedText) { bitmap ->
            storyShare.share(activity, bitmap)
            null
        }
    }

    fun instagramFullStory() {
        val activity = context as? Activity ?: return
        withFullStory(Busy.Instagram, instagramFailedText) { bitmap ->
            storyShare.shareToInstagramStory(activity, bitmap)
            null
        }
    }

    Scaffold(
        snackbarHost = { AppSnackbarHost() },
        containerColor = Ink,
        topBar = {
            StoryTopBar(
                title = data.workout?.title ?: stringResource(R.string.todays_story),
                onBack = onBack,
            )
        },
    ) { padding ->
        if (templates.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (!catalogError) CircularProgressIndicator(color = Lime)
            }
            return@Scaffold
        }
        var healthReady by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            viewModel.reloadHealth()
            healthReady = true
        }
        LaunchedEffect(pagerState.currentPage, templates, healthReady, style, data) {
            if (!healthReady) return@LaunchedEffect
            listOf(pagerState.currentPage, pagerState.currentPage + 1, pagerState.currentPage - 1).forEach { index ->
                templates.getOrNull(index)?.let { viewModel.render(it.id) }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TemplateStrip(
                templates = templates,
                current = pagerState.currentPage,
                onPick = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            )
            Spacer(Modifier.height(12.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 36.dp),
                pageSpacing = 16.dp,
            ) { page ->
                val template = templates[page]
                val bitmap = pages[template.id]
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Surface)
                            .border(1.dp, Line, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (bitmap != null && !bitmap.isRecycled) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = template.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                            )
                        } else {
                            CircularProgressIndicator(color = Lime)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            val ready = currentTemplate?.let { pages[it.id] } != null && busy == null
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top,
            ) {
                if (instagramInstalled) {
                    CircleAction(
                        icon = AppIcons.Instagram,
                        label = stringResource(R.string.instagram),
                        onClick = ::instagramFullStory,
                        style = ActionStyle.Instagram,
                        enabled = ready,
                        size = 64.dp,
                    )
                }
                CircleAction(
                    icon = AppIcons.Save,
                    label = stringResource(R.string.save_story),
                    onClick = ::requestSave,
                    style = if (instagramInstalled) ActionStyle.Tonal else ActionStyle.Primary,
                    enabled = ready,
                    size = 64.dp,
                )
                CircleAction(
                    icon = AppIcons.Share,
                    label = stringResource(R.string.share_story),
                    onClick = ::share,
                    enabled = ready,
                    size = 64.dp,
                )
                CircleAction(
                    icon = AppIcons.Edit,
                    label = stringResource(R.string.edit_story),
                    onClick = { editorOpen = true },
                    size = 64.dp,
                )
            }
        }
        if (busy != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Ink.copy(alpha = 0.5f))
                    .clickable(enabled = true, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Lime)
            }
        }
    }

    if (editorOpen) {
        ModalBottomSheet(
            onDismissRequest = { editorOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Surface,
            scrimColor = Ink.copy(alpha = 0.2f),
        ) {
            StoryEditorSheet(
                viewModel = viewModel,
                style = style,
                template = currentTemplate,
            )
        }
    }
}

@Composable
private fun TemplateStrip(templates: List<StoryTemplate>, current: Int, onPick: (Int) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(current) { listState.animateScrollToItem((current - 1).coerceAtLeast(0)) }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(templates, key = { _, template -> template.id }) { index, template ->
            Pill(text = template.name, selected = index == current, onClick = { onPick(index) })
        }
    }
}
