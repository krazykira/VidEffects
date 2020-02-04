package com.sherazkhilji.videffects.model;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.sherazkhilji.videffects.model.Utils;
import com.sherazkhilji.videffects.interfaces.Filter;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static com.sherazkhilji.videffects.Constants.FLOAT_SIZE_BYTES;

class TextureRenderer extends BaseRenderer {

    int getTextureId() {
        return textureHandles[0];
    }

    TextureRenderer(Filter filter) {
        super(filter);
        super.init();
    }

    void draw(int viewportWidth, int viewportHeight) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
        super.draw();
    }
}