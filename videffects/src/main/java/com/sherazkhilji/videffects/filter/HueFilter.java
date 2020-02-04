package com.sherazkhilji.videffects.filter;

import com.sherazkhilji.videffects.Constants;
import com.sherazkhilji.videffects.interfaces.Filter;

import java.util.Locale;

public class HueFilter implements Filter {

    private static final String SHADER = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "float hue=%f;\n"
            + "void main() {\n"
            + "vec4 kRGBToYPrime = vec4 (0.299, 0.587, 0.114, 0.0);\n"
            + "vec4 kRGBToI = vec4 (0.595716, -0.274453, -0.321263, 0.0);\n"
            + "vec4 kRGBToQ = vec4 (0.211456, -0.522591, 0.31135, 0.0);\n"
            + "vec4 kYIQToR = vec4 (1.0, 0.9563, 0.6210, 0.0);\n"
            + "vec4 kYIQToG = vec4 (1.0, -0.2721, -0.6474, 0.0);\n"
            + "vec4 kYIQToB = vec4 (1.0, -1.1070, 1.7046, 0.0);\n"
            + "vec4 color = texture2D(sTexture, vTextureCoord);\n"
            + "float YPrime = dot(color, kRGBToYPrime);\n"
            + "float I = dot(color, kRGBToI);\n"
            + "float Q = dot(color, kRGBToQ);\n"
            + "float chroma = sqrt (I * I + Q * Q);\n"
            + "Q = chroma * sin (hue);\n"
            + "I = chroma * cos (hue);\n"
            + "vec4 yIQ = vec4 (YPrime, I, Q, 0.0);\n"
            + "color.r = dot (yIQ, kYIQToR);\n"
            + "color.g = dot (yIQ, kYIQToG);\n"
            + "color.b = dot (yIQ, kYIQToB);\n"
            + "gl_FragColor = color;\n"
            + "}\n";

    private float hue;

    public void setHue(float degrees) {
        this.hue = ((degrees - 45) / 45f + 0.5f) * -1;
    }

    @Override
    public String getVertexShader() {
        return Constants.DEFAULT_VERTEX_SHADER;
    }

    @Override
    public String getFragmentShader() {
        return String.format(Locale.ENGLISH, SHADER, hue);
    }
}