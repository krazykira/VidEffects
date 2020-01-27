package com.sherazkhilji.videffects.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.Surface;

import com.sherazkhilji.videffects.NoEffect;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

class SavingService extends IntentService {

    private static final String PATH = "path";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String TAG = "SavingService";
    private static final String VIDEO = "video/";

    private static final int MAX_CHUNK_SIZE = 1024 * 1024;

    public static void saveVideo(Context context, String path, int width, int height) {
        Intent intent = new Intent(context, SavingService.class);
        intent.putExtra(WIDTH, width);
        intent.putExtra(HEIGHT, height);
        context.startService(intent.putExtra(PATH, path));
    }

    private MediaExtractor extractor = new MediaExtractor();
    private MediaCodec decoder = null;
    private MediaCodec encoder = null;
    private SurfaceTexture surfaceTexture = null;

    public SavingService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String path = intent.getStringExtra(PATH);
        int width = intent.getIntExtra(WIDTH, 0);
        int height = intent.getIntExtra(HEIGHT, 0);

        if (path == null) {
            Log.e(TAG, "Path to video is empty");
            return;
        }

        try {
            extractor.setDataSource(path);
            for (int i = 0; i < extractor.getTrackCount(); i++) {

                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);

                if (mime != null && mime.startsWith(VIDEO)) {
                    extractor.selectTrack(i);

                    // Create H.264 encoder
                    encoder = MediaCodec.createEncoderByType(mime);

                    // Configure the encoder
                    encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                    Surface inputSurface = encoder.createInputSurface();

                    initEgl(inputSurface);

                    // Init output surface
                    TextureRenderer textureRenderer = new TextureRenderer(new NoEffect());
                    surfaceTexture = new SurfaceTexture(textureRenderer.getTextureId());

                    surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            // Listen for it in the same thread, also possible in other thread.
                        }
                    });

                    Surface outputSurface = new Surface(surfaceTexture);
                    decoder = MediaCodec.createDecoderByType(mime);
                    decoder.configure(format, outputSurface, null, 0);

//                    muxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

                    encoder.start();
                    decoder.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proccessTheVideo() {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_CHUNK_SIZE);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        int chunkSize;
        while (true) {
            chunkSize = extractor.readSampleData(buffer, 0);

            if (chunkSize != 0) {
                extractor.advance();
            } else {
                return;
            }
        }
    }

    private void initEgl(Surface inputSurface) {
        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "eglDisplay == EGL14.EGL_NO_DISPLAY: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        int[] version = new int[2];

        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            Log.e(TAG, "eglInitialize: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        int[] attrsList = new int[]{
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] nConfigs = new int[1];

        if (!EGL14.eglChooseConfig(eglDisplay, attrsList, 0, configs, 0, configs.length, nConfigs, 0)) {
            Log.e(TAG, "eglChooseConfig: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        int err = EGL14.eglGetError();
        if (err != EGL14.EGL_SUCCESS) {
            Log.e(TAG, GLUtils.getEGLErrorString(err));
        }

        int[] ctxAttrs = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };

        EGLContext eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttrs, 0);

        err = EGL14.eglGetError();
        if (err != EGL14.EGL_SUCCESS) {
            Log.e(TAG, GLUtils.getEGLErrorString(err));
        }

        int[] surfaceAttrs = new int[]{
                EGL14.EGL_NONE
        };

        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], inputSurface, surfaceAttrs, 0);

        err = EGL14.eglGetError();
        if (err != EGL14.EGL_SUCCESS) {
            Log.e(TAG, GLUtils.getEGLErrorString(err));
        }

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            Log.d(TAG, "eglMakeCurrent: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
    }
}