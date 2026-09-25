package com.instashow.template

import com.instashow.health.StatFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StoryTemplate(
    val id: String,
    val name: String,
    val background: TemplateBackground,
    val photo: PhotoSlot,
    val stats: List<StatSlot>,
    /** Fills cells with every supporting stat the phone recorded today, so a full-day layout has no gaps. */
    val grid: StatGrid? = null,
    /** Hidden unless the story has at least this many supporting stats, so data-heavy layouts never look empty. */
    val minStats: Int = 0,
    val overlay: String? = null,
    val scrim: ScrimSlot? = null,
    val quote: QuoteSlot? = null,
    val panels: List<GlassPanel> = emptyList(),
    /** Which stories this layout suits: "day", "workout", or both when empty. */
    val modes: List<String> = emptyList(),
    /** Hide this layout unless the workout has a GPS route. */
    val needsRoute: Boolean = false,
    /** Hide this layout for stories without a photo, when its design is built around a framed photo. */
    val needsPhoto: Boolean = false,
    /** Still looks finished with no stats at all, as a photo, date and headline. */
    val statsOptional: Boolean = false,
    val route: RouteSlot? = null,
    val texts: List<TextSlot> = emptyList(),
    val rings: List<RingSlot> = emptyList(),
    /** Photo treatment: "none", "mono", "warm", "cool", "fade", "punch" or "duotone". */
    val filter: String = "none",
    val vignette: Float = 0f,
    /** Shadow and highlight colors for the "duotone" filter. */
    val duotone: List<String> = emptyList(),
    /** Film grain over the whole story, 0 to 1. */
    val grain: Float = 0f,
    val shapes: List<ShapeSlot> = emptyList(),
)

@Serializable
data class TemplateBackground(
    /** "solid", "gradient", or "mesh" (first color is the base, the rest are soft glowing blobs). */
    val type: String,
    val colors: List<String>,
    val angle: Float = 180f,
)

@Serializable
data class PhotoSlot(
    /** "rect", "circle", "arch" (rounded top), or "none" for designs without a photo. */
    val shape: String,
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val radius: Float = 0f,
    /** Tilt in degrees, like a print dropped on a table. */
    val rotate: Float = 0f,
    val border: String? = null,
    val borderWidth: Float = 0f,
    val shadow: Boolean = false,
)

@Serializable
data class ScrimSlot(
    val edge: String,
    val height: Float,
    val color: String,
    val maxAlpha: Float,
)

@Serializable
data class QuoteSlot(
    val text: String,
    val left: Float,
    val top: Float,
    val size: Float,
    val color: String,
    val align: String = "left",
    val tracking: Float = 0.14f,
    val style: String = "bold",
    val caption: String? = null,
    /** Wrap width as a fraction of the canvas. Text wider than this is shrunk to fit. */
    val maxWidth: Float = 0.92f,
)

@Serializable
data class GlassPanel(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val radius: Float,
    val fill: String,
    val fillAlpha: Float,
    val stroke: String,
    val strokeAlpha: Float,
    val blur: Boolean = true,
)

@Serializable
data class StatSlot(
    val stat: String,
    /** Label under the value. Blank or "auto" uses the stat's own label, which matters for hero/second/third. */
    val label: String = "auto",
    /** Horizontal anchor. For align "left" this is the left edge. For "center" this is the text center. */
    val left: Float,
    val top: Float,
    val valueSize: Float,
    val labelSize: Float,
    val color: String,
    val align: String = "left",
    /** Font for the value: "display", "bold", "medium", "regular" or "serif". */
    val font: String = "display",
    /** Unit size relative to the value, such as the "km" after "5.21". */
    val unitScale: Float = 0.42f,
    /** Put the label above the value instead of below. */
    val labelAbove: Boolean = false,
    val accent: String? = null,
    val outline: Boolean = false,
    val shadow: Boolean = false,
)

@Serializable
data class StatGrid(
    val left: Float,
    val top: Float,
    val width: Float,
    val columns: Int = 2,
    val rows: Int = 4,
    /** Height of one row, as a fraction of the canvas height. */
    val rowHeight: Float = 0.08f,
    val valueSize: Float = 64f,
    val labelSize: Float = 20f,
    val color: String,
    val font: String = "display",
    val unitScale: Float = 0.42f,
    val labelAbove: Boolean = false,
    val accent: String? = null,
    /** Where to start in the supporting stats: 0 is "second", 1 is "third" and so on. */
    val from: Int = 0,
    /** Hairlines between rows, in this color. */
    val divider: String? = null,
)

@Serializable
data class TextSlot(
    /** Supports {title}, {kind}, {date}, {day}, {longdate}, {goal}, {workoutlist}, {steps}, {distance}, {hero}, {time}. Uppercase tokens give uppercase values. */
    val text: String,
    val left: Float,
    val top: Float,
    val size: Float,
    val color: String,
    val align: String = "left",
    val font: String = "bold",
    val tracking: Float = 0f,
    val alpha: Float = 1f,
    /** Draws a rounded pill behind the text. */
    val pill: String? = null,
    val pillAlpha: Float = 1f,
    val modes: List<String> = emptyList(),
    /** The user's headline from the editor replaces this text. */
    val editable: Boolean = false,
    /** Draws the letters as outlines. */
    val outline: Boolean = false,
    /** Rotation in degrees around the anchor. -90 runs the text up the left edge. */
    val rotate: Float = 0f,
    /** Stacks the text this many times; all but the last copy are outlines. */
    val repeat: Int = 1,
    /** Distance between stacked copies, as a fraction of the text size. */
    val repeatGap: Float = 0.9f,
    val shadow: Boolean = false,
    /** Fit width as a fraction of the canvas. 0 uses the space left of the anchor. */
    val maxWidth: Float = 0f,
)

@Serializable
data class ShapeSlot(
    /** "rect", "circle", "line", "barcode", "grid", "horizon" or "dots". */
    val type: String,
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val color: String,
    val alpha: Float = 1f,
    val radius: Float = 0f,
    /** 0 fills the shape; above 0 draws an outline this thick. */
    val stroke: Float = 0f,
    val dash: Boolean = false,
    /** "back" draws behind the photo, "front" above it. */
    val layer: String = "front",
    val rotate: Float = 0f,
    /** Lines or dots per row for grid, horizon and dots. */
    val count: Int = 8,
)

@Serializable
data class RouteSlot(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val color: String,
    val stroke: Float = 10f,
    val glow: Boolean = true,
    val markers: Boolean = true,
    /** "solid", "dotted" or "double". */
    val style: String = "solid",
)

@Serializable
data class RingSlot(
    /** "goal" fills toward the step goal. */
    val stat: String = "goal",
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val stroke: Float,
    val color: String,
    val track: String,
    val trackAlpha: Float = 0.25f,
)

/** Colors are "#RRGGBB" or Android-style "#AARRGGBB" (alpha first). */
object TemplateJson {
    private val json = Json { ignoreUnknownKeys = true }

    val statKeys = StatFormat.keys
    private val fonts = setOf("display", "bold", "medium", "regular", "serif", "mono")
    private val filters = setOf("none", "mono", "warm", "cool", "fade", "punch", "duotone")
    private val shapeTypes = setOf("rect", "circle", "line", "barcode", "grid", "horizon", "dots")
    private val modeNames = setOf("day", "workout")

    fun parse(text: String): StoryTemplate = json.decodeFromString(text)

    fun validate(template: StoryTemplate): List<String> {
        val issues = mutableListOf<String>()
        if (!template.id.matches(ID_PATTERN)) issues += "id must be lowercase words separated by hyphens"
        if (template.name.isBlank() || template.name.length > 40) issues += "name must be 1–40 characters"
        if (template.background.type !in setOf("solid", "gradient", "mesh")) issues += "background type must be solid, gradient or mesh"
        if (template.background.colors.isEmpty()) issues += "background needs a color"
        if (template.background.type != "solid" && template.background.colors.size < 2) {
            issues += "gradient needs two colors"
        }
        template.background.colors.forEach { color ->
            if (!color.matches(HEX_COLOR)) issues += "invalid color $color"
        }
        template.overlay?.let { overlay ->
            if (!overlay.matches(HEX_COLOR)) issues += "invalid overlay $overlay"
        }
        if (template.filter !in filters) issues += "unknown filter ${template.filter}"
        if (template.filter == "duotone" && template.duotone.size != 2) issues += "duotone needs two colors"
        template.duotone.forEach { if (!it.matches(HEX_COLOR)) issues += "invalid duotone color $it" }
        if (template.grain !in 0f..1f) issues += "grain must sit between 0 and 1"
        template.shapes.forEach { shape ->
            if (shape.type !in shapeTypes) issues += "unknown shape ${shape.type}"
            if (shape.left !in -0.5f..1.5f || shape.top !in -0.5f..1.5f) issues += "shape must start near the canvas"
            if (shape.width < 0f || shape.height < 0f) issues += "shape size can't be negative"
            if (!shape.color.matches(HEX_COLOR)) issues += "invalid shape color ${shape.color}"
            if (shape.layer !in setOf("back", "front")) issues += "shape layer must be back or front"
            if (shape.alpha !in 0f..1f) issues += "shape alpha must sit between 0 and 1"
        }
        if (template.vignette !in 0f..1f) issues += "vignette must sit between 0 and 1"
        template.modes.forEach { if (it !in modeNames) issues += "unknown mode $it" }
        template.scrim?.let { scrim ->
            if (scrim.edge !in setOf("top", "bottom")) issues += "scrim edge must be top or bottom"
            if (scrim.height !in 0.05f..1f) issues += "scrim height must sit between 0.05 and 1"
            if (!scrim.color.matches(HEX_COLOR)) issues += "invalid scrim color ${scrim.color}"
            if (scrim.maxAlpha !in 0f..1f) issues += "scrim alpha must sit between 0 and 1"
        }
        template.quote?.let { quote ->
            if (quote.text.isBlank() || quote.text.length > 40) issues += "quote must be 1–40 characters"
            if (quote.left !in 0f..1f || quote.top !in 0f..1f) issues += "quote anchor must be inside the canvas"
            if (quote.size !in 1f..280f) issues += "quote size is out of range"
            if (!quote.color.matches(HEX_COLOR)) issues += "invalid quote color ${quote.color}"
            if (quote.align !in setOf("left", "center")) issues += "quote align must be left or center"
            if (quote.style !in setOf("bold", "italic", "display")) issues += "quote style must be bold, italic or display"
            quote.caption?.let { caption ->
                if (caption.isBlank() || caption.length > 48) issues += "caption must be 1–48 characters"
            }
            if (quote.tracking !in -0.1f..0.5f) issues += "quote tracking is out of range"
        }
        template.panels.forEach { panel ->
            if (!inUnitRange(panel.left, panel.top, panel.width, panel.height)) {
                issues += "glass panel must sit inside the canvas"
            }
            if (panel.radius !in 0f..400f) issues += "glass radius is out of range"
            if (!panel.fill.matches(HEX_COLOR)) issues += "invalid glass fill ${panel.fill}"
            if (!panel.stroke.matches(HEX_COLOR)) issues += "invalid glass stroke ${panel.stroke}"
            if (panel.fillAlpha !in 0f..1f || panel.strokeAlpha !in 0f..1f) {
                issues += "glass alpha must sit between 0 and 1"
            }
        }
        if (template.photo.shape !in setOf("rect", "circle", "arch", "none")) issues += "photo shape must be rect, circle, arch or none"
        template.photo.border?.let { if (!it.matches(HEX_COLOR)) issues += "invalid photo border $it" }
        if (template.photo.shape != "none" &&
            !inUnitRange(template.photo.left, template.photo.top, template.photo.width, template.photo.height)
        ) {
            issues += "photo slot must sit inside the canvas"
        }
        template.grid?.let { grid ->
            if (grid.columns !in 1..4 || grid.rows !in 1..8) issues += "grid must have 1–4 columns and 1–8 rows"
            if (!inUnitRange(grid.left, grid.top, grid.width, grid.rowHeight * grid.rows)) issues += "grid must sit inside the canvas"
            if (grid.from !in StatFormat.supportingKeys.indices) issues += "grid must start at a supporting stat"
            if (grid.valueSize !in 1f..400f || grid.labelSize !in 0f..200f) issues += "grid text size is out of range"
            if (!grid.color.matches(HEX_COLOR)) issues += "invalid grid color ${grid.color}"
            grid.accent?.let { if (!it.matches(HEX_COLOR)) issues += "invalid grid accent $it" }
            grid.divider?.let { if (!it.matches(HEX_COLOR)) issues += "invalid grid divider $it" }
            if (grid.font !in fonts) issues += "unknown font ${grid.font}"
        }
        if (template.stats.isEmpty() && template.grid == null && template.route == null && template.texts.none { '{' in it.text }) {
            issues += "template needs a stat, a route or a text with stat tokens"
        }
        template.stats.forEach { slot ->
            if (slot.stat !in statKeys) issues += "unknown stat ${slot.stat}"
            if (slot.align !in setOf("left", "center", "right")) issues += "align must be left, center or right"
            if (slot.left !in 0f..1f || slot.top !in 0f..1f) issues += "stat anchor must be inside the canvas"
            if (slot.valueSize !in 1f..400f || slot.labelSize !in 0f..200f) issues += "stat text size is out of range"
            if (!slot.color.matches(HEX_COLOR)) issues += "invalid stat color ${slot.color}"
            if (slot.font !in fonts) issues += "unknown font ${slot.font}"
            slot.accent?.let { if (!it.matches(HEX_COLOR)) issues += "invalid stat accent $it" }
        }
        template.texts.forEach { text ->
            if (text.text.isBlank()) issues += "text is blank"
            if (text.left !in 0f..1f || text.top !in 0f..1f) issues += "text anchor must be inside the canvas"
            if (text.size !in 1f..400f) issues += "text size is out of range"
            if (!text.color.matches(HEX_COLOR)) issues += "invalid text color ${text.color}"
            if (text.align !in setOf("left", "center", "right")) issues += "text align must be left, center or right"
            if (text.font !in fonts) issues += "unknown font ${text.font}"
            text.pill?.let { if (!it.matches(HEX_COLOR)) issues += "invalid pill color $it" }
            text.modes.forEach { if (it !in modeNames) issues += "unknown mode $it" }
            if (text.repeat !in 1..12) issues += "text repeat must sit between 1 and 12"
        }
        template.route?.let { route ->
            if (!inUnitRange(route.left, route.top, route.width, route.height)) issues += "route must sit inside the canvas"
            if (!route.color.matches(HEX_COLOR)) issues += "invalid route color ${route.color}"
            if (route.stroke !in 1f..60f) issues += "route stroke is out of range"
            if (route.style !in setOf("solid", "dotted", "double")) issues += "route style must be solid, dotted or double"
        }
        template.rings.forEach { ring ->
            if (ring.stat != "goal") issues += "ring stat must be goal"
            if (ring.centerX !in 0f..1f || ring.centerY !in 0f..1f) issues += "ring center must be inside the canvas"
            if (ring.radius !in 0.01f..0.5f) issues += "ring radius is out of range"
            if (!ring.color.matches(HEX_COLOR) || !ring.track.matches(HEX_COLOR)) issues += "invalid ring color"
        }
        return issues
    }

    private fun inUnitRange(left: Float, top: Float, width: Float, height: Float): Boolean {
        if (width <= 0f || height <= 0f) return false
        if (left < -EPSILON || top < -EPSILON) return false
        return left + width <= 1f + EPSILON && top + height <= 1f + EPSILON
    }

    private val ID_PATTERN = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")
    private val HEX_COLOR = Regex("^#(?:[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
    private const val EPSILON = 0.001f
}

/** The line the editor's headline field changes: the quote, or else the first editable text. */
val StoryTemplate.headlineText: String?
    get() = quote?.text ?: texts.firstOrNull { it.editable }?.text

/** Goal rings need today's steps; without them those designs look empty. */
fun StoryTemplate.hasDataFor(data: com.instashow.story.StoryData): Boolean {
    if (rings.isNotEmpty() && data.day.steps == null) return false
    if (minStats > 0 && com.instashow.health.StatFormat.supportingKeys.count { com.instashow.health.StatFormat.value(it, data).available } < minStats) {
        return false
    }
    if (!statsOptional && !com.instashow.health.StatFormat.value("hero", data).available) return false
    return true
}

/** Whether a template suits the story being made. */
fun StoryTemplate.fits(isWorkout: Boolean, hasRoute: Boolean, hasPhoto: Boolean = true): Boolean {
    if (needsRoute && !hasRoute) return false
    if (needsPhoto && !hasPhoto) return false
    if (modes.isEmpty()) return true
    return (if (isWorkout) "workout" else "day") in modes
}
