package com.videffects.sample.controller

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.sherazkhilji.videffects.AutoFixEffect
import com.sherazkhilji.videffects.HueEffect
import com.sherazkhilji.videffects.filter.AutoFixFilter
import com.sherazkhilji.videffects.filter.GrainFilter
import com.sherazkhilji.videffects.filter.HueFilter
import com.sherazkhilji.videffects.filter.NoEffectFilter
import com.sherazkhilji.videffects.interfaces.ConvertResultListener
import com.sherazkhilji.videffects.interfaces.Filter
import com.sherazkhilji.videffects.interfaces.ShaderInterface
import com.sherazkhilji.videffects.model.Metadata
import com.videffects.sample.view.VideoActivity
import com.videffects.sample.view.ShaderChooserDialog
import com.videffects.sample.interfaces.OnSelectShaderListener
import com.videffects.sample.interfaces.ProgressChangeListener
import com.videffects.sample.model.*
import java.io.File

class VideoController(private var activity: VideoActivity?,
                      filename: String) {

    private var filter: Filter = NoEffectFilter()

    private var assetFileDescriptor: AssetFileDescriptor = activity?.assets?.openFd(filename)
            ?: throw RuntimeException("Asset not found")

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var metadata: Metadata? = AssetsMetadataExtractor().extract(assetFileDescriptor)
    private val shouldAskWritePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    private val progressChangeListener: ProgressChangeListener = object : ProgressChangeListener() {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filter.run {
                when (this) {
                    is GrainFilter -> setStrength(transformGrain(progress))
                    is HueFilter -> setHue(transformHue(progress))
                    is AutoFixFilter -> setStrength(transformAutofix(progress))
                    else -> activity?.showToast("Changing intensity not implemented for selected effect in this demo")
                }
            }
        }
    }

    init {
        metadata
        setupMediaPlayer()
        setupView()
    }

    private fun setupMediaPlayer() {
        mediaPlayer
        mediaPlayer.isLooping = true
        mediaPlayer.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
        )
    }

    private fun setupView() {
        val metadata = this.metadata
        val activity = this.activity
        if (metadata != null && activity != null) {
            activity.setupVideoSurfaceView(mediaPlayer, metadata.width, metadata.height)
            activity.setupSeekBar(progressChangeListener)
        }
    }

    fun chooseShader() {
        val videoWidth = metadata?.width?.toInt() ?: return
        val videoHeight = metadata?.height?.toInt() ?: return

        val dialog = ShaderChooserDialog.newInstance(videoWidth, videoHeight)
        dialog.setListener(object : OnSelectShaderListener {
            override fun onSelectShader(shader: Any) {
                when (shader) {
                    is ShaderInterface -> {
                        filter = NoEffectFilter()
                        activity?.onSelectShader(shader)
                    }
                    is Filter -> {
                        filter = shader
                        activity?.onSelectFilter(shader)
                    }
                    else -> return
                }
            }
        })
        dialog.show(activity?.supportFragmentManager, ShaderChooserDialog::class.java.simpleName)
    }

    fun saveVideo() {
        activity?.let {
            if (shouldAskWritePermission && isStoragePermissionNotGranted(it)) {
                it.requestStoragePermissions()
            } else {
                save()
            }
        }
    }

    private fun save() {

        if (filter is NoEffectFilter) {
            activity?.showToast("Saving will work only with Filters.")
        }

        val parent = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: throw RuntimeException("Activity is destroyed!")
        val child = "out.mp4"
        val outPath = File(parent, child).toString()
        val assetConverterThread = AssetConverterThread(
                AssetsConverter(assetFileDescriptor),
                filter,
                outPath,
                object : ConvertResultListener {

                    override fun onSuccess() {
                        activity?.onFinishSavingVideo("Video successfully saved at $outPath")
                    }

                    override fun onFail() {
                        activity?.onFinishSavingVideo("Video wasn't saved. Check log for details.")
                    }
                }
        )

        activity?.onStartSavingVideo()
        assetConverterThread.start()
    }

    private fun isStoragePermissionNotGranted(ctx: Context): Boolean {
        val result = ContextCompat.checkSelfPermission(ctx, WRITE_EXTERNAL_STORAGE)
        return result != PackageManager.PERMISSION_GRANTED
    }

    fun onPause() {
        mediaPlayer.pause()
    }

    fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        assetFileDescriptor.close()
        activity = null
    }

    private class AssetConverterThread(private var assetsConverter: AssetsConverter,
                                       private var filter: Filter,
                                       private var outPath: String,
                                       private var listener: ConvertResultListener) : Thread() {

        override fun run() {
            super.run()
            assetsConverter.startConverter(filter, outPath, listener)
        }
    }
}