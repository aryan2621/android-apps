package com.instashow.story

import com.instashow.health.DayStats
import com.instashow.health.GeoPoint
import com.instashow.health.Workout
import com.instashow.settings.DEFAULT_STEP_GOAL
import com.instashow.settings.Units
import java.time.LocalDate

/** Everything a story can show. A story is about either the whole of today or one of today's workouts. */
data class StoryData(
    val day: DayStats = DayStats(),
    val stepGoal: Long = DEFAULT_STEP_GOAL,
    val workout: Workout? = null,
    /** Every workout recorded today, newest first. A day story sums them up. */
    val workouts: List<Workout> = emptyList(),
    val units: Units = Units.Metric,
    val date: LocalDate = LocalDate.now(),
) {
    val isWorkout: Boolean get() = workout != null

    /** The workout's own route, or for a day story the longest route recorded today. */
    val route: List<GeoPoint>
        get() = workout?.route
            ?: workouts.filter { it.route.size >= 2 }.maxByOrNull { it.distanceMeters ?: 0.0 }?.route.orEmpty()

    val hasRoute: Boolean get() = route.size >= 2
}
