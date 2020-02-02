package com.videffects.sample.activity

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.sherazkhilji.sample.R
import com.sherazkhilji.videffects.filter.NoEffectFilter
import com.videffects.sample.VideoController
import com.videffects.sample.resizeView
import kotlinx.android.synthetic.main.activity_video.*


class VideoActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "kifio-VideoActivity"

        private const val WRITE_EXTERNAL_STORAGE = 201

        fun startActivity(assetsFileDescriptor: AssetFileDescriptor) {
            // TODO: Implement opening from assets gallery
        }
    }

    private var videoController: VideoController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        videoController = VideoController(this)
    }

    fun setupVideoSurfaceView(mediaPlayer: MediaPlayer, width: Double, height: Double) {
        videoSurfaceView.resizeView(width, height)
        videoSurfaceView.init(mediaPlayer, NoEffectFilter())
    }

    fun setupSeekBar(onSeekBarChangeListener: SeekBar.OnSeekBarChangeListener) {
        intensitySeekBar.max = 100
        intensitySeekBar.isEnabled = false
        intensitySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle item selection
        return when (item.itemId) {
            R.id.chooseShader -> {
                videoController?.chooseShader(videoSurfaceView.width, videoSurfaceView.height)
                true
            }
            R.id.save -> {
                videoController?.saveVideo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE) save()
    }


    override fun onResume() {
        super.onResume()
        videoSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
        videoSurfaceView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}