package com.videffects.sample.tools

import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.media.MediaMetadataRetriever
import android.util.DisplayMetrics
import android.view.View

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