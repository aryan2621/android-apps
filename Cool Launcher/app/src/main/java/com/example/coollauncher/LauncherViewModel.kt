package com.example.coollauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coollauncher.data.AppGroup
import com.example.coollauncher.data.AppInfo
import com.example.coollauncher.data.AppRepository
import com.example.coollauncher.data.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID

data class LauncherUiState(
    val apps: List<AppInfo> = emptyList(),
    val groups: List<AppGroup> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
)

class LauncherViewModel(
    private val appRepository: AppRepository,
    private val groupsRepository: GroupsRepository,
) : ViewModel() {

    private val appsFlow = MutableStateFlow<List<AppInfo>>(emptyList())
    private val loadingFlow = MutableStateFlow(true)
    private val refreshingFlow = MutableStateFlow(false)

    val uiState: StateFlow<LauncherUiState> = combine(
        appsFlow,
        groupsRepository.groupsFlow,
        loadingFlow,
        refreshingFlow,
    ) { apps, groups, isLoading, isRefreshing ->
        LauncherUiState(apps = apps, groups = groups, isLoading = isLoading, isRefreshing = isRefreshing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState(isLoading = true)
    )

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            loadingFlow.value = true
            try {
                val list = withContext(Dispatchers.IO) {
                    var apps = appRepository.getCachedApps()
                    if (apps.isEmpty()) {
                        apps = appRepository.getLaunchableApps()
                        appRepository.saveToCache(apps)
                    }
                    apps
                }
                appsFlow.value = list
            } finally {
                loadingFlow.value = false
            }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            refreshingFlow.value = true
            try {
                val list = withContext(Dispatchers.IO) {
                    val apps = appRepository.getLaunchableApps()
                    appRepository.saveToCache(apps)
                    apps
                }
                appsFlow.value = list
            } finally {
                refreshingFlow.value = false
            }
        }
    }

    fun launchApp(packageName: String): Boolean {
        return appRepository.launchApp(packageName)
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            val current = groupsRepository.getGroups()
            val newGroup = AppGroup(id = UUID.randomUUID().toString(), name = name.trim())
            if (newGroup.name.isNotEmpty()) {
                groupsRepository.saveGroups(current + newGroup)
            }
        }
    }

    fun addAppToGroup(packageName: String, groupId: String) {
        viewModelScope.launch {
            val current = groupsRepository.getGroups()
            val updated = current.map { g ->
                if (g.id == groupId && packageName !in g.appPackageNames) {
                    g.copy(appPackageNames = g.appPackageNames + packageName)
                } else g
            }
            groupsRepository.saveGroups(updated)
        }
    }

    fun addAppsToGroup(packageNames: List<String>, groupId: String) {
        if (packageNames.isEmpty()) return
        viewModelScope.launch {
            val current = groupsRepository.getGroups()
            val updated = current.map { g ->
                if (g.id == groupId) {
                    val toAdd = packageNames.filter { it !in g.appPackageNames }
                    if (toAdd.isEmpty()) g else g.copy(appPackageNames = g.appPackageNames + toAdd)
                } else g
            }
            groupsRepository.saveGroups(updated)
        }
    }

    fun removeAppFromGroup(packageName: String, groupId: String) {
        viewModelScope.launch {
            val current = groupsRepository.getGroups()
            val updated = current.map { g ->
                if (g.id == groupId) {
                    g.copy(appPackageNames = g.appPackageNames.filter { it != packageName })
                } else g
            }
            groupsRepository.saveGroups(updated)
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            val current = groupsRepository.getGroups()
            groupsRepository.saveGroups(current.filter { it.id != groupId })
        }
    }

    fun renameGroup(groupId: String, newName: String) {
        viewModelScope.launch {
            val current = groupsRepository.getGroups()
            val updated = current.map { g ->
                if (g.id == groupId) g.copy(name = newName.trim()) else g
            }
            if (newName.trim().isNotEmpty()) {
                groupsRepository.saveGroups(updated)
            }
        }
    }
}

class LauncherViewModelFactory(
    private val appRepository: AppRepository,
    private val groupsRepository: GroupsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == LauncherViewModel::class.java) {
            return LauncherViewModel(appRepository, groupsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
