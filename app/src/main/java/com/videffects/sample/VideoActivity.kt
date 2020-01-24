package com.videffects.sample

import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import com.sherazkhilji.sample.R
import com.sherazkhilji.videffects.GrainEffect
import kotlinx.android.synthetic.main.activity_video.*


class VideoActivity : Activity() {

    companion object {

        private const val UNKNOWN_SIZE_ERROR_MESSAGE = "Can't get video parameters"

        fun startActivity(assetsFileDescriptor: AssetFileDescriptor) {
            // TODO: Implement opening from assets gallery
        }
    }

    private val mediaPlayer = MediaPlayer()
    private var assetFileDescriptor: AssetFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        assetFileDescriptor = assets.openFd("video_0.mp4")

        assetFileDescriptor?.let {
            val size = getSize()

            if (size == null) {
                Toast.makeText(this, UNKNOWN_SIZE_ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                return
            }

            resizeView(size)
            mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            videoSurfaceView.init(mediaPlayer, GrainEffect(0.1F))
        }
    }

    private fun getSize(): Pair<Double, Double>? {
        assetFileDescriptor?.let {
            val retriever = MediaMetadataRetriever().apply {
                setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }

            val w = retriever.getDouble(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val h = retriever.getDouble(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

            retriever.release()
            return if (w != null && h != null) Pair(w, h) else null
        }

        return null
    }

    private fun MediaMetadataRetriever.getDouble(key: Int) = extractMetadata(key).toDoubleOrNull()

    override fun onResume() {
        super.onResume()
//        mediaPlayer.start()
        videoSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
        videoSurfaceView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun resizeView(videoSize: Pair<Double, Double>) {

        val offset = getOffset(R.dimen.filters_view_height)
        val maxWidth = screenWidth()
        val maxHeight = screenHeight()  - offset

        val scale = kotlin.math.min(
                maxWidth / videoSize.first,
                maxHeight / videoSize.second)

        videoSurfaceView.layoutParams.width = (videoSize.first * scale).toInt()
        videoSurfaceView.layoutParams.height = (videoSize.second * scale).toInt()
        videoSurfaceView.requestLayout()
    }

    // Extensions
    private fun Activity.screenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun Activity.screenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun Activity.getOffset(id: Int): Int {
        return resources.getDimensionPixelOffset(id)
    }
}