package com.videffects.sample.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sherazkhilji.sample.R
import com.sherazkhilji.videffects.filter.NoEffectFilter
import com.sherazkhilji.videffects.interfaces.Filter
import com.sherazkhilji.videffects.interfaces.ShaderInterface
import com.videffects.sample.controller.VideoController
import com.videffects.sample.model.resizeView
import kotlinx.android.synthetic.main.activity_video.*


class VideoActivity : AppCompatActivity() {

    companion object {

        const val WRITE_EXTERNAL_STORAGE = 201
        const val ASSET_NAME = "name"

        fun startActivity(ctx: Context, assetName: String) {
            ctx.startActivity(Intent(ctx, VideoActivity::class.java)
                    .putExtra(ASSET_NAME, assetName))
        }
    }

    private var videoController: VideoController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val filename = intent.getStringExtra(ASSET_NAME)
                ?: throw RuntimeException("Asset name is null")
        videoController = VideoController(this, filename)
        progress.setOnClickListener {  }
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.save).isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle item selection
        return when (item.itemId) {
            R.id.chooseShader -> {
                videoController?.chooseShader()
                true
            }
            R.id.save -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isStoragePermissionNotGranted()) {
                        requestStoragePermissions()
                    } else {
                        videoController?.saveVideo()
                    }
                }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == WRITE_EXTERNAL_STORAGE) videoController?.saveVideo()
        }
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

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE)
    }

    private fun isStoragePermissionNotGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result != PackageManager.PERMISSION_GRANTED
    }

    fun onSelectShader(shader: ShaderInterface) {
        videoSurfaceView.setShader(shader)
        intensitySeekBar.isEnabled = false
        intensitySeekBar.progress = 100
    }

    fun onSelectFilter(filter: Filter) {
        videoSurfaceView.setFilter(filter)
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

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}