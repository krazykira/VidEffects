package com.sherazkhilji.videffects.interfaces;

import android.opengl.GLSurfaceView;

/**
 * An interface that every effect must implement so that there is a common
 * getShader method that every effect class is force to override
 *
 * @author sheraz.khilji
 */
public interface ShaderInterface {

    // TODO: Convert it to instance method maybe.
    String mVertexShader = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
            + "}\n";

    /**
     * Returns Shader code
     *
     * @param mGlSurfaceView send this for every shader but this will only be used when the
     *                       shader needs it.
     * @return complete shader code in C
     */
    String getShader(GLSurfaceView mGlSurfaceView);
}
