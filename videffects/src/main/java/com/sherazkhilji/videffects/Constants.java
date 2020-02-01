package com.sherazkhilji.videffects;

public final class Constants {

    public static final int FLOAT_SIZE_BYTES = 4;

    public static final String DEFAULT_VERTEX_SHADER = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
            + "}\n";
}
