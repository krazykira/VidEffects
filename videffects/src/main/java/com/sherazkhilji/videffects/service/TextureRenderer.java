package com.sherazkhilji.videffects.service;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.sherazkhilji.videffects.Utils;
import com.sherazkhilji.videffects.interfaces.ShaderInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

class TextureRenderer {

    private int[] indices = new int[]{
            2, 1, 0, 0, 3, 2
    };

    private int program;
    private int vertexHandle = 0;
    private int[] bufferHandles = new int[2];
    private int uvsHandle = 0;
    private int texMatrixHandle = 0;
    private int mvpHandle = 0;
    private int samplerHandle = 0;
    private int[] textureHandles = new int[1];

    int getTextureId() {
        return textureHandles[0];
    }

    TextureRenderer(ShaderInterface shaderInterface) {
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(Utils.VERTICES.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexBuffer.put(Utils.VERTICES);
        vertexBuffer.position(0);

        IntBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();

        indexBuffer.put(indices);
        indexBuffer.position(0);

        // Get Shaders from ShaderInterface.
        int vertexShader  = Utils.loadShader(GLES20.GL_VERTEX_SHADER, "");
        int fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, "");

        program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        vertexHandle = GLES20.glGetAttribLocation(program, "vertexPosition");
        uvsHandle = GLES20.glGetAttribLocation(program, "uvs");
        texMatrixHandle = GLES20.glGetUniformLocation(program, "texMatrix");
        mvpHandle = GLES20.glGetUniformLocation(program, "mvp");
        samplerHandle = GLES20.glGetUniformLocation(program, "texSampler");

        GLES20.glGenBuffers(2, bufferHandles, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Utils.VERTICES.length * 4, vertexBuffer, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4, indexBuffer, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glGenTextures(1, textureHandles, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureHandles[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void draw(int viewportWidth, int viewportHeight, float[] texMatrix, float[] mvpMatrix) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
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