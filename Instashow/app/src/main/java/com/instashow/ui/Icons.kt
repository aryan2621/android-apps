package com.instashow.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.instashow.health.WorkoutKind

private fun iconFromPath(name: String, path: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(pathData = PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.Black))
    }.build()

object AppIcons {
    val Camera = iconFromPath(
        "Camera",
        "M9,2 L7.17,4 H4 c-1.1,0 -2,0.9 -2,2 v12 c0,1.1 0.9,2 2,2 h16 c1.1,0 2,-0.9 2,-2 V6 c0,-1.1 -0.9,-2 -2,-2 h-3.17 L15,2 H9 z M12,17 c-2.76,0 -5,-2.24 -5,-5 s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5 z",
    )
    val Gallery = iconFromPath(
        "Gallery",
        "M22,16 V4 c0,-1.1 -0.9,-2 -2,-2 H8 c-1.1,0 -2,0.9 -2,2 v12 c0,1.1 0.9,2 2,2 h12 c1.1,0 2,-0.9 2,-2 z M13,12 l2.03,2.71 L16,11 l4,5 H8 l3,-4 z M2,6 v14 c0,1.1 0.9,2 2,2 h14 v-2 H4 V6 H2 z",
    )
    val Sparkle = iconFromPath(
        "Sparkle",
        "M19,9l1.25,-2.75L23,5l-2.75,-1.25L19,1l-1.25,2.75L15,5l2.75,1.25L19,9zM11.5,9.5L9,4L6.5,9.5L1,12l5.5,2.5L9,20l2.5,-5.5L17,12L11.5,9.5zM19,15l-1.25,2.75L15,19l2.75,1.25L19,23l1.25,-2.75L23,19l-2.75,-1.25L19,15z",
    )
    val SmallerText = iconFromPath(
        "SmallerText",
        "M0.99,19 h2.42 l1.27,-3.58 h5.65 L11.59,19 h2.42 L8.75,5 h-2.5 L0.99,19 z M5.41,13.39 L7.44,7.6 h0.12 l2.03,5.79 H5.41 z M23,11 v2 h-8 v-2 h8 z",
    )
    val LargerText = iconFromPath(
        "LargerText",
        "M1.99,19 h2.42 l1.27,-3.58 h5.65 L12.59,19 h2.42 L9.75,5 h-2.5 L1.99,19 z M6.41,13.39 L8.44,7.6 h0.12 l2.03,5.79 H6.41 z M20,8 v3 h3 v2 h-3 v3 h-2 v-3 h-3 v-2 h3 V8 h2 z",
    )
    val Save = iconFromPath("Save", "M19,9 h-4 V3 H9 v6 H5 l7,7 L19,9 z M5,18 v2 h14 v-2 H5 z")
    val Edit = iconFromPath(
        "Edit",
        "M3,17.25 V21 h3.75 L17.81,9.94 l-3.75,-3.75 L3,17.25 z M20.71,7.04 c0.39,-0.39 0.39,-1.02 0,-1.41 l-2.34,-2.34 c-0.39,-0.39 -1.02,-0.39 -1.41,0 l-1.83,1.83 3.75,3.75 1.83,-1.83 z",
    )
    val Share = iconFromPath(
        "Share",
        "M18,16.08 c-0.76,0 -1.44,0.3 -1.96,0.77 L8.91,12.7 c0.05,-0.23 0.09,-0.46 0.09,-0.7 s-0.04,-0.47 -0.09,-0.7 l7.05,-4.11 c0.54,0.5 1.25,0.81 2.04,0.81 c1.66,0 3,-1.34 3,-3 s-1.34,-3 -3,-3 -3,1.34 -3,3 c0,0.24 0.04,0.47 0.09,0.7 L8.04,9.81 C7.5,9.31 6.79,9 6,9 c-1.66,0 -3,1.34 -3,3 s1.34,3 3,3 c0.79,0 1.5,-0.31 2.04,-0.81 l7.12,4.16 c-0.05,0.21 -0.08,0.43 -0.08,0.65 c0,1.61 1.31,2.92 2.92,2.92 s2.92,-1.31 2.92,-2.92 -1.31,-2.92 -2.92,-2.92 z",
    )
    val Settings = iconFromPath(
        "Settings",
        "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z",
    )
    val Flame = iconFromPath(
        "Flame",
        "M13.5,0.67s0.74,2.65 0.74,4.8c0,2.06 -1.35,3.73 -3.41,3.73 -2.07,0 -3.63,-1.67 -3.63,-3.73l0.03,-0.36C5.21,7.51 4,10.62 4,14c0,4.42 3.58,8 8,8s8,-3.58 8,-8C20,8.61 17.41,3.8 13.5,0.67zM11.71,19c-1.78,0 -3.22,-1.4 -3.22,-3.14 0,-1.62 1.05,-2.76 2.81,-3.12 1.77,-0.36 3.6,-1.21 4.62,-2.58 0.39,1.29 0.59,2.65 0.59,4.04 0,2.65 -2.15,4.8 -4.8,4.8z",
    )
    val Close = iconFromPath(
        "Close",
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z",
    )
    val Check = iconFromPath("Check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z")
    val ChevronRight = iconFromPath("ChevronRight", "M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z")
    val Lock = iconFromPath(
        "Lock",
        "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM12,17c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2zM15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 1.71,0 3.1,1.39 3.1,3.1v2z",
    )
    val Map = iconFromPath(
        "Map",
        "M20.5,3l-0.16,0.03L15,5.1 9,3 3.36,4.9c-0.21,0.07 -0.36,0.25 -0.36,0.48V20.5c0,0.28 0.22,0.5 0.5,0.5l0.16,-0.03L9,18.9l6,2.1 5.64,-1.9c0.21,-0.07 0.36,-0.25 0.36,-0.48V3.5c0,-0.28 -0.22,-0.5 -0.5,-0.5zM15,19l-6,-2.11V5l6,2.11V19z",
    )
    val Info = iconFromPath(
        "Info",
        "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2V7h2v2z",
    )

    private val Run = iconFromPath(
        "Run",
        "M13.49,5.48c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM9.89,19.38l1,-4.4 2.1,2v6h2v-7.5l-2.1,-2 0.6,-3c1.3,1.5 3.3,2.5 5.5,2.5v-2c-1.9,0 -3.5,-1 -4.3,-2.4l-1,-1.6c-0.4,-0.6 -1,-1 -1.7,-1 -0.3,0 -0.5,0.1 -0.8,0.1l-5.2,2.2v4.7h2v-3.4l1.8,-0.7 -1.6,8.1 -4.9,-1 -0.4,2 7,1.4z",
    )
    private val Walk = iconFromPath(
        "Walk",
        "M13.5,5.5c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM9.8,8.9L7,23h2.1l1.8,-8 2.1,2v6h2v-7.5l-2.1,-2 0.6,-3C14.8,12 16.8,13 19,13v-2c-1.9,0 -3.5,-1 -4.3,-2.4l-1,-1.6c-0.4,-0.6 -1,-1 -1.7,-1 -0.3,0 -0.5,0.1 -0.8,0.1L6,8.3V13h2V9.6l1.8,-0.7",
    )
    private val Ride = iconFromPath(
        "Ride",
        "M15.5,5.5c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM5,12c-2.8,0 -5,2.2 -5,5s2.2,5 5,5 5,-2.2 5,-5 -2.2,-5 -5,-5zM5,20.5c-1.9,0 -3.5,-1.6 -3.5,-3.5s1.6,-3.5 3.5,-3.5 3.5,1.6 3.5,3.5 -1.6,3.5 -3.5,3.5zM10.8,10.5l2.4,-2.4 0.8,0.8c1.3,1.3 3,2.1 5.1,2.1V9c-1.5,0 -2.7,-0.6 -3.6,-1.5l-1.9,-1.9c-0.5,-0.4 -1,-0.6 -1.6,-0.6s-1.1,0.2 -1.4,0.6L7.8,8.4c-0.4,0.4 -0.6,0.9 -0.6,1.4 0,0.6 0.2,1.1 0.6,1.4L11,14v5h2v-6.2l-2.2,-2.3zM19,12c-2.8,0 -5,2.2 -5,5s2.2,5 5,5 5,-2.2 5,-5 -2.2,-5 -5,-5zM19,20.5c-1.9,0 -3.5,-1.6 -3.5,-3.5s1.6,-3.5 3.5,-3.5 3.5,1.6 3.5,3.5 -1.6,3.5 -3.5,3.5z",
    )
    private val Hike = iconFromPath("Hike", "M14,6l-3.75,5 2.85,3.8 -1.6,1.2C9.81,13.75 7,10 7,10l-6,8h22L14,6z")
    private val Strength = iconFromPath(
        "Strength",
        "M20.57,14.86L22,13.43 20.57,12 17,15.57 8.43,7 12,3.43 10.57,2 9.14,3.43 7.71,2 5.57,4.14 4.14,2.71 2.71,4.14l1.43,1.43L2,7.71l1.43,1.43L2,10.57 3.43,12 7,8.43 15.57,17 12,20.57 13.43,22l1.43,-1.43L16.29,22l2.14,-2.14 1.43,1.43 1.43,-1.43 -1.43,-1.43L22,16.29z",
    )
    private val Bolt = iconFromPath("Bolt", "M7,2v11h3v9l7,-12h-4l4,-8z")

    fun workout(kind: WorkoutKind): ImageVector = when (kind) {
        WorkoutKind.Run -> Run
        WorkoutKind.Walk -> Walk
        WorkoutKind.Ride -> Ride
        WorkoutKind.Hike -> Hike
        WorkoutKind.Strength -> Strength
        WorkoutKind.Swim, WorkoutKind.Yoga, WorkoutKind.Other -> Bolt
    }

    val Instagram: ImageVector = ImageVector.Builder(
        name = "Instagram",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        fun stroke(data: String) = addPath(
            pathData = PathParser().parsePathString(data).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        stroke("M7,2.5h10a4.5,4.5 0 0,1 4.5,4.5v10a4.5,4.5 0 0,1 -4.5,4.5H7a4.5,4.5 0 0,1 -4.5,-4.5V7a4.5,4.5 0 0,1 4.5,-4.5z")
        stroke("M12,8a4,4 0 1,0 0,8a4,4 0 1,0 0,-8z")
        addPath(
            pathData = PathParser().parsePathString("M17.5,5.3a1.2,1.2 0 1,0 0,2.4a1.2,1.2 0 1,0 0,-2.4z").toNodes(),
            fill = SolidColor(Color.Black),
        )
    }.build()

    val Google: ImageVector = ImageVector.Builder(
        name = "Google",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 48f,
        viewportHeight = 48f,
    ).apply {
        fun path(data: String, color: Long) {
            addPath(pathData = PathParser().parsePathString(data).toNodes(), fill = SolidColor(Color(color)))
        }
        path("M24,9.5c3.54,0 6.71,1.22 9.21,3.6l6.85,-6.85C35.9,2.38 30.47,0 24,0 14.62,0 6.51,5.38 2.56,13.22l7.98,6.19C12.43,13.72 17.74,9.5 24,9.5z", 0xFFEA4335)
        path("M46.98,24.55c0,-1.57 -0.15,-3.09 -0.38,-4.55H24v9.02h12.94c-0.58,2.96 -2.26,5.48 -4.78,7.18l7.73,6c4.51,-4.18 7.09,-10.36 7.09,-17.65z", 0xFF4285F4)
        path("M10.53,28.59c-0.48,-1.45 -0.76,-2.99 -0.76,-4.59s0.27,-3.14 0.76,-4.59l-7.98,-6.19C0.92,16.46 0,20.12 0,24c0,3.88 0.92,7.54 2.56,10.78l7.97,-6.19z", 0xFFFBBC05)
        path("M24,48c6.48,0 11.93,-2.13 15.89,-5.81l-7.73,-6c-2.15,1.45 -4.92,2.3 -8.16,2.3 -6.26,0 -11.57,-4.22 -13.47,-9.91l-7.98,6.19C6.51,42.62 14.62,48 24,48z", 0xFF34A853)
    }.build()
}
