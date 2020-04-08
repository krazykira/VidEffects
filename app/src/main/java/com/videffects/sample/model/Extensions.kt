package com.videffects.sample.model

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View

/**
 * Change width x height
 */
fun View.resizeView(w: Double, h: Double) {

    val activity = this.context as Activity
    val maxWidth = activity.screenWidth()
    val maxHeight = activity.screenHeight() - (2 * 56.toPx())

    val scale = kotlin.math.min(
            maxWidth / w,
            maxHeight / h)

    layoutParams.width = (w * scale).toInt()
    layoutParams.height = (h * scale).toInt()
    requestLayout()
}

/**
 * Convert dp to px
 */
fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * Get screen width in pixels
 */
fun Activity.screenWidth(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

/**
 * Get screen height in pixels
 */
fun Activity.screenHeight(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

private const val MAX_HUE = 0.1f
private const val MAX_GRAIN = 360
private const val MAX_PROGRESS = 100F

// Transform input from progress bar to effect intensity
fun transformGrain(progress: Int) = (MAX_HUE * progress) / MAX_PROGRESS

fun transformHue(progress: Int) = (MAX_GRAIN * progress) / MAX_PROGRESS

fun transformAutofix(progress: Int) = progress / MAX_PROGRESS