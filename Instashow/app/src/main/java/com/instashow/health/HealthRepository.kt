package com.instashow.health

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.ExerciseRoute
import androidx.health.connect.client.records.ExerciseRouteResult
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthRepository(context: Context) {
    private val appContext = context.applicationContext

    private val stepsRead = HealthPermission.getReadPermission(StepsRecord::class)
    private val distanceRead = HealthPermission.getReadPermission(DistanceRecord::class)
    private val caloriesRead = HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    private val exerciseRead = HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    private val heartRateRead = HealthPermission.getReadPermission(HeartRateRecord::class)
    private val restingHeartRateRead = HealthPermission.getReadPermission(RestingHeartRateRecord::class)
    private val totalCaloriesRead = HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    private val floorsRead = HealthPermission.getReadPermission(FloorsClimbedRecord::class)
    private val elevationRead = HealthPermission.getReadPermission(ElevationGainedRecord::class)
    private val sleepRead = HealthPermission.getReadPermission(SleepSessionRecord::class)

    private val corePermissions = setOf(stepsRead, distanceRead)
    private val extraPermissions = setOf(
        caloriesRead,
        exerciseRead,
        heartRateRead,
        restingHeartRateRead,
        totalCaloriesRead,
        floorsRead,
        elevationRead,
        sleepRead,
    )

    /** Everything the app can use. Asked for together so the user sees one Health Connect screen. */
    fun requiredPermissions(): Set<String> = corePermissions + extraPermissions

    suspend fun snapshot(): HealthSnapshot {
        val availability = availability()
        if (availability != HealthAvailability.Available) {
            return HealthSnapshot(availability, permissionsGranted = false, stats = DayStats())
        }
        return try {
            val client = HealthConnectClient.getOrCreate(appContext)
            val granted = client.permissionController.getGrantedPermissions()
            if (granted.intersect(corePermissions + extraPermissions).isEmpty()) {
                return HealthSnapshot(availability, permissionsGranted = false, stats = DayStats())
            }
            coroutineScope {
                val today = async { readToday(client, granted) }
                val workouts = async { readWorkouts(client, granted) }
                HealthSnapshot(
                    availability = availability,
                    permissionsGranted = granted.containsAll(corePermissions),
                    stats = today.await(),
                    workouts = workouts.await(),
                    extrasMissing = !granted.containsAll(extraPermissions),
                )
            }
        } catch (security: SecurityException) {
            Log.w(TAG, "Health Connect permission was rejected", security)
            HealthSnapshot(availability, permissionsGranted = false, stats = DayStats())
        } catch (exception: Exception) {
            Log.e(TAG, "Health Connect read failed", exception)
            HealthSnapshot(availability, permissionsGranted = false, stats = DayStats(), failed = true)
        }
    }

    private fun availability(): HealthAvailability = when (HealthConnectClient.getSdkStatus(appContext)) {
        HealthConnectClient.SDK_AVAILABLE -> HealthAvailability.Available
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthAvailability.UpdateRequired
        else -> HealthAvailability.Unavailable
    }

    private suspend fun readToday(client: HealthConnectClient, granted: Set<String>): DayStats {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant()
        val end = Instant.now().coerceAtLeast(start.plusMillis(1))
        val filter = TimeRangeFilter.between(start, end)

        val core = buildSet {
            if (stepsRead in granted) add(StepsRecord.COUNT_TOTAL)
            if (distanceRead in granted) add(DistanceRecord.DISTANCE_TOTAL)
            if (caloriesRead in granted) add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
            if (exerciseRead in granted) add(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL)
        }
        val extra = buildSet {
            if (totalCaloriesRead in granted) add(TotalCaloriesBurnedRecord.ENERGY_TOTAL)
            if (floorsRead in granted) add(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL)
            if (elevationRead in granted) add(ElevationGainedRecord.ELEVATION_GAINED_TOTAL)
            if (heartRateRead in granted) {
                add(HeartRateRecord.BPM_AVG)
                add(HeartRateRecord.BPM_MAX)
            }
            if (restingHeartRateRead in granted) add(RestingHeartRateRecord.BPM_AVG)
        }
        val response = if (core.isEmpty()) null else client.aggregate(AggregateRequest(core, filter))
        // The optional stats are read apart from the core ones so one bad data source can't blank the whole day.
        val more = aggregateOrNull(client, extra, filter)
        // Sleep that ended this morning started last night, so the window opens the evening before.
        val sleep = if (sleepRead in granted) {
            val night = today.minusDays(1).atTime(SLEEP_WINDOW_START_HOUR, 0).atZone(zone).toInstant()
            aggregateOrNull(client, setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL), TimeRangeFilter.between(night, end))
                ?.get(SleepSessionRecord.SLEEP_DURATION_TOTAL)?.seconds
        } else {
            null
        }
        return DayStats(
            steps = if (stepsRead in granted) response?.get(StepsRecord.COUNT_TOTAL) ?: 0L else null,
            distanceMeters = if (distanceRead in granted) response?.get(DistanceRecord.DISTANCE_TOTAL)?.inMeters ?: 0.0 else null,
            // Many phones never record these two; zero then means "not tracked", so it stays off the story.
            activeCalories = response?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)?.inKilocalories?.takeIf { it >= 1.0 },
            exerciseMinutes = response?.get(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL)?.toMinutes()?.takeIf { it > 0 },
            // Only what the phone actually recorded: a stat with no data stays off the story.
            totalCalories = more?.get(TotalCaloriesBurnedRecord.ENERGY_TOTAL)?.inKilocalories?.takeIf { it > 0.0 },
            floors = more?.get(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL)?.takeIf { it >= 1.0 },
            elevationMeters = more?.get(ElevationGainedRecord.ELEVATION_GAINED_TOTAL)?.inMeters?.takeIf { it >= 1.0 },
            averageHeartRate = more?.get(HeartRateRecord.BPM_AVG)?.takeIf { it > 0 },
            maxHeartRate = more?.get(HeartRateRecord.BPM_MAX)?.takeIf { it > 0 },
            restingHeartRate = more?.get(RestingHeartRateRecord.BPM_AVG)?.takeIf { it > 0 },
            sleepSeconds = sleep?.takeIf { it >= MIN_SLEEP_SECONDS },
        )
    }

    private suspend fun aggregateOrNull(
        client: HealthConnectClient,
        metrics: Set<AggregateMetric<*>>,
        filter: TimeRangeFilter,
    ): AggregationResult? {
        if (metrics.isEmpty()) return null
        return try {
            client.aggregate(AggregateRequest(metrics, filter))
        } catch (exception: Exception) {
            Log.w(TAG, "Health Connect couldn't total $metrics", exception)
            null
        }
    }

    private suspend fun readWorkouts(client: HealthConnectClient, granted: Set<String>): List<Workout> {
        if (exerciseRead !in granted) return emptyList()
        val zone = ZoneId.systemDefault()
        val since = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val sessions = client.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.after(since),
                ascendingOrder = false,
                pageSize = MAX_WORKOUTS,
            ),
        ).records
        return coroutineScope {
            sessions.map { session -> async { toWorkout(client, session, granted) } }.map { it.await() }
        }
    }

    private suspend fun toWorkout(
        client: HealthConnectClient,
        session: ExerciseSessionRecord,
        granted: Set<String>,
    ): Workout {
        val metrics = buildSet {
            if (distanceRead in granted) add(DistanceRecord.DISTANCE_TOTAL)
            if (caloriesRead in granted) add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
            if (heartRateRead in granted) add(HeartRateRecord.BPM_AVG)
            if (stepsRead in granted) add(StepsRecord.COUNT_TOTAL)
            if (heartRateRead in granted) add(HeartRateRecord.BPM_MAX)
            if (elevationRead in granted) add(ElevationGainedRecord.ELEVATION_GAINED_TOTAL)
        }
        val aggregate = if (metrics.isEmpty()) {
            null
        } else {
            runCatching {
                client.aggregate(AggregateRequest(metrics, TimeRangeFilter.between(session.startTime, session.endTime)))
            }.getOrNull()
        }
        val routeResult = session.exerciseRouteResult
        return Workout(
            id = session.metadata.id,
            type = session.exerciseType,
            title = session.title?.takeIf { it.isNotBlank() } ?: WorkoutNames.title(session.exerciseType, session.startTime),
            start = session.startTime,
            end = session.endTime,
            distanceMeters = aggregate?.get(DistanceRecord.DISTANCE_TOTAL)?.inMeters?.takeIf { it > 0.0 },
            activeCalories = aggregate?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)?.inKilocalories?.takeIf { it > 0.0 },
            averageHeartRate = aggregate?.get(HeartRateRecord.BPM_AVG)?.takeIf { it > 0 },
            maxHeartRate = aggregate?.get(HeartRateRecord.BPM_MAX)?.takeIf { it > 0 },
            elevationMeters = aggregate?.get(ElevationGainedRecord.ELEVATION_GAINED_TOTAL)?.inMeters?.takeIf { it >= 1.0 },
            steps = aggregate?.get(StepsRecord.COUNT_TOTAL)?.takeIf { it > 0 },
            route = (routeResult as? ExerciseRouteResult.Data)?.exerciseRoute?.let(::simplify).orEmpty(),
            routeConsentNeeded = routeResult is ExerciseRouteResult.ConsentRequired,
        )
    }

    companion object {
        private const val TAG = "HealthRepository"
        private const val SLEEP_WINDOW_START_HOUR = 18
        private const val MIN_SLEEP_SECONDS = 20 * 60L
        private const val MAX_WORKOUTS = 12
        private const val MAX_ROUTE_POINTS = 400

        /** Keeps the route shape while bounding the number of points the renderer has to draw. */
        fun simplify(route: ExerciseRoute): List<GeoPoint> {
            val points = route.route.map { GeoPoint(it.latitude, it.longitude) }
            if (points.size <= MAX_ROUTE_POINTS) return points
            val step = points.size / MAX_ROUTE_POINTS.toDouble()
            return (0 until MAX_ROUTE_POINTS).map { points[(it * step).toInt()] } + points.last()
        }
    }
}

fun openHealthConnectInstall(context: Context) {
    val packageId = "com.google.android.apps.healthdata"
    val intents = listOf(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageId&url=healthconnect%3A%2F%2Fonboarding"),
        ),
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageId"),
        ),
    )
    for (intent in intents) {
        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            continue
        }
    }
}
