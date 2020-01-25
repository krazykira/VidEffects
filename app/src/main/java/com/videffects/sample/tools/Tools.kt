package com.videffects.sample.tools

import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.media.MediaMetadataRetriever
import android.util.DisplayMetrics
import android.view.View
import com.sherazkhilji.videffects.GrainEffect
import com.sherazkhilji.videffects.HueEffect
import com.sherazkhilji.videffects.interfaces.ShaderInterface

/**
 * Returns size of video frames in format width x height
 */
fun getSize(afd: AssetFileDescriptor): Pair<Double, Double>? {
    val retriever = MediaMetadataRetriever().apply {
        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
    }

    val w = retriever.getDouble(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
    val h = retriever.getDouble(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

    retriever.release()
    return if (w != null && h != null) Pair(w, h) else null
}

/**
 * Change width x height
 */
fun View.resizeView(newSize: Pair<Double, Double>) {

    val activity = this.context as Activity
    val maxWidth = activity.screenWidth()
    val maxHeight = activity.screenHeight()

    val scale = kotlin.math.min(
            maxWidth / newSize.first,
            maxHeight / newSize.second)

    layoutParams.width = (newSize.first * scale).toInt()
    layoutParams.height = (newSize.second * scale).toInt()
    requestLayout()
}

// Extensions
private fun MediaMetadataRetriever.getDouble(key: Int) = extractMetadata(key).toDoubleOrNull()

fun Activity.screenWidth(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

private fun Activity.screenHeight(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

private const val MAX_HUE = 0.1f
private const val MAX_GRAIN = 360
private const val MAX_PROGRESS = 100F

// Transform input from progress bar to effect intensity
fun ShaderInterface.transformIntensity(progress: Int): Float {
    return when (this) {
        is GrainEffect -> (MAX_HUE * progress) / MAX_PROGRESS
        is HueEffect -> (MAX_GRAIN * progress) / MAX_PROGRESS
        else -> progress / MAX_PROGRESS
    }
}