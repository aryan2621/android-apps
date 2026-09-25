package com.instashow.render

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.instashow.R

/** Typefaces used on the story canvas. Loaded once, off the main thread, on first use. */
class StoryFonts(context: Context) {
    private val appContext = context.applicationContext

    val display: Typeface by lazy { load(R.font.anton_regular, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)) }
    val bold: Typeface by lazy { load(R.font.outfit_bold, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)) }
    val semibold: Typeface by lazy { load(R.font.outfit_semibold, Typeface.create("sans-serif-medium", Typeface.NORMAL)) }
    val medium: Typeface by lazy { load(R.font.outfit_medium, Typeface.create("sans-serif-medium", Typeface.NORMAL)) }
    val regular: Typeface by lazy { load(R.font.outfit_regular, Typeface.SANS_SERIF) }
    val mono: Typeface by lazy { Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) }
    val serif: Typeface by lazy { load(R.font.instrument_serif_italic, Typeface.create(Typeface.SERIF, Typeface.ITALIC)) }

    fun named(name: String): Typeface = when (name) {
        "display" -> display
        "bold" -> bold
        "medium" -> medium
        "regular" -> regular
        "serif" -> serif
        "mono" -> mono
        else -> bold
    }

    private fun load(id: Int, fallback: Typeface): Typeface =
        runCatching { ResourcesCompat.getFont(appContext, id) }.getOrNull() ?: fallback
}
