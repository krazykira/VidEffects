package com.sherazkhilji.videffects;

import android.opengl.GLES20;
import android.util.Log;

public class Utils {

    private static final String TAG = "Utils";

    private Utils() {

    }

    public static float[] VERTICES = {
            -1.0f, -1.0f, 0.0f, 0.0f, 0.f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.f,
            -1.0f, 1.0f, 0.0f, 0.0f, 1.f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f
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
}
