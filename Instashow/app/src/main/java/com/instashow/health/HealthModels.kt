package com.instashow.health

import java.time.Instant

data class DayStats(
    val steps: Long? = null,
    val distanceMeters: Double? = null,
    val activeCalories: Double? = null,
    val exerciseMinutes: Long? = null,
    /** Everything burned today, resting energy included. */
    val totalCalories: Double? = null,
    val floors: Double? = null,
    val elevationMeters: Double? = null,
    val averageHeartRate: Long? = null,
    val maxHeartRate: Long? = null,
    val restingHeartRate: Long? = null,
    /** Last night's sleep, plus any nap so far today. */
    val sleepSeconds: Long? = null,
)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class Workout(
    val id: String,
    val type: Int,
    val title: String,
    val start: Instant,
    val end: Instant,
    val distanceMeters: Double? = null,
    val activeCalories: Double? = null,
    val averageHeartRate: Long? = null,
    val maxHeartRate: Long? = null,
    val elevationMeters: Double? = null,
    val steps: Long? = null,
    val route: List<GeoPoint> = emptyList(),
    val routeConsentNeeded: Boolean = false,
) {
    val durationSeconds: Long get() = (end.epochSecond - start.epochSecond).coerceAtLeast(0)
}

enum class HealthAvailability {
    Available,
    Unavailable,
    UpdateRequired,
}

data class HealthSnapshot(
    val availability: HealthAvailability,
    val permissionsGranted: Boolean,
    val stats: DayStats,
    /** Today's workouts, newest first. */
    val workouts: List<Workout> = emptyList(),
    /** True when steps and distance are granted but some of the other stats are not. */
    val extrasMissing: Boolean = false,
    val loading: Boolean = false,
    val failed: Boolean = false,
    val sample: Boolean = false,
)
