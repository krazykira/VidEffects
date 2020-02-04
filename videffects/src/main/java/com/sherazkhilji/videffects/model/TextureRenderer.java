package com.sherazkhilji.videffects.model;

import android.opengl.GLES20;

import com.sherazkhilji.videffects.interfaces.Filter;

class TextureRenderer extends BaseRenderer {

    private Filter filter;

    TextureRenderer(Filter filter) {
        super.init();
        this.filter = filter;
    }

    void draw(int viewportWidth, int viewportHeight) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
        super.draw();
    }

    @Override
    protected int getFragmentShader() {
        return Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, filter.getFragmentShader());
    }

    @Override
    protected int getVertexShader() {
        return Utils.loadShader(GLES20.GL_VERTEX_SHADER, filter.getVertexShader());
    }
}