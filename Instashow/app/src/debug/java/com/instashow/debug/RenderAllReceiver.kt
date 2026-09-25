package com.instashow.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.instashow.R
import com.instashow.health.SampleHealth
import com.instashow.render.StoryFonts
import com.instashow.render.StoryRenderer
import com.instashow.settings.Units
import com.instashow.story.StoryData
import com.instashow.template.TemplateCatalog
import com.instashow.template.fits
import com.instashow.template.hasDataFor
import java.io.File

/**
 * Renders every template with sample data into files/renders so designs can be reviewed without
 * tapping through the app. Debug builds only.
 *
 * adb shell am broadcast -n com.instashow/.debug.RenderAllReceiver
 */
class RenderAllReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        Thread {
            try {
                renderAll(context)
            } catch (exception: Exception) {
                Log.e(TAG, "Render failed", exception)
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun renderAll(context: Context) {
        val out = File(context.filesDir, "renders").apply {
            deleteRecursively()
            mkdirs()
        }
        val renderer = StoryRenderer(StoryFonts(context))
        val templates = TemplateCatalog(context).load()
        val photo = BitmapFactory.decodeResource(context.resources, R.drawable.home_backdrop)
        val health = SampleHealth.snapshot()
        val day = StoryData(
            day = health.stats,
            workout = null,
            workouts = health.workouts,
            units = Units.Metric,
        )
        fun sample(id: String) = health.workouts.first { it.id == id }
        val run = day.copy(workout = sample("sample-run"))
        val lift = day.copy(workout = sample("sample-lift"))
        // A phone that only records steps and distance: stories must still look complete.
        val basic = StoryData(
            day = com.instashow.health.DayStats(steps = 7_342, distanceMeters = 5_120.0),
            units = Units.Metric,
        )
        val empty = StoryData(units = Units.Metric)
        val longRun = run.copy(
            units = Units.Imperial,
            workout = sample("sample-run").copy(title = "Sunday Long Run With The Crew Around Lake Merritt"),
        )
        val ride = day.copy(workout = sample("sample-ride"))
        val cases = listOf(
            "day" to day, "run" to run, "lift" to lift,
            "empty" to empty, "longrun" to longRun, "ride" to ride, "basic" to basic,
        )
        cases.forEach { (name, data) ->
            listOf("photo" to photo, "plain" to null).forEach { (variant, image) ->
                val fitting = templates.filter { it.fits(data.isWorkout, data.hasRoute, image != null) && it.hasDataFor(data) }
                val renders = fitting.map { template ->
                    template.name to renderer.render(template, image, data, width = 540, height = 960)
                }
                renders.chunked(8).forEachIndexed { page, chunk ->
                    writeSheet(File(out, "sheet__${name}__${variant}__${page + 1}.png"), chunk)
                }
                renders.forEach { it.second.recycle() }
            }
        }
        Log.i(TAG, "Rendered to ${out.absolutePath}")
    }

    /** Four across, labeled, so a whole set can be reviewed in one image. */
    private fun writeSheet(file: File, renders: List<Pair<String, Bitmap>>) {
        val columns = 4
        val cellW = 540
        val cellH = 960
        val label = 56
        val rows = (renders.size + columns - 1) / columns
        val sheet = Bitmap.createBitmap(columns * cellW, rows * (cellH + label), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.rgb(40, 40, 44))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
        }
        renders.forEachIndexed { index, (name, bitmap) ->
            val x = (index % columns) * cellW.toFloat()
            val y = (index / columns) * (cellH + label).toFloat()
            canvas.drawText(name, x + 16f, y + 42f, paint)
            canvas.drawBitmap(bitmap, x, y + label, null)
        }
        file.outputStream().use { sheet.compress(Bitmap.CompressFormat.JPEG, 82, it) }
        sheet.recycle()
    }

    private companion object {
        const val TAG = "RenderAll"
    }
}
