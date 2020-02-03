package com.videffects.sample.view

import android.Manifest
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.sherazkhilji.sample.R
import com.sherazkhilji.videffects.filter.NoEffectFilter
import com.sherazkhilji.videffects.interfaces.Filter
import com.sherazkhilji.videffects.interfaces.ShaderInterface
import com.videffects.sample.controller.VideoController
import com.videffects.sample.model.resizeView
import kotlinx.android.synthetic.main.activity_video.*


class VideoActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "kifio-VideoActivity"

        const val WRITE_EXTERNAL_STORAGE = 201

        fun startActivity(assetsFileDescriptor: AssetFileDescriptor) {
            // TODO: Implement opening from assets gallery
        }
    }

    private var videoController: VideoController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        videoController = VideoController(this, "video_2.mp4")
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
                videoController?.chooseShader()
                true
            }
            R.id.save -> {
                videoController?.saveVideo(videoSurfaceView.filter)
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
        if (requestCode == WRITE_EXTERNAL_STORAGE) videoController?.saveVideo(videoSurfaceView.filter)
    }


    override fun onResume() {
        super.onResume()
        videoSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        videoController?.onPause()
        videoSurfaceView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoController?.onDestroy()
        videoController = null
    }

    fun getFilter(): Filter? = videoSurfaceView.filter

    fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE)
    }

    fun onSelectShader(shader: ShaderInterface) {
        videoSurfaceView.shader = shader
        intensitySeekBar.isEnabled = false
        intensitySeekBar.progress = 100
    }

    fun onSelectFilter(filter: Filter) {
        videoSurfaceView.filter = filter
        intensitySeekBar.isEnabled = true
        intensitySeekBar.progress = 0
    }

    fun onStartSavingVideo() {
        progress.visibility = View.VISIBLE
    }

    fun onFinishSavingVideo(msg: String) {
        runOnUiThread {
            progress.visibility = View.GONE
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}