package com.instashow

import android.content.Context
import com.instashow.auth.AuthRepository
import com.instashow.health.HealthRepository
import com.instashow.photo.PhotoStore
import com.instashow.render.StoryFonts
import com.instashow.render.StoryRenderer
import com.instashow.settings.SettingsRepository
import com.instashow.share.StoryShare
import com.instashow.template.TemplateCatalog

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val authRepository = AuthRepository(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val healthRepository = HealthRepository(appContext)
    val photoStore = PhotoStore(appContext)
    val templateCatalog = TemplateCatalog(appContext)
    val storyRenderer = StoryRenderer(StoryFonts(appContext))
    val storyShare = StoryShare(appContext)
}
