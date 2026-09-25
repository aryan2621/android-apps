package com.instashow.render

/** What the user changed in the editor. Applies on top of any template. */
data class StoryStyle(
    val textScale: Float = 1f,
    val textColor: Int? = null,
    val backgroundColor: Int? = null,
    val frameScale: Float = 0.86f,
    /** Replaces the template's headline. Blank hides it. Null keeps the template's own. */
    val headline: String? = null,
    /** Replaces the template's caption. Blank hides it. Null keeps the template's own. */
    val caption: String? = null,
)
