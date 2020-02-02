package com.videffects.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.sherazkhilji.videffects.AutoFixEffect
import com.sherazkhilji.videffects.HueEffect
import com.sherazkhilji.videffects.filter.GrainFilter
import com.sherazkhilji.videffects.interfaces.Filter
import com.sherazkhilji.videffects.interfaces.ShaderInterface
import com.sherazkhilji.videffects.model.Converter
import com.sherazkhilji.videffects.model.Metadata
import com.sherazkhilji.videffects.model.MetadataExtractor
import com.sherazkhilji.videffects.service.SavingService
import com.videffects.sample.activity.VideoActivity
import com.videffects.sample.fragment.ShaderChooserDialog
import com.videffects.sample.interfaces.OnSelectShaderListener
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File

class VideoController(private var activity: VideoActivity?) {

    // leteinit
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var assetFileDescriptor: AssetFileDescriptor
    private lateinit var assetsConverter: AssetsConverter

    // nullable
    private var metadata: Metadata? = null

    // initialized
    private val shouldAskWritePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    init {
        activity?.let {
            assetFileDescriptor = it.assets.openFd("video_0.mp4")
            assetsConverter = AssetsConverter(assetFileDescriptor)
            metadata = MetadataExtractor().extract(assetFileDescriptor)
            setupMediaPlayer()
            setupView()
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.isLooping = true
        mediaPlayer.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
        )
    }

    private fun setupView(activity: VideoActivity) {
        val metadata = this.metadata
        if (metadata != null) {
            activity.setupVideoSurfaceView(mediaPlayer, metadata.width, metadata.height)
            activity.setupSeekBar(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    videoSurfaceView.filter.apply {
                        when (this) {
                            is GrainFilter -> this.setStrength(transformGrain(progress))
                            is HueEffect -> this.setDegrees(transformHue(progress))
                            is AutoFixEffect -> this.setScale(transformAutofix(progress))
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    Log.d(VideoActivity.TAG, "onStartTrackingTouch")
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.d(VideoActivity.TAG, "onStopTrackingTouch")
                }
            })
        } else {
            activity.showToast("Cannot get metadata from video.")
        }
    }


    fun onFinishSaving() {
        activity?.progress?.visibility = View.GONE
//        if (resultCode == SavingService.SUCCESSFUL_SAVING) {
//            Toast.makeText(this@VideoActivity, "Video saved!", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this@VideoActivity, "Video wasn't saved! Check logs.", Toast.LENGTH_SHORT).show()
//        }
    }

    fun dispose() {
        activity = null
    }

    fun chooseShader(videoViewWidth: Int, videoViewHeight: Int) {
        val dialog = ShaderChooserDialog.newInstance(videoViewWidth, videoViewHeight)
        dialog.setListener(object : OnSelectShaderListener {
            override fun onSelectShader(shader: Any) {
                when (shader) {
                    is ShaderInterface -> {
                        videoSurfaceView.shader = shader
                        intensitySeekBar.isEnabled = false
                    }
                    is Filter -> {
                        videoSurfaceView.setFilter(shader)
                        intensitySeekBar.isEnabled = true
                    }
                    else -> {
                        return
                    }
                }

                intensitySeekBar.progress = 0
            }
        })
        dialog.show(activity?.supportFragmentManager, ShaderChooserDialog::class.java.simpleName)
    }

    fun save() {
        val outPath = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "out.mp4").toString()
        val intent = Intent(this, SavingService::class.java)
        intent.putExtra(SavingService.PATH, "video_2.mp4")
        intent.putExtra(SavingService.FILTER, videoSurfaceView.filter)
        intent.putExtra(SavingService.OUT_PATH, outPath)
        intent.putExtra(SavingService.RECEIVER, receiver)
        progress.visibility = View.VISIBLE
        startService(intent)
    }

    fun saveVideo() {
        if (shouldAskWritePermission) {
            val result = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (result != PackageManager.PERMISSION_GRANTED
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), VideoActivity.WRITE_EXTERNAL_STORAGE)
            } else {
                save()
            }
        } else {
            save()
        }
    }
}