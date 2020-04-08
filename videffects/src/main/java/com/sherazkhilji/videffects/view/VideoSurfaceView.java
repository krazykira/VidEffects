package com.sherazkhilji.videffects.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.sherazkhilji.videffects.NoEffect;
import com.sherazkhilji.videffects.filter.NoEffectFilter;
import com.sherazkhilji.videffects.interfaces.Filter;
import com.sherazkhilji.videffects.interfaces.ShaderInterface;
import com.sherazkhilji.videffects.model.BaseRenderer;
import com.sherazkhilji.videffects.model.Utils;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.sherazkhilji.videffects.Constants.DEFAULT_VERTEX_SHADER;

/**
 * This GLSurfaceView can be used to display video that is being played by media
 * player and at the same time different effect can be applied on the video.
 * This view uses shader for applying different effects.
 *
 * @author sheraz.khilji
 */
@SuppressLint("ViewConstructor")
public class VideoSurfaceView extends GLSurfaceView {
    private static final String TAG = "VideoSurfaceView";
    private MediaPlayer mMediaPlayer = null;
    private Filter filter = new NoEffectFilter();
    private ShaderInterface effect = new NoEffect();

    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        VideoRender videoRender = new VideoRender();
        setEGLContextClientVersion(2);
        setRenderer(videoRender);
    }

    /**
     * initializes media player and the effect that is going to be applied on
     * video. The video is played automatically so you dont need to call play.
     *
     * @param mediaPlayer  instance of {@link MediaPlayer}
     * @param shaderEffect any effect that implements {@link ShaderInterface}
     */
    @Deprecated
    public void init(MediaPlayer mediaPlayer, ShaderInterface shaderEffect) {
        setupMediaPlayer(mediaPlayer);
        setShader(shaderEffect);
    }

    public void init(MediaPlayer mediaPlayer, Filter filter) {
        setupMediaPlayer(mediaPlayer);
        setFilter(filter);
    }

    private void setupMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            Log.e(TAG, "Set MediaPlayer before continuing");
        } else {
            mMediaPlayer = mediaPlayer;
        }
    }

    /**
     * @param shaderEffect any effect that implements {@link ShaderInterface}
     */
    @Deprecated
    public void setShader(ShaderInterface shaderEffect) {
        this.filter = null;
        effect = shaderEffect != null ? shaderEffect : new NoEffect();
    }

    public void setFilter(Filter filter) {
        this.effect = null;
        this.filter = filter != null ? filter : new NoEffectFilter();
    }

    public ShaderInterface getShader() {
        return effect;
    }

    public Filter getFilter() {
        return filter;
    }

    @Override
    public void onResume() {
        if (mMediaPlayer == null) {
            Log.e(TAG, "Call init() before Continuing");
            return;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMediaPlayer.pause();
    }
    private class VideoRender extends BaseRenderer implements Renderer,
            SurfaceTexture.OnFrameAvailableListener {

        private SurfaceTexture mSurface;
        private boolean updateSurface = false;
        private boolean isMediaPlayerPrepared = false;

        VideoRender() {
            Matrix.setIdentityM(getTransformMatrix(), 0);
        }


        @Override
        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
            super.init();
            /*
             * Create the SurfaceTexture that will feed this textureID, and pass
             * it to the MediaPlayer
             */
            mSurface = new SurfaceTexture(getTexture());
            mSurface.setOnFrameAvailableListener(this);

            Surface surface = new Surface(mSurface);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            surface.release();

            if (!isMediaPlayerPrepared) {
                try {
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isMediaPlayerPrepared = true;
            }

            synchronized (this) {
                updateSurface = false;
            }

            mMediaPlayer.start();
        }

        @Override
        public void onSurfaceChanged(GL10 glUnused, int width, int height) {

        }

        @Override
        synchronized public void onFrameAvailable(SurfaceTexture surface) {
            updateSurface = true;
        }

        @Override
        public void onDrawFrame(GL10 glUnused) {
            synchronized (this) {
                if (updateSurface) {
                    mSurface.updateTexImage();
                    mSurface.getTransformMatrix(getTransformMatrix());
                    updateSurface = false;
                }
            }

            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            draw();
        }

        @Override
        protected void draw() {
            super.draw();
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0);
            GLES20.glFinish();
        }

        @Override
        protected int getVertexShader() {
            String vertexShaderString;
            if (effect != null) {
                vertexShaderString = DEFAULT_VERTEX_SHADER;
            } else if (filter != null) {
                vertexShaderString = filter.getVertexShader();
            } else {
                throw new IllegalStateException("Effect is not applied");
            }
            return Utils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderString);
        }

        @Override
        protected int getFragmentShader() {
            String fragmentShaderString;
            if (effect != null) {
                fragmentShaderString = effect.getShader(VideoSurfaceView.this);
            } else if (filter != null) {
                fragmentShaderString = filter.getFragmentShader();
            } else {
                throw new IllegalStateException("Effect is not applied");
            }
            return Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderString);
        }
    } // End of class VideoRender.

} // End of class VideoSurfaceView.
