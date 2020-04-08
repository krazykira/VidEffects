package com.sherazkhilji.videffects.model;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static com.sherazkhilji.videffects.Constants.FLOAT_SIZE_BYTES;


public abstract class BaseRenderer {

    private static final String TAG = "BaseRenderer";

    private int[] bufferHandles = new int[2];
    private int[] textureHandles = new int[2];

    private float[] texMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private int[] linkStatus = new int[1];

    protected BaseRenderer() {

    }

    protected abstract int getVertexShader();

    protected abstract int getFragmentShader();

    protected int getTexture() {
        return textureHandles[0];
    }

    protected float[] getTransformMatrix() {
        return texMatrix;
    }

    private int createProgram() {
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, getVertexShader());
            GLES20.glAttachShader(program, getFragmentShader());
            GLES20.glLinkProgram(program);
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    protected void init() {
        GLES20.glGenBuffers(2, bufferHandles, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Utils.VERTICES.length * FLOAT_SIZE_BYTES, Utils.getVertexBuffer(), GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, Utils.INDICES.length * FLOAT_SIZE_BYTES, Utils.getIndicesBuffer(), GLES20.GL_DYNAMIC_DRAW);
        GLES20.glGenTextures(2, textureHandles, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureHandles[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    protected void draw() {
        Matrix.setIdentityM(mvpMatrix, 0);

        int program = createProgram();
        int vertexHandle = GLES20.glGetAttribLocation(program, "aPosition");
        int uvsHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        int texMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        int samplerHandle = GLES20.glGetUniformLocation(program, "sTexture");

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(texMatrixHandle, 1, false, texMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1]);
        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 4 * 5, 0);
        GLES20.glEnableVertexAttribArray(uvsHandle);
        GLES20.glVertexAttribPointer(uvsHandle, 2, GLES20.GL_FLOAT, false, 4 * 5, 3 * 4);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0);
    }
}