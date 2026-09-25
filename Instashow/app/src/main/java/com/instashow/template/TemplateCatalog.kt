package com.instashow.template

import android.content.Context

class TemplateCatalog(private val context: Context) {
    fun load(): List<StoryTemplate> {
        val names = context.assets.list(DIRECTORY).orEmpty()
            .filter { it.endsWith(".json") }
            .sorted()
        val templates = names.map { name ->
            val text = context.assets.open("$DIRECTORY/$name").bufferedReader().use { it.readText() }
            val template = TemplateJson.parse(text)
            val issues = TemplateJson.validate(template)
            check(issues.isEmpty()) { "$name: ${issues.joinToString()}" }
            template
        }
        check(templates.isNotEmpty()) { "No story templates found" }
        val duplicates = templates.groupBy { it.id }.filter { it.value.size > 1 }.keys
        check(duplicates.isEmpty()) { "Duplicate template ids: $duplicates" }
        return templates
    }

    private companion object {
        const val DIRECTORY = "templates"
    }
}
