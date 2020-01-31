package com.videffects.sample.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sherazkhilji.sample.R
import com.sherazkhilji.videffects.AutoFixEffect
import com.sherazkhilji.videffects.GrainEffect
import com.sherazkhilji.videffects.HueEffect
import com.sherazkhilji.videffects.interfaces.ShaderInterface
import com.sherazkhilji.videffects.service.SavingService
import com.videffects.sample.fragment.ShaderChooserDialog
import com.videffects.sample.interfaces.OnSelectShaderListener
import com.videffects.sample.tools.getSize
import com.videffects.sample.tools.resizeView
import com.videffects.sample.tools.transformIntensity
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File
import kotlin.math.roundToInt


class VideoActivity : AppCompatActivity(), OnSelectShaderListener, SeekBar.OnSeekBarChangeListener {

    companion object {

        private const val TAG = "kifio-VideoActivity"

        private const val WRITE_EXTERNAL_STORAGE = 201

        private const val UNKNOWN_SIZE_ERROR_MESSAGE = "Can't get video parameters"

        fun startActivity(assetsFileDescriptor: AssetFileDescriptor) {
            // TODO: Implement opening from assets gallery
        }
    }

    private var size: Pair<Double, Double>? = null
    private val mediaPlayer = MediaPlayer()
    private var assetFileDescriptor: AssetFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        assetFileDescriptor = assets.openFd("video_0.mp4")
        assetFileDescriptor?.let {
            val size = getSize(it)

            if (size == null) {
                Toast.makeText(this, UNKNOWN_SIZE_ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                return
            }

            videoSurfaceView.resizeView(size)
            this.size = size
            mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            mediaPlayer.isLooping = true
            videoSurfaceView.init(mediaPlayer, GrainEffect(0.1F))
        }

        intensitySeekBar.max = 100
        intensitySeekBar.isEnabled = false
        intensitySeekBar.setOnSeekBarChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle item selection
        return when (item.itemId) {
            R.id.chooseShader -> {
                val dialog = ShaderChooserDialog()
                dialog.show(this.supportFragmentManager, ShaderChooserDialog::class.java.simpleName)
                true
            }
            R.id.save -> {
                if (shouldAskWritePermission) {
                    val result = ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (result != PackageManager.PERMISSION_GRANTED
                            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE)
                    } else {
                        save()
                    }
                } else {
                    save()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val shouldAskWritePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE) save()
    }

    private fun save() {
        size?.let {
            val outPath = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "out.mp4").toString()
            val intent = Intent(this, SavingService::class.java)
            intent.putExtra(SavingService.WIDTH, it.first.roundToInt())
            intent.putExtra(SavingService.HEIGHT, it.second.roundToInt())
            intent.putExtra(SavingService.PATH, "video_0.mp4")
            intent.putExtra(SavingService.IS_ASSET, true)
            intent.putExtra(SavingService.OUT_PATH, outPath)
            progress.visibility = View.VISIBLE
            startService(intent)
        }
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

    override fun onSelectShader(shader: ShaderInterface, allowAdjustment: Boolean) {
        videoSurfaceView.shader = shader
        intensitySeekBar.progress = 0
        intensitySeekBar.isEnabled = allowAdjustment
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        videoSurfaceView.shader.apply {
            when (this) {
                is GrainEffect -> this.setStrength(transformIntensity(progress))
                is HueEffect -> this.setDegrees(transformIntensity(progress))
                is AutoFixEffect -> this.setScale(transformIntensity(progress))
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        Log.d(TAG, "onStartTrackingTouch")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        Log.d(TAG, "onStopTrackingTouch")
    }
}