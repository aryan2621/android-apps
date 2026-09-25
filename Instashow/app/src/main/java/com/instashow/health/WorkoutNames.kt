package com.instashow.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import java.time.Instant
import java.time.ZoneId

enum class WorkoutKind {
    Run,
    Walk,
    Ride,
    Hike,
    Swim,
    Strength,
    Yoga,
    Other,
}

object WorkoutNames {
    fun kind(type: Int): WorkoutKind = when (type) {
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL,
        -> WorkoutKind.Run
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> WorkoutKind.Walk
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY,
        -> WorkoutKind.Ride
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> WorkoutKind.Hike
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER,
        -> WorkoutKind.Swim
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING,
        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING,
        ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS,
        -> WorkoutKind.Strength
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA,
        ExerciseSessionRecord.EXERCISE_TYPE_PILATES,
        -> WorkoutKind.Yoga
        else -> WorkoutKind.Other
    }

    fun noun(type: Int): String = when (kind(type)) {
        WorkoutKind.Run -> "Run"
        WorkoutKind.Walk -> "Walk"
        WorkoutKind.Ride -> "Ride"
        WorkoutKind.Hike -> "Hike"
        WorkoutKind.Swim -> "Swim"
        WorkoutKind.Strength -> "Workout"
        WorkoutKind.Yoga -> "Yoga"
        WorkoutKind.Other -> "Workout"
    }

    /** "Morning Run", "Evening Ride" and so on, in the phone's time zone. */
    fun title(type: Int, start: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val hour = start.atZone(zone).hour
        val part = when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }
        return "$part ${noun(type)}"
    }

    /** Distance-based sports read best with a pace; the rest lead with time. */
    fun hasPace(type: Int): Boolean = kind(type) in setOf(WorkoutKind.Run, WorkoutKind.Walk, WorkoutKind.Hike)

    fun hasSpeed(type: Int): Boolean = kind(type) == WorkoutKind.Ride
}
