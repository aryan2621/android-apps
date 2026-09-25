package com.instashow.story

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.instashow.BuildConfig
import com.instashow.health.DayStats
import com.instashow.health.GeoPoint
import com.instashow.health.HealthAvailability
import com.instashow.health.HealthRepository
import com.instashow.health.HealthSnapshot
import com.instashow.health.SampleHealth
import com.instashow.photo.PhotoStore
import com.instashow.render.StoryRenderer
import com.instashow.render.StoryStyle
import com.instashow.settings.Settings
import com.instashow.settings.SettingsRepository
import com.instashow.settings.Units
import com.instashow.template.StoryTemplate
import com.instashow.template.TemplateCatalog
import com.instashow.template.fits
import com.instashow.template.hasDataFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

enum class PhotoProblem {
    Unreadable,
    CameraMissing,
}

class StoryViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val healthRepository: HealthRepository,
    private val templateCatalog: TemplateCatalog,
    private val photoStore: PhotoStore,
    private val storyRenderer: StoryRenderer,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val photoPath: StateFlow<String?> = savedStateHandle.getStateFlow(PHOTO_PATH, null)
    private val workoutId: StateFlow<String?> = savedStateHandle.getStateFlow(WORKOUT_ID, null)
    val hasPhoto: StateFlow<Boolean> = photoPath.map { it != null && it != NO_PHOTO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val settings: StateFlow<Settings> = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        Settings(),
    )

    private val _health = MutableStateFlow(
        HealthSnapshot(
            availability = HealthAvailability.Unavailable,
            permissionsGranted = false,
            stats = DayStats(),
            loading = true,
        ),
    )
    val health: StateFlow<HealthSnapshot> = _health.asStateFlow()

    /** Routes the user unlocked one workout at a time through Health Connect's consent screen. */
    private val consentedRoutes = MutableStateFlow<Map<String, List<GeoPoint>>>(emptyMap())

    val storyData: StateFlow<StoryData> = combine(_health, settings, workoutId, consentedRoutes) { health, settings, id, routes ->
        val workouts = health.workouts.map { found ->
            routes[found.id]?.let { found.copy(route = it, routeConsentNeeded = false) } ?: found
        }
        StoryData(
            day = health.stats,
            stepGoal = settings.stepGoal,
            workout = id?.let { wanted -> workouts.firstOrNull { it.id == wanted } },
            workouts = workouts,
            units = settings.units,
            date = LocalDate.now(),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StoryData())

    private val allTemplates = MutableStateFlow<List<StoryTemplate>>(emptyList())

    /** Templates that suit the current story, so a lifting session never gets an empty route layout. */
    val templates: StateFlow<List<StoryTemplate>> = combine(allTemplates, storyData, hasPhoto) { templates, data, photo ->
        templates.filter { it.fits(data.isWorkout, data.hasRoute, photo) && it.hasDataFor(data) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _catalogError = MutableStateFlow(false)
    val catalogError: StateFlow<Boolean> = _catalogError.asStateFlow()

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing.asStateFlow()

    private val _photoProblem = MutableStateFlow<PhotoProblem?>(null)
    val photoProblem: StateFlow<PhotoProblem?> = _photoProblem.asStateFlow()

    private val _pages = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val pages: StateFlow<Map<String, Bitmap>> = _pages.asStateFlow()

    private val _style = MutableStateFlow(StoryStyle())
    val style: StateFlow<StoryStyle> = _style.asStateFlow()

    private val _rendering = MutableStateFlow(false)
    val rendering: StateFlow<Boolean> = _rendering.asStateFlow()

    private val _renderFailed = MutableStateFlow(false)
    val renderFailed: StateFlow<Boolean> = _renderFailed.asStateFlow()

    private val renderJobs = mutableMapOf<String, Job>()
    private val renderedKey = mutableMapOf<String, String>()
    private val retired = mutableMapOf<String, Bitmap>()

    init {
        viewModelScope.launch {
            try {
                allTemplates.value = withContext(Dispatchers.IO) { templateCatalog.load() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _catalogError.value = true
            }
        }
        refreshHealth()
    }

    fun requiredPermissions(): Set<String> = healthRepository.requiredPermissions()

    fun refreshHealth() {
        viewModelScope.launch { reloadHealth() }
    }

    suspend fun reloadHealth() {
        _health.value = _health.value.copy(loading = true, failed = false)
        val useSample = BuildConfig.DEBUG && settingsRepository.settings.first().sampleData
        _health.value = if (useSample) SampleHealth.snapshot() else healthRepository.snapshot()
    }

    fun setUnits(units: Units) {
        viewModelScope.launch { settingsRepository.setUnits(units) }
    }

    fun setStepGoal(goal: Long) {
        viewModelScope.launch { settingsRepository.setStepGoal(goal) }
    }

    fun setSampleData(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSampleData(enabled)
            reloadHealth()
        }
    }

    /** Makes the next story about one workout. Null goes back to the whole day. */
    fun selectWorkout(id: String?) {
        if (savedStateHandle.get<String>(WORKOUT_ID) == id) return
        savedStateHandle[WORKOUT_ID] = id
    }

    fun onRouteConsent(id: String, route: List<GeoPoint>) {
        if (route.size < 2) return
        consentedRoutes.update { it + (id to route) }
    }

    fun createCaptureUri(): Uri = photoStore.newCaptureTarget()

    fun onCameraUnavailable() {
        _photoProblem.value = PhotoProblem.CameraMissing
    }

    fun clearPhotoProblem() {
        _photoProblem.value = null
    }

    fun importPhoto(uri: Uri) {
        viewModelScope.launch {
            _importing.value = true
            _photoProblem.value = null
            try {
                val file = withContext(Dispatchers.IO) { photoStore.import(uri) }
                clearRenderedPages()
                savedStateHandle[PHOTO_PATH] = file.absolutePath
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _photoProblem.value = PhotoProblem.Unreadable
            } finally {
                _importing.value = false
            }
        }
    }

    /** Stories work without a photo too: the template's own gradient fills the frame. */
    fun useNoPhoto() {
        clearRenderedPages()
        savedStateHandle[PHOTO_PATH] = NO_PHOTO
    }

    fun setTextScale(scale: Float) {
        val clamped = scale.coerceIn(TEXT_SCALE_MIN, TEXT_SCALE_MAX)
        val next = (clamped / TEXT_SCALE_STEP).roundToInt() * TEXT_SCALE_STEP
        if (abs(next - _style.value.textScale) < 0.001f) return
        updateStyle { it.copy(textScale = next) }
    }

    fun setTextColor(color: Int?) {
        updateStyle { it.copy(textColor = color?.let(::opaque)) }
    }

    fun setBackgroundColor(color: Int?) {
        updateStyle { it.copy(backgroundColor = color?.let(::opaque)) }
    }

    fun setFrameScale(scale: Float) {
        if (_style.value.backgroundColor == null) return
        val clamped = scale.coerceIn(FRAME_SCALE_MIN, FRAME_SCALE_MAX)
        val next = (clamped / FRAME_SCALE_STEP).roundToInt() * FRAME_SCALE_STEP
        if (abs(next - _style.value.frameScale) < 0.001f) return
        updateStyle { it.copy(frameScale = next) }
    }

    fun setHeadline(text: String?) {
        updateStyle { it.copy(headline = text?.take(MAX_HEADLINE)) }
    }

    fun setCaption(text: String?) {
        updateStyle { it.copy(caption = text?.take(MAX_CAPTION)) }
    }

    private fun updateStyle(change: (StoryStyle) -> StoryStyle) {
        val next = change(_style.value)
        if (next == _style.value) return
        _style.value = next
        dropInFlightRenders()
    }

    fun render(templateId: String) {
        val key = renderKey()
        val cached = _pages.value[templateId]
        if (cached != null && !cached.isRecycled && renderedKey[templateId] == key) return
        if (renderJobs[templateId]?.isActive == true) return
        val template = allTemplates.value.firstOrNull { it.id == templateId } ?: return
        val path = savedStateHandle.get<String>(PHOTO_PATH)
        val file = path?.takeUnless { it == NO_PHOTO }?.let(::File)
        if (path == null || (file != null && !file.exists())) {
            _renderFailed.value = true
            return
        }
        renderJobs[templateId] = viewModelScope.launch {
            val job = coroutineContext[Job]
            _rendering.value = true
            _renderFailed.value = false
            val data = storyData.value
            val style = _style.value
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    renderBitmap(template, file, data, style, StoryRenderer.PREVIEW_WIDTH, StoryRenderer.PREVIEW_HEIGHT)
                }
                if (!isActive || renderKey() != key) {
                    bitmap.recycle()
                    return@launch
                }
                _pages.update { current ->
                    val displayed = current[templateId]
                    if (displayed != null && displayed !== bitmap) {
                        val older = retired.put(templateId, displayed)
                        if (older != null && older !== displayed && !older.isRecycled) older.recycle()
                    }
                    current + (templateId to bitmap)
                }
                renderedKey[templateId] = key
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _renderFailed.value = true
            } finally {
                if (renderJobs[templateId] === job) renderJobs.remove(templateId)
                _rendering.value = renderJobs.isNotEmpty()
            }
        }
    }

    /** Full 1080×1920 render for saving and sharing. Previews are drawn smaller to save memory. */
    suspend fun renderFull(templateId: String): Bitmap = withContext(Dispatchers.Default) {
        val template = allTemplates.value.first { it.id == templateId }
        val file = savedStateHandle.get<String>(PHOTO_PATH)?.takeUnless { it == NO_PHOTO }?.let(::File)
        renderBitmap(template, file, storyData.value, _style.value, StoryRenderer.STORY_WIDTH, StoryRenderer.STORY_HEIGHT)
    }

    private fun renderBitmap(
        template: StoryTemplate,
        file: File?,
        data: StoryData,
        style: StoryStyle,
        width: Int,
        height: Int,
    ): Bitmap {
        val photo = file?.let(photoStore::load)
        try {
            return storyRenderer.render(template, photo, data, style, width, height)
        } finally {
            if (photo != null && !photo.isRecycled) photo.recycle()
        }
    }

    private fun renderKey(): String =
        "${_style.value.hashCode()}:${storyData.value.hashCode()}:${savedStateHandle.get<String>(PHOTO_PATH)}"

    private fun dropInFlightRenders() {
        renderJobs.values.forEach { it.cancel() }
        renderJobs.clear()
        _rendering.value = false
    }

    private fun opaque(color: Int): Int = color or 0xFF000000.toInt()

    private fun clearRenderedPages() {
        dropInFlightRenders()
        val stale = _pages.value.values + retired.values
        _pages.value = emptyMap()
        retired.clear()
        renderedKey.clear()
        stale.forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    override fun onCleared() {
        clearRenderedPages()
    }

    companion object {
        const val TEXT_SCALE_MIN = 0.8f
        const val TEXT_SCALE_MAX = 1.6f
        const val TEXT_SCALE_STEP = 0.05f
        const val FRAME_SCALE_MIN = 0.62f
        const val FRAME_SCALE_MAX = 0.92f
        const val FRAME_SCALE_STEP = 0.02f
        const val MAX_HEADLINE = 28
        const val MAX_CAPTION = 48
        private const val PHOTO_PATH = "photo_path"
        private const val WORKOUT_ID = "workout_id"
        private const val NO_PHOTO = "none"

        fun factory(
            healthRepository: HealthRepository,
            templateCatalog: TemplateCatalog,
            photoStore: PhotoStore,
            storyRenderer: StoryRenderer,
            settingsRepository: SettingsRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return StoryViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    healthRepository = healthRepository,
                    templateCatalog = templateCatalog,
                    photoStore = photoStore,
                    storyRenderer = storyRenderer,
                    settingsRepository = settingsRepository,
                ) as T
            }
        }
    }
}
