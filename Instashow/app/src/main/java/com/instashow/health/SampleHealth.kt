package com.instashow.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.cos
import kotlin.math.sin

/**
 * Realistic numbers for trying templates without Health Connect data, such as on an emulator or a
 * phone that hasn't recorded anything yet. Only offered in debug builds.
 */
object SampleHealth {
    fun snapshot(): HealthSnapshot {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val runStart = today.atTime(7, 12).atZone(zone).toInstant()
        val rideStart = today.atTime(17, 40).atZone(zone).toInstant()
        val liftStart = today.atTime(12, 30).atZone(zone).toInstant()
        return HealthSnapshot(
            availability = HealthAvailability.Available,
            permissionsGranted = true,
            stats = DayStats(
                steps = 11_482,
                distanceMeters = 8_734.0,
                activeCalories = 612.0,
                exerciseMinutes = 140,
                totalCalories = 2_684.0,
                floors = 14.0,
                elevationMeters = 96.0,
                averageHeartRate = 84,
                maxHeartRate = 171,
                restingHeartRate = 58,
                sleepSeconds = 7 * 3600L + 32 * 60,
            ),
            workouts = listOf(
                Workout(
                    id = "sample-ride",
                    type = ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
                    title = WorkoutNames.title(ExerciseSessionRecord.EXERCISE_TYPE_BIKING, rideStart, zone),
                    start = rideStart,
                    end = rideStart.plusSeconds(64 * 60 + 40),
                    distanceMeters = 24_830.0,
                    activeCalories = 688.0,
                    averageHeartRate = 142,
                    route = loop(seed = 2),
                ),
                Workout(
                    id = "sample-lift",
                    type = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                    title = WorkoutNames.title(ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, liftStart, zone),
                    start = liftStart,
                    end = liftStart.plusSeconds(48 * 60),
                    activeCalories = 311.0,
                    averageHeartRate = 128,
                ),
                Workout(
                    id = "sample-run",
                    type = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                    title = WorkoutNames.title(ExerciseSessionRecord.EXERCISE_TYPE_RUNNING, runStart, zone),
                    start = runStart,
                    end = runStart.plusSeconds(27 * 60 + 14),
                    distanceMeters = 5_210.0,
                    activeCalories = 402.0,
                    averageHeartRate = 156,
                    maxHeartRate = 171,
                    elevationMeters = 48.0,
                    steps = 5_480,
                    route = loop(seed = 1),
                ),
            ),
            sample = true,
        )
    }

    /** A wobbly closed loop that looks like a real neighborhood route. */
    private fun loop(seed: Int): List<GeoPoint> {
        val points = 180
        val baseLat = 37.7749
        val baseLon = -122.4194
        return (0..points).map { index ->
            val t = index / points.toDouble() * 2 * Math.PI
            val radius = 0.009 + 0.0022 * sin(3 * t + seed) + 0.0014 * cos(5 * t + seed * 2) + 0.0006 * sin(11 * t)
            GeoPoint(
                latitude = baseLat + radius * sin(t) * (1.0 + 0.35 * seed),
                longitude = baseLon + radius * cos(t) * 1.3,
            )
        }
    }
}
