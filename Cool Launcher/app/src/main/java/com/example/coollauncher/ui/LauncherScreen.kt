package com.example.coollauncher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.coollauncher.LauncherViewModel
import com.example.coollauncher.data.AppGroup
import com.example.coollauncher.data.AppInfo
import com.example.coollauncher.ui.components.AppGridCell
import com.example.coollauncher.ui.components.CustomDialog
import com.example.coollauncher.ui.components.FolderAppsSheet
import com.example.coollauncher.ui.components.CustomTextField
import com.example.coollauncher.ui.components.FrostedCard
import com.example.coollauncher.ui.components.GroupFolderCard
import com.example.coollauncher.ui.components.PrimaryFAB
import com.example.coollauncher.ui.components.PrimaryIconButton
import com.example.coollauncher.ui.components.SecondaryButton
import com.example.coollauncher.ui.components.SecondaryIconButton
import com.example.coollauncher.ui.components.AlphabetSlider
import com.example.coollauncher.ui.components.TourGuide
import com.example.coollauncher.ui.components.TourIcon
import com.example.coollauncher.ui.components.TourStep
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.LocalHardMinimalMode
import com.example.coollauncher.ui.theme.backgroundDark
import com.example.coollauncher.ui.theme.textPrimary
import com.example.coollauncher.ui.theme.textSecondary
import com.example.coollauncher.util.openAppInfo

private data class AppGroupMenuState(
    val packageName: String,
    val groupId: String?,
    val groups: List<AppGroup>,
) {
    val inGroup: Boolean get() = groupId != null
}

@Composable
private fun HardModeSectionHeader(
    title: String,
    extraTopPadding: Dp = 0.dp,
) {
    Text(
        text = title,
        style = AppTypography.sectionTitle(),
        color = textPrimary,
        modifier = Modifier.padding(top = 8.dp + extraTopPadding, bottom = 4.dp),
    )
}

@Composable
private fun HardModeGroupHeader(
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = AppTypography.sectionTitle(),
            color = textPrimary,
        )
        Text(
            text = if (isExpanded) "▾" else "▸",
            style = AppTypography.body(),
            color = textSecondary,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HardModeListRow(
    app: AppInfo,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    indent: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null,
) {
    val verticalPadding = if (indent) 6.dp else 10.dp
    val horizontalPadding = if (indent) 4.dp else 4.dp
    val startPadding = if (indent) 20.dp else 0.dp
    val effectiveOnClick = if (isSelectionMode && onToggleSelect != null) onToggleSelect else onLaunch
    val effectiveOnLongClick = if (isSelectionMode) null else onLongClick
    val accentColors = LocalAccentColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = effectiveOnClick,
                onLongClick = effectiveOnLongClick,
            )
            .padding(start = startPadding, end = horizontalPadding, top = verticalPadding, bottom = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = app.label.toString(),
            style = AppTypography.body(),
            color = textPrimary,
        )
        if (isSelectionMode) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(AppIcons.Check),
                        contentDescription = null,
                        tint = accentColors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: AppInfo,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = onLongClick,
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppIcon(drawable = app.icon, size = 48.dp)
        Text(
            text = app.label.toString(),
            style = AppTypography.body(),
            color = textPrimary,
        )
    }
}

@Composable
private fun EmptyAppsHint(
    hasGroups: Boolean,
    onCreateGroupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (hasGroups) "All your apps are in groups." else "Your apps will appear here.",
            style = AppTypography.body(),
            color = textSecondary,
            textAlign = TextAlign.Center,
        )
        if (!hasGroups) {
            SecondaryIconButton(
                onClick = onCreateGroupClick,
                icon = { Icon(painter = painterResource(AppIcons.Add), contentDescription = null, tint = textPrimary) },
                contentDescription = "Create a group",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    showTour: Boolean = false,
    onTourComplete: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    var showNewGroupDialog by rememberSaveable { mutableStateOf(false) }
    var showAppGroupMenu: AppGroupMenuState? by rememberSaveable { mutableStateOf(null) }
    var newGroupName by rememberSaveable { mutableStateOf("") }
    var showDeleteGroupDialog by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
    var showFolderAppsSheet by rememberSaveable {
        mutableStateOf<Pair<AppGroup, List<AppInfo>>?>(
            null
        )
    }
    var tourStep by rememberSaveable { mutableStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isMoveSelectionMode by remember { mutableStateOf(false) }
    var selectedPackageNames by remember { mutableStateOf(setOf<String>()) }
    var showMoveToGroupDialog by remember { mutableStateOf(false) }

    fun toggleSelect(packageName: String) {
        selectedPackageNames = if (packageName in selectedPackageNames) {
            selectedPackageNames - packageName
        } else {
            selectedPackageNames + packageName
        }
    }

    val tourSteps = remember {
        listOf(
            TourStep(
                "Welcome",
                "Organize your apps with groups. Create folders and move apps into them.",
                TourIcon.Welcome
            ),
            TourStep(
                "Create a group",
                "Tap the + button at the bottom to create a new folder. Give it a name like Social or Work.",
                TourIcon.CreateGroup
            ),
            TourStep(
                "Folders & apps",
                "Tap a folder to open it and launch apps. Long-press any app to move it into a group.",
                TourIcon.FoldersApps
            ),
            TourStep(
                "Settings",
                "Open Settings (gear icon) to change theme color, font, minimal mode, and set default launcher.",
                TourIcon.Settings
            ),
            TourStep(
                "You're all set",
                "Swipe through your groups and tap apps to launch. Enjoy your minimal launcher.",
                TourIcon.Done
            ),
        )
    }

    val groupedPackages = uiState.groups.flatMap { it.appPackageNames }.toSet()
    val ungroupedApps = uiState.apps.filter { it.packageName !in groupedPackages }
    val query = searchQuery.trim().lowercase()
    val filteredUngrouped = if (query.isEmpty()) ungroupedApps
    else ungroupedApps.filter { it.label.toString().lowercase().contains(query) }

    fun groupAppsFiltered(group: AppGroup): List<AppInfo> {
        val list =
            group.appPackageNames.mapNotNull { pkg -> uiState.apps.find { it.packageName == pkg } }
        return if (query.isEmpty()) list else list.filter {
            it.label.toString().lowercase().contains(query)
        }
    }

    val groupsWithMatches = if (query.isEmpty()) uiState.groups
    else uiState.groups.filter { group -> groupAppsFiltered(group).isNotEmpty() }
    val groupsWithApps = groupsWithMatches.filter { group -> groupAppsFiltered(group).isNotEmpty() }
    val hasNoSearchResults =
        query.isNotEmpty() && groupsWithMatches.isEmpty() && filteredUngrouped.isEmpty()

    var expandedGroupIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    val context = LocalContext.current
    val accentColors = LocalAccentColors.current
    val hardMinimalMode = LocalHardMinimalMode.current
    val listState = rememberLazyListState()

    val letterToIndex: Map<Char, Int> = remember(
        hardMinimalMode,
        hasNoSearchResults,
        groupsWithApps,
        filteredUngrouped,
        query,
        uiState.apps,
        expandedGroupIds,
    ) {
        if (!hardMinimalMode || hasNoSearchResults || (groupsWithApps.isEmpty() && filteredUngrouped.isEmpty())) {
            emptyMap()
        } else {
            buildMap<Char, Int> {
                fun putFirstLetter(name: String, index: Int) {
                    val c = name.uppercase().firstOrNull() ?: return
                    if (c in 'A'..'Z' && !containsKey(c)) put(c, index)
                }

                var idx = 1
                for (group in groupsWithApps) {
                    val groupApps = groupAppsFiltered(group)
                    putFirstLetter(group.name, idx)
                    idx++
                    if (group.id in expandedGroupIds) {
                        for (appInGroup in groupApps) {
                            putFirstLetter(appInGroup.label.toString(), idx)
                            idx++
                        }
                    }
                }
                if (filteredUngrouped.isNotEmpty()) {
                    idx++
                    for (app in filteredUngrouped) {
                        putFirstLetter(app.label.toString(), idx)
                        idx++
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        backgroundDark,
                        backgroundDark.copy(alpha = 0.98f),
                    ),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 40.dp, bottom = 8.dp),
                ) {
                    val searchFocusRequester = remember { FocusRequester() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { searchFocusRequester.requestFocus() }
                    ) {
                        CustomTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search apps",
                            label = null,
                            singleLine = true,
                            bottomBorderOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            focusRequester = searchFocusRequester,
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshApps() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (hardMinimalMode && letterToIndex.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .fillMaxHeight(),
                                        contentPadding = PaddingValues(
                                            start = 20.dp,
                                            end = 12.dp,
                                            top = 24.dp,
                                            bottom = 24.dp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(20.dp),
                                    ) {
                                        item(key = "top_padding") {
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }

                                        if (hasNoSearchResults) {
                                            item(key = "no_results") {
                                                Text(
                                                    text = "No apps match \"$searchQuery\"",
                                                    style = AppTypography.body(),
                                                    color = textSecondary,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 24.dp),
                                                )
                                            }
                                        } else if (hardMinimalMode) {
                                            if (groupsWithApps.isEmpty() && filteredUngrouped.isEmpty()) {
                                                item(key = "empty_apps") {
                                                    EmptyAppsHint(
                                                        hasGroups = uiState.groups.isNotEmpty(),
                                                        onCreateGroupClick = {
                                                            showNewGroupDialog = true
                                                        },
                                                    )
                                                }
                                            } else {
                                                groupsWithApps.forEach { group ->
                                                    val groupApps = groupAppsFiltered(group)
                                                    val isExpanded = group.id in expandedGroupIds
                                                    item(key = "group_header_${group.id}") {
                                                        HardModeGroupHeader(
                                                            title = group.name,
                                                            isExpanded = isExpanded,
                                                            onClick = {
                                                                expandedGroupIds = if (isExpanded) expandedGroupIds - group.id else expandedGroupIds + group.id
                                                            },
                                                        )
                                                    }
                                                    if (isExpanded) {
                                                        items(
                                                            items = groupApps,
                                                            key = { it.packageName },
                                                        ) { app ->
                                                            HardModeListRow(
                                                                app = app,
                                                                onLaunch = { viewModel.launchApp(app.packageName) },
                                                                onLongClick = {
                                                                    showAppGroupMenu =
                                                                        AppGroupMenuState(
                                                                            packageName = app.packageName,
                                                                            groupId = group.id,
                                                                            groups = uiState.groups,
                                                                        )
                                                                },
                                                                indent = true,
                                                                isSelectionMode = isMoveSelectionMode,
                                                                isSelected = app.packageName in selectedPackageNames,
                                                                onToggleSelect = { toggleSelect(app.packageName) },
                                                            )
                                                        }
                                                    }
                                                }
                                                if (filteredUngrouped.isNotEmpty()) {
                                                    item(key = "ungrouped_header") {
                                                        HardModeSectionHeader("Apps", extraTopPadding = 16.dp)
                                                    }
                                                    items(
                                                        items = filteredUngrouped,
                                                        key = { it.packageName },
                                                    ) { app ->
                                                        HardModeListRow(
                                                            app = app,
                                                            onLaunch = { viewModel.launchApp(app.packageName) },
                                                            onLongClick = {
                                                                showAppGroupMenu =
                                                                    AppGroupMenuState(
                                                                        packageName = app.packageName,
                                                                        groupId = null,
                                                                        groups = uiState.groups,
                                                                    )
                                                            },
                                                            isSelectionMode = isMoveSelectionMode,
                                                            isSelected = app.packageName in selectedPackageNames,
                                                            onToggleSelect = { toggleSelect(app.packageName) },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        item(key = "bottom_padding_for_fab") {
                                            Spacer(modifier = Modifier.height(80.dp))
                                        }
                                    }
                                    AlphabetSlider(
                                        letterToIndex = letterToIndex,
                                        listState = listState,
                                        modifier = Modifier
                                            .widthIn(max = 36.dp)
                                            .padding(top = 24.dp, bottom = 24.dp, end = 8.dp),
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        horizontal = 20.dp,
                                        vertical = 24.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                ) {
                                    item(key = "top_padding") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    if (hasNoSearchResults) {
                                        item(key = "no_results") {
                                            Text(
                                                text = "No apps match \"$searchQuery\"",
                                                style = AppTypography.body(),
                                                color = textSecondary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 24.dp),
                                            )
                                        }
                                    } else if (hardMinimalMode) {
                                        if (groupsWithApps.isEmpty() && filteredUngrouped.isEmpty()) {
                                            item(key = "empty_apps") {
                                                EmptyAppsHint(
                                                    hasGroups = uiState.groups.isNotEmpty(),
                                                    onCreateGroupClick = {
                                                        showNewGroupDialog = true
                                                    },
                                                )
                                            }
                                        } else {
                                            groupsWithApps.forEach { group ->
                                                val groupApps = groupAppsFiltered(group)
                                                val isExpanded = group.id in expandedGroupIds
                                                item(key = "group_header_${group.id}") {
                                                    HardModeGroupHeader(
                                                        title = group.name,
                                                        isExpanded = isExpanded,
                                                        onClick = {
                                                            expandedGroupIds = if (isExpanded) expandedGroupIds - group.id else expandedGroupIds + group.id
                                                        },
                                                    )
                                                }
                                                if (isExpanded) {
                                                    items(
                                                        items = groupApps,
                                                        key = { it.packageName },
                                                    ) { app ->
                                                        HardModeListRow(
                                                            app = app,
                                                            onLaunch = { viewModel.launchApp(app.packageName) },
                                                            onLongClick = {
                                                                showAppGroupMenu = AppGroupMenuState(
                                                                    packageName = app.packageName,
                                                                    groupId = group.id,
                                                                    groups = uiState.groups,
                                                                )
                                                            },
                                                            indent = true,
                                                            isSelectionMode = isMoveSelectionMode,
                                                            isSelected = app.packageName in selectedPackageNames,
                                                            onToggleSelect = { toggleSelect(app.packageName) },
                                                        )
                                                    }
                                                }
                                            }
                                            if (filteredUngrouped.isNotEmpty()) {
                                                item(key = "ungrouped_header") {
                                                    HardModeSectionHeader("Apps", extraTopPadding = 16.dp)
                                                }
                                                items(
                                                    items = filteredUngrouped,
                                                    key = { it.packageName },
                                                ) { app ->
                                                    HardModeListRow(
                                                        app = app,
                                                        onLaunch = { viewModel.launchApp(app.packageName) },
                                                        onLongClick = {
                                                            showAppGroupMenu = AppGroupMenuState(
                                                                packageName = app.packageName,
                                                                groupId = null,
                                                                groups = uiState.groups,
                                                            )
                                                        },
                                                        isSelectionMode = isMoveSelectionMode,
                                                        isSelected = app.packageName in selectedPackageNames,
                                                        onToggleSelect = { toggleSelect(app.packageName) },
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        if (groupsWithMatches.isNotEmpty()) {
                                            item(key = "folders_grid") {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                                ) {
                                                    for (rowGroups in groupsWithMatches.chunked(2)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                12.dp
                                                            ),
                                                        ) {
                                                            for (group in rowGroups) {
                                                                val groupApps =
                                                                    groupAppsFiltered(group)
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .widthIn(min = 0.dp),
                                                                ) {
                                                                    GroupFolderCard(
                                                                        groupName = group.name,
                                                                        apps = groupApps,
                                                                        onFolderClick = {
                                                                            showFolderAppsSheet =
                                                                                Pair(
                                                                                    group,
                                                                                    groupApps
                                                                                )
                                                                        },
                                                                        onGroupLongClick = {
                                                                            showDeleteGroupDialog =
                                                                                Pair(
                                                                                    group.id,
                                                                                    group.name
                                                                                )
                                                                        },
                                                                        onAppClick = { app ->
                                                                            viewModel.launchApp(app.packageName)
                                                                        },
                                                                        onAppLongClick = { app ->
                                                                            showAppGroupMenu =
                                                                                AppGroupMenuState(
                                                                                    packageName = app.packageName,
                                                                                    groupId = group.id,
                                                                                    groups = uiState.groups,
                                                                                )
                                                                        },
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (filteredUngrouped.isEmpty()) {
                                            item(key = "empty_apps") {
                                                EmptyAppsHint(
                                                    hasGroups = uiState.groups.isNotEmpty(),
                                                    onCreateGroupClick = {
                                                        showNewGroupDialog = true
                                                    },
                                                )
                                            }
                                        } else {
                                            item(key = "apps_grid") {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                                ) {
                                                    val apps = filteredUngrouped
                                                    val rowCount = (apps.size + 3) / 4
                                                    for (rowIndex in 0 until rowCount) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            ),
                                                        ) {
                                                            for (col in 0 until 4) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .widthIn(min = 0.dp),
                                                                ) {
                                                                    val index = rowIndex * 4 + col
                                                                    if (index < apps.size) {
                                                                        val app = apps[index]
                                                                        AppGridCell(
                                                                            app = app,
                                                                            onLaunch = {
                                                                                viewModel.launchApp(app.packageName)
                                                                            },
                                                                            onLongClick = {
                                                                                showAppGroupMenu =
                                                                                    AppGroupMenuState(
                                                                                        packageName = app.packageName,
                                                                                        groupId = null,
                                                                                        groups = uiState.groups,
                                                                                    )
                                                                            },
                                                                            isSelectionMode = isMoveSelectionMode,
                                                                            isSelected = app.packageName in selectedPackageNames,
                                                                            onToggleSelect = { toggleSelect(app.packageName) },
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item(key = "bottom_padding_for_fab") {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(backgroundDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isMoveSelectionMode) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selectedPackageNames.isNotEmpty()) {
                            SecondaryButton(
                                text = "Move ${selectedPackageNames.size} app${if (selectedPackageNames.size == 1) "" else "s"}",
                                onClick = { showMoveToGroupDialog = true },
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) { }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SecondaryIconButton(
                            onClick = {
                                isMoveSelectionMode = false
                                selectedPackageNames = emptySet()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(AppIcons.Close),
                                    contentDescription = null,
                                    tint = textSecondary
                                )
                            },
                            contentDescription = "Cancel selection",
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        SecondaryIconButton(
                            onClick = {
                                isMoveSelectionMode = true
                                selectedPackageNames = emptySet()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(AppIcons.Checklist),
                                    contentDescription = null,
                                    tint = textSecondary
                                )
                            },
                            contentDescription = "Select apps to move",
                        )
                    }
                    PrimaryFAB(
                        onClick = { showNewGroupDialog = true },
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Add),
                                contentDescription = null,
                                tint = accentColors.onPrimary
                            )
                        },
                        contentDescription = "New group",
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        SecondaryIconButton(
                            onClick = onOpenSettings,
                            icon = {
                                Icon(
                                    painter = painterResource(AppIcons.Settings),
                                    contentDescription = null,
                                    tint = textSecondary
                                )
                            },
                            contentDescription = "Settings",
                        )
                    }
                }
            }
        }

        if (showTour && tourSteps.isNotEmpty()) {
            TourGuide(
                currentStep = tourStep,
                totalSteps = tourSteps.size,
                steps = tourSteps,
                onNext = {
                    if (tourStep >= tourSteps.size - 1) {
                        onTourComplete()
                    } else {
                        tourStep += 1
                    }
                },
                onBack = {
                    if (tourStep > 0) tourStep -= 1
                },
                onSkip = { onTourComplete() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (showNewGroupDialog) {
            CustomDialog(
                onDismissRequest = { showNewGroupDialog = false; newGroupName = "" },
                title = "Create a group",
                content = {
                    CustomTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = "Group name",
                        placeholder = "e.g. Social, Work, Games",
                        singleLine = true,
                    )
                },
                confirmButton = {
                    PrimaryIconButton(
                        onClick = {
                            viewModel.createGroup(newGroupName)
                            showNewGroupDialog = false
                            newGroupName = ""
                        },
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Check),
                                contentDescription = null,
                                tint = accentColors.onPrimary
                            )
                        },
                        contentDescription = "Create",
                        enabled = newGroupName.isNotBlank(),
                    )
                },
                dismissButton = {
                    SecondaryIconButton(
                        onClick = { showNewGroupDialog = false; newGroupName = "" },
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Close),
                                contentDescription = null,
                                tint = textPrimary
                            )
                        },
                        contentDescription = "Cancel",
                    )
                },
            )
        }

        if (showMoveToGroupDialog && selectedPackageNames.isNotEmpty()) {
            CustomDialog(
                onDismissRequest = { showMoveToGroupDialog = false },
                title = "Move ${selectedPackageNames.size} app${if (selectedPackageNames.size == 1) "" else "s"} to group",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.groups.forEach { g ->
                            SecondaryButton(
                                text = g.name,
                                onClick = {
                                    viewModel.addAppsToGroup(selectedPackageNames.toList(), g.id)
                                    selectedPackageNames = emptySet()
                                    isMoveSelectionMode = false
                                    showMoveToGroupDialog = false
                                },
                            )
                        }
                        if (uiState.groups.isEmpty()) {
                            Text(
                                "Create a group first using the + button.",
                                style = AppTypography.bodySmall(),
                                color = textSecondary,
                            )
                        }
                    }
                },
                dismissButton = {
                    SecondaryIconButton(
                        onClick = { showMoveToGroupDialog = false },
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Close),
                                contentDescription = null,
                                tint = textPrimary
                            )
                        },
                        contentDescription = "Cancel",
                    )
                },
            )
        }

        showAppGroupMenu?.let { state ->
            var selectedTab by remember(state.packageName) { mutableStateOf(0) }
            CustomDialog(
                onDismissRequest = { showAppGroupMenu = null },
                title = "App options",
                dismissButton = {
                    SecondaryIconButton(
                        onClick = { showAppGroupMenu = null },
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Close),
                                contentDescription = null,
                                tint = textPrimary
                            )
                        },
                        contentDescription = "Close",
                    )
                },
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTab == 0) accentColors.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedTab = 0 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Move to group",
                                    style = AppTypography.body(),
                                    color = if (selectedTab == 0) accentColors.primary else textSecondary,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTab == 1) accentColors.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedTab = 1 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "App config",
                                    style = AppTypography.body(),
                                    color = if (selectedTab == 1) accentColors.primary else textSecondary,
                                )
                            }
                        }
                        when (selectedTab) {
                            0 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (state.inGroup) {
                                    SecondaryButton(
                                        text = "Remove from current group",
                                        onClick = {
                                            state.groupId?.let {
                                                viewModel.removeAppFromGroup(
                                                    state.packageName,
                                                    it
                                                )
                                            }
                                            showAppGroupMenu = null
                                        },
                                    )
                                }
                                state.groups.filter { state.groupId != it.id }.forEach { g ->
                                    SecondaryButton(
                                        text = "Move to ${g.name}",
                                        onClick = {
                                            viewModel.addAppToGroup(state.packageName, g.id)
                                            showAppGroupMenu = null
                                        },
                                    )
                                }
                                if (state.groups.isEmpty() || (state.groups.size == 1 && state.inGroup)) {
                                    Text(
                                        "Tap \"New group\" below to create a group, then long-press an app to add it.",
                                        style = AppTypography.bodySmall(),
                                        color = textSecondary,
                                    )
                                }
                            }
                            1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SecondaryButton(
                                    text = "Go to app settings",
                                    onClick = {
                                        context.openAppInfo(state.packageName)
                                        showAppGroupMenu = null
                                    },
                                )
                                Text(
                                    "Open system app info: permissions, storage, force stop, uninstall.",
                                    style = AppTypography.bodySmall(),
                                    color = textSecondary,
                                )
                            }
                        }
                    }
                },
            )
        }

        showDeleteGroupDialog?.let { (groupId, groupName) ->
            CustomDialog(
                onDismissRequest = { showDeleteGroupDialog = null },
                title = "Delete group",
                content = {
                    Text(
                        text = "Delete \"$groupName\"? Apps in this group will move to All apps.",
                        style = AppTypography.body(),
                        color = textSecondary,
                    )
                },
                confirmButton = {
                    PrimaryIconButton(
                        onClick = {
                            viewModel.deleteGroup(groupId)
                            showDeleteGroupDialog = null
                        },
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Delete),
                                contentDescription = null,
                                tint = accentColors.onPrimary
                            )
                        },
                        contentDescription = "Delete group",
                    )
                },
                dismissButton = {
                    SecondaryIconButton(
                        onClick = { showDeleteGroupDialog = null },
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Close),
                                contentDescription = null,
                                tint = textPrimary
                            )
                        },
                        contentDescription = "Cancel",
                    )
                },
            )
        }

        showFolderAppsSheet?.let { (group, groupApps) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showFolderAppsSheet = null },
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                    ),
                ) {
                    FolderAppsSheet(
                        folderName = group.name,
                        apps = groupApps,
                        onDismiss = { showFolderAppsSheet = null },
                        onAppClick = { app ->
                            viewModel.launchApp(app.packageName)
                            showFolderAppsSheet = null
                        },
                        onAppLongClick = { app ->
                            showFolderAppsSheet = null
                            showAppGroupMenu = AppGroupMenuState(
                                packageName = app.packageName,
                                groupId = group.id,
                                groups = uiState.groups,
                            )
                        },
                        closeIcon = {
                            Icon(
                                painter = painterResource(AppIcons.Close),
                                contentDescription = "Close",
                                tint = textPrimary,
                            )
                        },
                    )
                }
            }
        }
    }
