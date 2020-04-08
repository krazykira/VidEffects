package com.sherazkhilji.videffects;

import android.opengl.GLSurfaceView;

import com.sherazkhilji.videffects.interfaces.ShaderInterface;


/**
 * Displays the normal video without any effect.
 *
 * @author sheraz.khilji
 */
public class NoEffect implements ShaderInterface {

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        return "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES sTexture;\n" + "void main() {\n"
                + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
                + "}\n";

    }
}