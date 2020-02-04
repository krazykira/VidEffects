package com.sherazkhilji.videffects.filter;

import android.os.Parcel;

import com.sherazkhilji.videffects.Constants;
import com.sherazkhilji.videffects.interfaces.Filter;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class GrainFilter implements Filter {

    private float strength = 0.0F;

    private String shaderString;

    public GrainFilter(int width, int height) {
        shaderString = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + " vec2 seed;\n"
                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES tex_sampler_0;\n"
                + "uniform samplerExternalOES tex_sampler_1;\n"
                + "float scale;\n"
                + "float stepX;\n"
                + "float stepY;\n"
                + "float rand(vec2 loc) {\n"
                + "  float theta1 = dot(loc, vec2(0.9898, 0.233));\n"
                + "  float theta2 = dot(loc, vec2(12.0, 78.0));\n"
                + "  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n"
                + "  float temp = mod(197.0 * value, 1.0) + value;\n"
                + "  float part1 = mod(220.0 * temp, 1.0) + temp;\n"
                + "  float part2 = value * 0.5453;\n"
                + "  float part3 = cos(theta1 + theta2) * 0.43758;\n"
                + "  float sum = (part1 + part2 + part3);\n"
                + "  return fract(sum)*scale;\n"
                + "}\n"
                + "void main() {\n"
                + "  seed[0] = %f;\n"
                + "  seed[1] = %f;\n"
                + "  scale = %f;\n"
                + "  stepX = " + 0.5f / width + ";\n"
                + "  stepY = " + 0.5f / height + ";\n"
                + "  float noise = texture2D(tex_sampler_1, vTextureCoord + vec2(-stepX, -stepY)).r * 0.224;\n"
                + "  noise += texture2D(tex_sampler_1, vTextureCoord + vec2(-stepX, stepY)).r * 0.224;\n"
                + "  noise += texture2D(tex_sampler_1, vTextureCoord + vec2(stepX, -stepY)).r * 0.224;\n"
                + "  noise += texture2D(tex_sampler_1, vTextureCoord + vec2(stepX, stepY)).r * 0.224;\n"
                + "  noise += 0.4448;\n"
                + "  noise *= scale;\n"
                + "  vec4 color = texture2D(tex_sampler_0, vTextureCoord);\n"
                + "  float energy = 0.33333 * color.r + 0.33333 * color.g + 0.33333 * color.b;\n"
                + "  float mask = (1.0 - sqrt(energy));\n"
                + "  float weight = 1.0 - 1.333 * mask * noise;\n"
                + "  gl_FragColor = vec4(color.rgb * weight, color.a);\n"
                + "  gl_FragColor = gl_FragColor+vec4(rand(vTextureCoord + seed), rand(vTextureCoord + seed),rand(vTextureCoord + seed),1);\n"
                + "}\n";

    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    @Override
    public String getVertexShader() {
        return Constants.DEFAULT_VERTEX_SHADER;
    }

    @Override
    public String getFragmentShader() {
        return String.format(Locale.ENGLISH, shaderString,
                ThreadLocalRandom.current().nextFloat(),
                ThreadLocalRandom.current().nextFloat(),
                strength);
    }
}
