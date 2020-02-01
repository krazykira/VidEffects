package com.sherazkhilji.videffects.service;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.sherazkhilji.videffects.Utils;
import com.sherazkhilji.videffects.interfaces.Filter;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static com.sherazkhilji.videffects.Constants.FLOAT_SIZE_BYTES;

class TextureRenderer {

    private int program;
    private int vertexHandle = 0;
    private int uvsHandle = 0;
    private int texMatrixHandle = 0;
    private int mvpHandle = 0;
    private int samplerHandle = 0;
    private int[] bufferHandles = new int[2];
    private int[] textureHandles = new int[1];

    float[] texMatrix = new float[16];
    float[] mvpMatrix = new float[16];

    int getTextureId() {
        return textureHandles[0];
    }

    private Filter filter;

    TextureRenderer(Filter filter) {
        this.filter = filter;
        GLES20.glGenBuffers(2, bufferHandles, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Utils.VERTICES.length * FLOAT_SIZE_BYTES, Utils.getVertexBuffer(), GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, Utils.INDICES.length * FLOAT_SIZE_BYTES, Utils.getIndicesBuffer(), GLES20.GL_DYNAMIC_DRAW);
        GLES20.glGenTextures(1, textureHandles, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureHandles[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    void draw(int viewportWidth, int viewportHeight) {
        Matrix.setIdentityM(mvpMatrix, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);

        int vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER,
                filter.getVertexShader());
        int fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER,
                filter.getFragmentShader());

        program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        vertexHandle = GLES20.glGetAttribLocation(program, "aPosition");
        uvsHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        texMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        samplerHandle = GLES20.glGetUniformLocation(program, "sTexture");

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(texMatrixHandle, 1, false, texMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1]);
        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 4 * 5, 0);
        GLES20.glEnableVertexAttribArray(uvsHandle);
        GLES20.glVertexAttribPointer(uvsHandle, 2, GLES20.GL_FLOAT, false, 4 * 5, 3 * 4);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0);
    }
}