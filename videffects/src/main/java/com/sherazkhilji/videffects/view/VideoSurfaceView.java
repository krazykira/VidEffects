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
import com.sherazkhilji.videffects.Utils;
import com.sherazkhilji.videffects.filter.NoEffectFilter;
import com.sherazkhilji.videffects.interfaces.Filter;
import com.sherazkhilji.videffects.interfaces.ShaderInterface;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static com.sherazkhilji.videffects.Constants.DEFAULT_VERTEX_SHADER;
import static com.sherazkhilji.videffects.Constants.FLOAT_SIZE_BYTES;

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
    private @Deprecated
    ShaderInterface effect;
    private Filter filter;
    private VideoRender videoRender;

    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.videoRender = new VideoRender();
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
        effect = shaderEffect != null ? shaderEffect : new NoEffect();
    }

    public void init(MediaPlayer mediaPlayer, Filter filter) {
        setupMediaPlayer(mediaPlayer);
        this.filter = filter != null ? filter : new NoEffectFilter();
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

    private class VideoRender implements Renderer,
            SurfaceTexture.OnFrameAvailableListener {

        float[] texMatrix = new float[16];
        float[] mvpMatrix = new float[16];

        private int program;
        private int[] bufferHandles = new int[2];
        private int[] textureHandles = new int[2];
        private int mvpMatrixHandle;
        private int texMatrixHandle;
        private int vertexHandle;
        private int uvsHandle;

        private SurfaceTexture mSurface;
        private boolean updateSurface = false;
        private boolean isMediaPlayerPrepared = false;

        // TODO: Fix video orientation
        VideoRender() {
            Matrix.setIdentityM(texMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 glUnused) {
            synchronized (this) {
                if (updateSurface) {
                    mSurface.updateTexImage();
                    mSurface.getTransformMatrix(texMatrix);
                    updateSurface = false;
                }
            }
            setProgram();
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(program);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureHandles[0]);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0]);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1]);

            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 4 * 5, 0);
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glVertexAttribPointer(uvsHandle, 2, GLES20.GL_FLOAT, false, 4 * 5, 3 * 4);
            GLES20.glEnableVertexAttribArray(uvsHandle);

            Matrix.setIdentityM(mvpMatrix, 0);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
            GLES20.glUniformMatrix4fv(texMatrixHandle, 1, false, mvpMatrix, 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0);

            GLES20.glFinish();

        }

        @Override
        public void onSurfaceChanged(GL10 glUnused, int width, int height) {

        }

        @Override
        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
            setProgram();
            
            vertexHandle = GLES20.glGetAttribLocation(program, "aPosition");
            uvsHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
            mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
            texMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");

            GLES20.glGenTextures(2, textureHandles, 0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureHandles[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glGenBuffers(2, bufferHandles, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Utils.VERTICES.length * FLOAT_SIZE_BYTES, Utils.getVertexBuffer(), GLES20.GL_DYNAMIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, Utils.INDICES.length * FLOAT_SIZE_BYTES, Utils.getIndicesBuffer(), GLES20.GL_DYNAMIC_DRAW);

            /*
             * Create the SurfaceTexture that will feed this textureID, and pass
             * it to the MediaPlayer
             */
            mSurface = new SurfaceTexture(textureHandles[0]);
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
        synchronized public void onFrameAvailable(SurfaceTexture surface) {
            updateSurface = true;
        }

        private void setProgram() {
            if (effect != null) {
                program = createProgram(effect.getShader(VideoSurfaceView.this));
            } else if (filter != null) {
                program = createProgram(filter.getFragmentShader());
            } else {
                return;
            }
        }

        private int createProgram(String fragmentSource) {
            int vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, DEFAULT_VERTEX_SHADER);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            if (program != 0) {
                GLES20.glAttachShader(program, vertexShader);
                GLES20.glAttachShader(program, pixelShader);
                GLES20.glLinkProgram(program);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,
                        linkStatus, 0);
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    Log.e(TAG, "Could not link program: ");
                    Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    program = 0;
                }
            }
            return program;
        }

    } // End of class VideoRender.

} // End of class VideoSurfaceView.
