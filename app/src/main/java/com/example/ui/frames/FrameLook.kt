package com.example.ui.frames

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix

/**
 * Snapchat-style looks for Frames. The same matrix drives the live
 * camera preview, the saved photo, and playback, so a post looks the same
 * everywhere.
 */
enum class FrameLook(
    val key: String,
    val label: String,
    private val values: FloatArray,
    val tint: Color
) {
    CLEAN(
        "clean", "Clean",
        floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        Color.Transparent
    ),
    GOLDEN(
        "golden", "Golden",
        floatArrayOf(
            1.12f, 0.02f, 0f, 0f, 10f,
            0.02f, 1.02f, 0f, 0f, 2f,
            0f, 0f, 0.84f, 0f, -6f,
            0f, 0f, 0f, 1f, 0f
        ),
        Color(0x22FFB300)
    ),
    COOL(
        "cool", "Cool",
        floatArrayOf(
            0.88f, 0f, 0.03f, 0f, -4f,
            0f, 1.01f, 0.02f, 0f, 0f,
            0.02f, 0.03f, 1.14f, 0f, 8f,
            0f, 0f, 0f, 1f, 0f
        ),
        Color(0x1F2196F3)
    ),
    MONO(
        "mono", "Mono",
        floatArrayOf(
            0.36f, 0.5f, 0.14f, 0f, 0f,
            0.36f, 0.5f, 0.14f, 0f, 0f,
            0.36f, 0.5f, 0.14f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        Color.Transparent
    ),
    FADE(
        "fade", "Fade",
        floatArrayOf(
            0.9f, 0f, 0f, 0f, 26f,
            0f, 0.9f, 0f, 0f, 26f,
            0f, 0f, 0.9f, 0f, 26f,
            0f, 0f, 0f, 1f, 0f
        ),
        Color(0x14FFFFFF)
    ),
    VIVID(
        "vivid", "Vivid",
        floatArrayOf(
            1.32f, -0.12f, -0.05f, 0f, -8f,
            -0.06f, 1.3f, -0.06f, 0f, -8f,
            -0.04f, -0.12f, 1.32f, 0f, -8f,
            0f, 0f, 0f, 1f, 0f
        ),
        Color(0x14E91E63)
    );

    fun colorMatrix(): ColorMatrix = ColorMatrix(values.copyOf())

    fun colorFilter(): ColorMatrixColorFilter = ColorMatrixColorFilter(values.copyOf())

    /** Compose equivalent, used to preview and replay a saved look. */
    fun composeFilter(): ColorFilter = ColorFilter.colorMatrix(ComposeColorMatrix(values.copyOf()))

    companion object {
        fun fromKey(key: String?): FrameLook =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: CLEAN
    }
}

/** Bakes the selected look into a captured photo before upload. */
fun Bitmap.applyLook(look: FrameLook): Bitmap {
    if (look == FrameLook.CLEAN) return this
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = look.colorFilter()
    }
    canvas.drawBitmap(this, 0f, 0f, paint)
    if (out !== this) recycle()
    return out
}
