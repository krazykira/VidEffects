package com.sherazkhilji.videffects.model;

import android.media.MediaMetadataRetriever;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Utils {

    private static final String TAG = "Utils";

    private Utils() {

    }

    public static float[] VERTICES = {
            -1.0f, -1.0f, 0.0f, 0f, 0f,
            -1.0f,  1.0f, 0.0f, 0f, 1f,
             1.0f,  1.0f, 0.0f, 1f, 1f,
             1.0f, -1.0f, 0.0f, 1f, 0f
    };

    public static int[] INDICES = {
            2, 1, 0, 0, 3, 2
    };

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,
                    compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static FloatBuffer getVertexBuffer() {
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(Utils.VERTICES.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(Utils.VERTICES);
        vertexBuffer.position(0);
        return vertexBuffer;
    }

    public static IntBuffer getIndicesBuffer() {
        IntBuffer indexBuffer = ByteBuffer.allocateDirect(Utils.INDICES.length * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(Utils.INDICES);
        indexBuffer.position(0);
        return indexBuffer;
    }
}
