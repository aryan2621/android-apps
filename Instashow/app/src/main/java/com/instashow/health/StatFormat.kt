package com.instashow.health

import com.instashow.settings.Units
import com.instashow.story.StoryData
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToLong

/** A formatted stat split so the renderer can set the number large and the unit small. */
data class StatValue(
    val number: String,
    val unit: String = "",
    val label: String = "",
) {
    val available: Boolean get() = number != StatFormat.UNAVAILABLE
    val text: String get() = if (unit.isBlank()) number else "$number $unit"
}

object StatFormat {
    const val UNAVAILABLE = "—"

    /**
     * Supporting slots, in order. "hero" plus these pick the best stats for whatever the story is
     * about, so one layout works for a run, a lift or a whole day, and never shows a stat the phone
     * didn't record.
     */
    val supportingKeys = listOf("second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth")

    /** Keys a template can ask for. */
    val keys = setOf(
        "steps", "distance", "calories", "exercise", "duration", "pace", "speed", "heartrate",
        "maxhr", "restinghr", "totalcalories", "floors", "elevation", "sleep", "workouts",
        "goal", "hero",
    ) + supportingKeys

    fun value(stat: String, data: StoryData): StatValue {
        val workout = data.workout
        return when (stat) {
            "steps" -> count(workout?.steps ?: data.day.steps.takeIf { workout == null }, "STEPS")
            "distance" -> distance(workout?.distanceMeters ?: data.day.distanceMeters.takeIf { workout == null }, data.units)
            "calories" -> count(
                (workout?.activeCalories ?: data.day.activeCalories.takeIf { workout == null })?.roundToLong(),
                "CALORIES",
                unit = "kcal",
            )
            "exercise" -> count(data.day.exerciseMinutes.takeIf { workout == null }, "ACTIVE", unit = "min")
            "duration" -> duration(workout?.durationSeconds ?: data.day.exerciseMinutes?.times(60))
            "pace" -> pace(workout, data.units)
            "speed" -> speed(workout, data.units)
            "heartrate" -> count(workout?.averageHeartRate ?: data.day.averageHeartRate.takeIf { workout == null }, "AVG HR", unit = "bpm")
            "maxhr" -> count(workout?.maxHeartRate ?: data.day.maxHeartRate.takeIf { workout == null }, "MAX HR", unit = "bpm")
            "restinghr" -> count(data.day.restingHeartRate.takeIf { workout == null }, "RESTING HR", unit = "bpm")
            "totalcalories" -> count(
                data.day.totalCalories.takeIf { workout == null }?.roundToLong(),
                "TOTAL BURN",
                unit = "kcal",
            )
            "floors" -> count(data.day.floors.takeIf { workout == null }?.roundToLong(), "FLOORS")
            "elevation" -> elevation(workout?.elevationMeters ?: data.day.elevationMeters.takeIf { workout == null }, data.units)
            "sleep" -> sleep(data.day.sleepSeconds.takeIf { workout == null })
            "workouts" -> {
                val count = data.workouts.size.takeIf { workout == null && it > 0 }?.toLong()
                count(count, if (count == 1L) "WORKOUT" else "WORKOUTS")
            }
            "goal" -> goal(data)
            "hero" -> hero(data)
            in supportingKeys -> supporting(data).getOrElse(supportingKeys.indexOf(stat)) { StatValue(UNAVAILABLE) }
            else -> StatValue(UNAVAILABLE)
        }
    }

    private fun hero(data: StoryData): StatValue {
        val workout = data.workout ?: return value("steps", data)
        return if (workout.distanceMeters != null) value("distance", data) else value("duration", data)
    }

    /** The best stats after the hero, most interesting first, skipping anything the data doesn't have. */
    private fun supporting(data: StoryData): List<StatValue> {
        val workout = data.workout
        val order = if (workout == null) {
            listOf(
                "distance", "calories", "exercise", "sleep", "floors", "heartrate",
                "restinghr", "totalcalories", "elevation", "workouts", "maxhr",
            )
        } else {
            buildList {
                if (workout.distanceMeters != null) add("duration")
                if (WorkoutNames.hasPace(workout.type)) add("pace")
                if (WorkoutNames.hasSpeed(workout.type)) add("speed")
                add("heartrate")
                add("maxhr")
                add("elevation")
                add("calories")
                add("steps")
            }
        }
        val available = order.map { value(it, data) }.filter { it.available }
        return available.ifEmpty { order.take(2).map { value(it, data) } }
    }

    private fun count(value: Long?, label: String, unit: String = ""): StatValue =
        StatValue(value?.let(::formatCount) ?: UNAVAILABLE, if (value == null) "" else unit, label)

    fun formatCount(value: Long): String = "%,d".format(Locale.getDefault(), value)

    fun distance(meters: Double?, units: Units): StatValue {
        if (meters == null) return StatValue(UNAVAILABLE, label = "DISTANCE")
        return if (units == Units.Imperial) {
            val miles = meters / METERS_PER_MILE
            StatValue(decimal(miles, if (miles >= 100) 1 else 2), "mi", "DISTANCE")
        } else if (meters < 1000.0) {
            StatValue("%.0f".format(Locale.getDefault(), meters), "m", "DISTANCE")
        } else {
            val km = meters / 1000.0
            StatValue(decimal(km, if (km >= 100) 1 else 2), "km", "DISTANCE")
        }
    }

    /** Short form for the home screen, where two decimals is too much. */
    fun distanceShort(meters: Double?, units: Units): String {
        meters ?: return UNAVAILABLE
        return if (units == Units.Imperial) {
            "${decimal(meters / METERS_PER_MILE, 1)} mi"
        } else if (meters < 1000.0) {
            "%.0f m".format(Locale.getDefault(), meters)
        } else {
            "${decimal(meters / 1000.0, 1)} km"
        }
    }

    fun duration(seconds: Long?): StatValue {
        if (seconds == null || seconds <= 0) return StatValue(UNAVAILABLE, label = "TIME")
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        val number = if (hours > 0) "%d:%02d:%02d".format(hours, minutes, secs) else "%d:%02d".format(minutes, secs)
        return StatValue(number, label = "TIME")
    }

    fun durationShort(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"
    }

    private fun pace(workout: Workout?, units: Units): StatValue {
        val meters = workout?.distanceMeters
        val seconds = workout?.durationSeconds
        val label = "PACE"
        if (meters == null || seconds == null || meters < 100.0 || seconds <= 0) return StatValue(UNAVAILABLE, label = label)
        val unitMeters = if (units == Units.Imperial) METERS_PER_MILE else 1000.0
        val perUnit = (seconds / (meters / unitMeters)).roundToLong()
        if (perUnit >= 60 * 60) return StatValue(UNAVAILABLE, label = label)
        return StatValue(
            "%d:%02d".format(perUnit / 60, perUnit % 60),
            if (units == Units.Imperial) "/mi" else "/km",
            label,
        )
    }

    private fun speed(workout: Workout?, units: Units): StatValue {
        val meters = workout?.distanceMeters
        val seconds = workout?.durationSeconds
        if (meters == null || seconds == null || seconds <= 0) return StatValue(UNAVAILABLE, label = "SPEED")
        val metersPerSecond = meters / seconds
        return if (units == Units.Imperial) {
            StatValue(decimal(metersPerSecond * 3600 / METERS_PER_MILE, 1), "mph", "SPEED")
        } else {
            StatValue(decimal(metersPerSecond * 3.6, 1), "km/h", "SPEED")
        }
    }

    private fun elevation(meters: Double?, units: Units): StatValue {
        if (meters == null) return StatValue(UNAVAILABLE, label = "ELEVATION")
        return if (units == Units.Imperial) {
            StatValue(formatCount((meters * FEET_PER_METER).roundToLong()), "ft", "ELEVATION")
        } else {
            StatValue(formatCount(meters.roundToLong()), "m", "ELEVATION")
        }
    }

    private fun sleep(seconds: Long?): StatValue {
        if (seconds == null || seconds <= 0) return StatValue(UNAVAILABLE, label = "SLEEP")
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return StatValue(if (hours > 0) "${hours}h %02dm".format(minutes) else "${minutes}m", label = "SLEEP")
    }

    /** "Run 5.2 km · Strength 48 min": every workout today in a line, oldest first. */
    fun workoutList(data: StoryData): String = data.workouts.sortedBy { it.start }.joinToString(" · ") { workout ->
        val amount = workout.distanceMeters?.takeIf { it >= 100.0 }?.let { distanceShort(it, data.units) }
            ?: durationShort(workout.durationSeconds)
        "${WorkoutNames.noun(workout.type)} $amount"
    }

    private fun goal(data: StoryData): StatValue {
        val steps = data.day.steps ?: return StatValue(UNAVAILABLE, label = "OF GOAL")
        val percent = (steps * 100.0 / data.stepGoal.coerceAtLeast(1)).roundToLong()
        return StatValue(percent.toString(), "%", "OF GOAL")
    }

    private fun decimal(value: Double, places: Int): String = "%.${places}f".format(Locale.getDefault(), value)

    /** Replaces {tokens} in template text with story values. Unknown tokens are left as they are. */
    fun fill(text: String, data: StoryData): String {
        if ('{' !in text) return text
        val workout = data.workout
        val date = workout?.start?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate() ?: data.date
        val tokens = mapOf(
            "title" to (workout?.title?.let(::shortTitle) ?: "Today"),
            "kind" to (workout?.let { WorkoutNames.noun(it.type) } ?: "Day"),
            "date" to date.format(DATE_FORMAT),
            "day" to date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()),
            "longdate" to date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
            "goal" to formatCount(data.stepGoal),
            "steps" to value("steps", data).number,
            "distance" to value("distance", data).text,
            "hero" to value("hero", data).text,
            "workoutlist" to if (workout == null) workoutList(data) else "",
            "time" to (
                workout?.start?.atZone(java.time.ZoneId.systemDefault())?.toLocalTime()
                    ?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: ""
                ),
        )
        val statTokens = keys.flatMap { key ->
            val value = value(key, data)
            // Labels and units go blank with a missing value, so "STEPS" never sits next to nothing.
            val label = if (value.available) value.label else ""
            val unit = if (value.available) value.unit else ""
            // "11,482 steps" rather than a bare number when the stat has no unit of its own.
            val words = if (value.available && value.unit.isBlank() && value.label.isNotBlank() && value.label != "TIME") {
                "${value.number} ${value.label.lowercase(Locale.getDefault())}"
            } else {
                value.text
            }
            listOf(
                key to value.text,
                "${key}num" to value.number,
                "${key}unit" to unit,
                "${key}label" to label,
                "${key}words" to words,
            )
        }.toMap()
        val allTokens = statTokens + tokens
        val filled = TOKEN.replace(text) { match ->
            val raw = match.groupValues[1]
            val key = raw.lowercase(Locale.US)
            val value = allTokens[key] ?: return@replace match.value
            if (raw == raw.uppercase(Locale.US) && raw != raw.lowercase(Locale.US)) value.uppercase() else value
        }
        // Blank tokens can leave "TIME · " or " · 27:14"; tidy the separators they leave behind.
        return filled
            .replace(DOUBLE_SEPARATOR, " · ")
            .replace(EDGE_SEPARATOR, "")
    }

    /**
     * Workout names from other apps can be a whole sentence. Stories read better with a short one,
     * cut at a word boundary: "Sunday Long Run With The Crew…" becomes "Sunday Long Run With…".
     */
    fun shortTitle(title: String, max: Int = 22): String {
        val clean = title.trim()
        if (clean.length <= max) return clean
        val cut = clean.take(max).substringBeforeLast(' ').ifBlank { clean.take(max) }
        return cut.trimEnd(',', '.', '-', '–', ' ') + "…"
    }

    private val DOUBLE_SEPARATOR = Regex("(\\s*·\\s*){2,}")
    private val EDGE_SEPARATOR = Regex("^[\\s·/]+|[\\s·/]+$")
    private val TOKEN = Regex("\\{([A-Za-z]+)\\}")
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    private const val METERS_PER_MILE = 1609.344
    private const val FEET_PER_METER = 3.28084
}
