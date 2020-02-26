package com.sherazkhilji.videffects.model;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.sherazkhilji.videffects.interfaces.ConvertResultListener;
import com.sherazkhilji.videffects.interfaces.Filter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

@RequiresApi(Build.VERSION_CODES.M)
public class Converter {

    private static final String TAG = "kifio-Converter";
    private static final String VIDEO = "video/";
    private static final String AUDIO = "audio/";
    private static final String OUT_MIME = "video/avc";
    private static final int DEFAULT_FRAMERATE = 15;

    protected MediaExtractor videoExtractor = new MediaExtractor();
    protected MediaExtractor audioExtractor = new MediaExtractor();

    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;

    protected int width;
    protected int height;
    protected int bitrate;

    private TextureRenderer textureRenderer = null;
    private MediaMuxer muxer = null;
    private MediaCodec decoder = null;
    private MediaCodec encoder = null;
    private SurfaceTexture surfaceTexture = null;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private HandlerThread thread = null;
    private Surface inputSurface, outputSurface;

    private boolean allInputExtracted = false;
    private boolean allInputDecoded = false;
    private boolean allOutputEncoded = false;

    private long mediaCodedTimeoutUs = 1000L;
    private final Object lock = new Object();
    private volatile boolean frameAvailable = false;

    private EGLDisplay eglDisplay = null;
    private EGLContext eglContext = null;
    private EGLSurface eglSurface = null;

    public Converter() {

    }

    public Converter(String path) {
        setMetadata(path);
        try {
            videoExtractor.setDataSource(path);
            audioExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMetadata(String path) {
        Metadata metadata = new MetadataExtractor().extract(path);
        if (metadata != null) {
            width = (int) metadata.getWidth();
            height = (int) metadata.getHeight();
            bitrate = metadata.getBitrate();
        }
    }
    public void startConverter(Filter filter, String outPath, ConvertResultListener listener) {
        try {
            muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            long startTime = System.nanoTime();
            convertVideo(filter);
            if (audioTrackIndex != -1) {
                addAudio();
            }
            long endTime = System.nanoTime();
            Log.d(TAG, "Convert time: " + TimeUnit.NANOSECONDS.toSeconds(endTime - startTime));
            if (listener != null) listener.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onFail();
        } finally {
            releaseConverter();
        }
    }

    private void convertVideo(Filter filter) throws Exception {
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat format = videoExtractor.getTrackFormat(i);
            convert(i, format, filter);
        }
    }

    private void convert(int trackIndex, MediaFormat format, Filter filter) throws Exception {
        String mime = format.getString(MediaFormat.KEY_MIME);

        if (mime == null || !mime.startsWith(VIDEO)) return;

        videoExtractor.selectTrack(trackIndex);

        // Create H.264 encoder
        encoder = MediaCodec.createEncoderByType(mime);

        // Configure the encoder
        int frameRate = format.containsKey(MediaFormat.KEY_FRAME_RATE)
                ? format.getInteger(MediaFormat.KEY_FRAME_RATE)
                : DEFAULT_FRAMERATE;

        encoder.configure(getVideoFormat(frameRate), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = encoder.createInputSurface();

        initEgl(inputSurface);

        // Init output surface
        textureRenderer = new TextureRenderer(filter);
        surfaceTexture = new SurfaceTexture(textureRenderer.getTexture());

        // Control the thread from which OnFrameAvailableListener will be called
        thread = new HandlerThread("FrameHandlerThread");
        thread.start();

        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                synchronized (lock) {
                    // New frame available before the last frame was process...we dropped some frames
                    if (frameAvailable) {
                        Log.d(TAG, "Frame available before the last frame was process...we dropped some frames");
                    }
                    frameAvailable = true;
                    lock.notifyAll();
                }
            }
        }, new Handler(thread.getLooper()));

        outputSurface = new Surface(surfaceTexture);
        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(format, outputSurface, null, 0);

        encoder.start();
        decoder.start();
        convert();
    }

    private void addAudio() {
        int maxChunkSize = 1024 * 1024;
        ByteBuffer buffer = ByteBuffer.allocate(maxChunkSize);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        while (true) {

            int chunkSize = audioExtractor.readSampleData(buffer, 0);

            if (chunkSize >= 0) {
                bufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                bufferInfo.flags = audioExtractor.getSampleFlags();
                bufferInfo.size = chunkSize;

                muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo);
                audioExtractor.advance();
            } else {
                break;
            }
        }
    }

    private MediaFormat getVideoFormat(int frameRate) {
        MediaFormat format = MediaFormat.createVideoFormat(OUT_MIME, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 20);
        format.setString(MediaFormat.KEY_MIME, OUT_MIME);
        return format;
    }

    private MediaFormat getAudioFormat() {
        for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
            MediaFormat format = audioExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null &&
                    (mime.startsWith(AUDIO) || format.containsKey(MediaFormat.KEY_CHANNEL_COUNT))) {
                audioExtractor.selectTrack(i);
                return format;
            }
        }
        return null;
    }

    private void initEgl(Surface inputSurface) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "eglDisplay == EGL14.EGL_NO_DISPLAY: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        int[] version = new int[2];

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

        int[] ctxAttrs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };

        int[] surfaceAttrs = {
                EGL14.EGL_NONE
        };

        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            Log.e(TAG, "eglInitialize: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        if (!EGL14.eglChooseConfig(eglDisplay, attrsList, 0, configs, 0, configs.length, new int[configs.length], 0)) {
            Log.e(TAG, GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttrs, 0);
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], inputSurface, surfaceAttrs, 0);

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            Log.d(TAG, "eglMakeCurrent: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
    }

    private void convert() {
        allInputExtracted = false;
        allInputDecoded = false;
        allOutputEncoded = false;

        // Extract, decode, edit, encode, and mux
        while (!allOutputEncoded) {
            // Feed input to decoder
            if (!allInputExtracted) {
                feedInputToDecoder();
            }

            boolean encoderOutputAvailable = true;
            boolean decoderOutputAvailable = !allInputDecoded;

            int outBufferId;
            while (encoderOutputAvailable || decoderOutputAvailable) {
                // Drain Encoder & mux to output file first
                outBufferId = encoder.dequeueOutputBuffer(bufferInfo, mediaCodedTimeoutUs);
                if (outBufferId >= 0) {
                    ByteBuffer encodedBuffer = encoder.getOutputBuffer(outBufferId);
                    if (encodedBuffer != null) {
                        muxer.writeSampleData(videoTrackIndex, encodedBuffer, bufferInfo);
                        encoder.releaseOutputBuffer(outBufferId, false);
                        // Are we finished here?
                        if ((bufferInfo.flags & BUFFER_FLAG_END_OF_STREAM) != 0) {
                            allOutputEncoded = true;
                            break;
                        }
                    }
                } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    encoderOutputAvailable = false;
                } else if (outBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoTrackIndex = muxer.addTrack(encoder.getOutputFormat());
                    MediaFormat audioFormat = getAudioFormat();
                    if (audioFormat != null) {
                        audioTrackIndex = muxer.addTrack(audioFormat);
                    }
                    muxer.start();
                }

                if (outBufferId != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    continue;
                }

                // Get output from decoder and feed it to encoder
                if (!allInputDecoded) {
                    outBufferId = decoder.dequeueOutputBuffer(bufferInfo, mediaCodedTimeoutUs);
                    if (outBufferId >= 0) {
                        boolean render = bufferInfo.size > 0;
                        // Give the decoded frame to SurfaceTexture (onFrameAvailable() callback should
                        // be called soon after this)
                        decoder.releaseOutputBuffer(outBufferId, render);
                        if (render) {
                            // Wait till new frame available after onFrameAvailable has been called
                            waitTillFrameAvailable();

                            surfaceTexture.updateTexImage();
                            surfaceTexture.getTransformMatrix(textureRenderer.getTransformMatrix());

                            // Draw texture with opengl
                            textureRenderer.draw(width, height);

                            EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface,
                                    bufferInfo.presentationTimeUs * 1000);

                            EGL14.eglSwapBuffers(eglDisplay, eglSurface);
                        }

                        // Did we get all output from decoder?
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            allInputDecoded = true;
                            encoder.signalEndOfInputStream();
                        }
                    } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        decoderOutputAvailable = false;
                    }
                }
            }
        }
    }

    private void feedInputToDecoder() {
        int inBufferId = decoder.dequeueInputBuffer(mediaCodedTimeoutUs);
        if (inBufferId >= 0) {
            ByteBuffer buffer = decoder.getInputBuffer(inBufferId);
            if (buffer != null) {
                int sampleSize = videoExtractor.readSampleData(buffer, 0);
                if (sampleSize >= 0) {
                    decoder.queueInputBuffer(inBufferId, 0, sampleSize,
                            videoExtractor.getSampleTime(), videoExtractor.getSampleFlags());
                    videoExtractor.advance();
                } else {
                    decoder.queueInputBuffer(inBufferId, 0, 0,
                            0, BUFFER_FLAG_END_OF_STREAM);
                    allInputExtracted = true;
                }
            }
        }
    }

    private void waitTillFrameAvailable() {
        synchronized (lock) {
            while (!frameAvailable) {
                try {
                    lock.wait(100);
                    if (!frameAvailable) {
                        Log.e(TAG, "Surface frame wait timed out");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            frameAvailable = false;
        }
    }

    private void releaseConverter() {

        if (videoExtractor != null) {
            videoExtractor.release();
            videoExtractor = null;
        }

        if (decoder != null) {
            decoder.release();
            decoder = null;
        }


        if (encoder != null) {
            encoder.release();
            encoder = null;
        }

        releaseEgl();

        if (outputSurface != null) {
            outputSurface.release();
            outputSurface = null;
        }

        if (muxer != null) {
            muxer.release();
            muxer = null;
        }

        if (thread != null) {
            thread.quitSafely();
            thread = null;
        }
    }

    private void releaseEgl() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface);
            EGL14.eglDestroyContext(eglDisplay, eglContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(eglDisplay);
        }

        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }

        eglDisplay = EGL14.EGL_NO_DISPLAY;
        eglContext = EGL14.EGL_NO_CONTEXT;
        eglSurface = EGL14.EGL_NO_SURFACE;
    }
}
