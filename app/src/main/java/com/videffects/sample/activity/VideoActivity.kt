package com.videffects.sample.activity

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sherazkhilji.sample.R
import com.sherazkhilji.videffects.GrainEffect
import com.sherazkhilji.videffects.interfaces.ShaderInterface
import com.videffects.sample.fragment.EffectsDialog
import com.videffects.sample.interfaces.OnSelectShaderListener
import com.videffects.sample.tools.getSize
import com.videffects.sample.tools.resizeView
import kotlinx.android.synthetic.main.activity_video.*


class VideoActivity : AppCompatActivity(), OnSelectShaderListener {

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
            val size = getSize(it)

            if (size == null) {
                Toast.makeText(this, UNKNOWN_SIZE_ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                return
            }

            videoSurfaceView.resizeView(size)
            mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            mediaPlayer.isLooping = true
            videoSurfaceView.init(mediaPlayer, GrainEffect(0.1F))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle item selection
        return when (item.itemId) {
            R.id.chooseShader -> {
                val dialog = EffectsDialog()
                dialog.show(this.supportFragmentManager, EffectsDialog::class.java.simpleName)
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    override fun onSelectShader(shader: ShaderInterface) {
        videoSurfaceView.setShader(shader)
    }
}